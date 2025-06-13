package ch.oceandive.service;

import ch.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.repository.PremiumUserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResetPasswordService {

  private static final Logger logger = LoggerFactory.getLogger(ResetPasswordService.class);

  private final PremiumUserRepo premiumUserRepo;
  private final PasswordEncoder passwordEncoder;
  private final JavaMailSender mailSender;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  @Value("${app.password-reset.token-validity-hours:2}")
  private int tokenValidityHours;

  // Rate limiting for password reset requests
  private final Map<String, LocalDateTime> resetAttempts = new ConcurrentHashMap<>();

  public ResetPasswordService(PremiumUserRepo premiumUserRepo,
      PasswordEncoder passwordEncoder,
      @Autowired(required = false) JavaMailSender mailSender) {
    this.premiumUserRepo = premiumUserRepo;
    this.passwordEncoder = passwordEncoder;
    this.mailSender = mailSender;
  }

  /**
   * Initiates password reset process with rate limiting
   *
   * @param email User's email address
   */
  @Transactional
  public void initiatePasswordReset(String email) {
    String normalizedEmail = email.toLowerCase().trim();
    // Rate limiting: max 3 attempts per hour per email
    if (isRateLimited(normalizedEmail)) {
      logger.warn("Rate limit exceeded for password reset: {}", normalizedEmail);
      return; // Don't reveal rate limiting to user for security
    }
    PremiumUser user = premiumUserRepo.findByEmail(normalizedEmail);
    if (user == null) {
      logger.info("Password reset requested for non-existent email: {}", normalizedEmail);
      return; // Don't reveal that email doesn't exist
    }
    // Generate and store reset token
    String token = UUID.randomUUID().toString();
    LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(tokenValidityHours);
    user.setPasswordResetToken(token);
    user.setPasswordResetTokenExpiry(tokenExpiry);
    premiumUserRepo.save(user);
    // Record reset attempt for rate limiting
    resetAttempts.put(normalizedEmail, LocalDateTime.now());
    // Send reset email
    sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token);
    logger.info("Password reset initiated for user: {}", user.getUsername());
  }
  /**
   * Validates a password reset token
   * @param token Reset token
   * @return true if token is valid and not expired
   */
  public boolean validateResetToken(String token) {
    if (token == null || token.trim().isEmpty()) {
      return true;
    }
    try {
      PremiumUser user = premiumUserRepo.findByPasswordResetToken(token);
      if (user == null) {
        return true;
      }
      if (user.getPasswordResetTokenExpiry() == null) {
        return true;
      }
      if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
        // Auto-cleanup expired tokens
        cleanupExpiredToken(user);
        return true;
      }
      return false;
    } catch (Exception e) {
      logger.error("Error validating reset token", e);
      return true;
    }
  }
  /**
   * Gets user by reset token (for email verification)
   * @param token Reset token
   * @return PremiumUser or null if token invalid
   */
  public PremiumUser getUserByResetToken(String token) {
    if (validateResetToken(token)) {
      return null;
    }
    try {
      PremiumUser user = premiumUserRepo.findByPasswordResetToken(token);
      // Double-check token is still valid
      if (user != null && user.getPasswordResetTokenExpiry() != null) {
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
          cleanupExpiredToken(user);
          return null;
        }
      }
      return user;
    } catch (Exception e) {
      logger.error("Error retrieving user by reset token", e);
      return null;
    }
  }
  /**
   * Resets password using a valid token (single-use)
   * @param token Reset token
   * @param newPassword New password
   * @throws ResourceNotFoundException if token is invalid
   * @throws IllegalArgumentException if token is expired or password invalid
   */
  @Transactional
  public void resetPassword(String token, String newPassword) {
    if (newPassword == null || newPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("Password cannot be empty");
    }
    if (newPassword.length() < 6) {
      throw new IllegalArgumentException("Password must be at least 6 characters long");
    }
    PremiumUser user = getUserByResetToken(token);
    if (user == null || validateResetToken(token)) {
      throw new ResourceNotFoundException("Invalid or expired reset token");
    }
    // Update the password and immediately invalidate the token (single-use)
    user.setPassword(passwordEncoder.encode(newPassword));
    user.setPasswordResetToken(null);
    user.setPasswordResetTokenExpiry(null);
    premiumUserRepo.save(user);
    logger.info("Password successfully reset for user: {}", user.getUsername());
    logger.info("SECURITY EVENT: Password reset completed for user: {} at {}",
        user.getUsername(), LocalDateTime.now());
  }
  /**
   * Verifies that the provided email matches the token owner's email
   * @param token Reset token
   * @param email Email to verify
   * @return true if email matches token owner
   */
  public boolean verifyEmailForToken(String token, String email) {
    if (token == null || email == null) {
      return false;
    }
    PremiumUser user = getUserByResetToken(token);
    if (user == null) {
      return false;
    }
    String normalizedEmail = email.toLowerCase().trim();
    return normalizedEmail.equals(user.getEmail().toLowerCase());
  }
  /**
   * Checks if email is rate limited for password reset requests
   */
  private boolean isRateLimited(String email) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneHourAgo = now.minusHours(1);
    // Clean up old entries
    resetAttempts.entrySet().removeIf(entry -> entry.getValue().isBefore(oneHourAgo));
    // Count recent attempts for this email
    long recentAttempts = resetAttempts.entrySet().stream()
        .filter(entry -> entry.getKey().equals(email))
        .filter(entry -> entry.getValue().isAfter(oneHourAgo))
        .count();
    return recentAttempts >= 3;
  }
  /**
   * Cleans up expired tokens
   */
  private void cleanupExpiredToken(PremiumUser user) {
    user.setPasswordResetToken(null);
    user.setPasswordResetTokenExpiry(null);
    premiumUserRepo.save(user);
  }
  /**
   * Sends password reset email
   */
  private void sendPasswordResetEmail(String email, String firstName, String token) {
    String resetUrl = baseUrl + "/reset-password?token=" + token;
    if (mailSender == null) {
      return;
    }
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(email);
      message.setSubject("OceanDive - Password Reset Request");
      message.setText(buildEmailContent(firstName, resetUrl));
      mailSender.send(message);
      logger.info("Password reset email sent to: {}", email);
    } catch (Exception e) {
      logger.error("Failed to send password reset email to {}: {}", email, e.getMessage());
    }
  }
  /**
   * Builds the email content for password reset
   */
  private String buildEmailContent(String firstName, String resetUrl) {
    return String.format("""
            Hi %s,
            
            You requested a password reset for your OceanDive account.
            
            Click the link below to reset your password:
            %s
            
            For your security, you will need to verify your email address before setting a new password.
            
            This link will expire in %d hours.
            
            If you didn't request this password reset, please ignore this email.
            Your account remains secure and no changes have been made.
            
            Best regards,
            The OceanDive Team
            
            ---
            For security reasons, please do not reply to this email.
            """, firstName, resetUrl, tokenValidityHours);
  }
}
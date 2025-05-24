package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.dto.AdminDTO;
import ch.fhnw.oceandive.dto.ApiResponse;
import ch.fhnw.oceandive.dto.LoginRequest;
import ch.fhnw.oceandive.dto.PremiumUserDTO;
import ch.fhnw.oceandive.exceptionHandler.DuplicateResourceException;
import ch.fhnw.oceandive.model.UserDetailsServiceImpl;
import ch.fhnw.oceandive.security.TokenService;
import ch.fhnw.oceandive.service.AdminService;
import ch.fhnw.oceandive.service.PremiumUserService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
  private static final int MAX_LOGIN_ATTEMPTS = 5;
  private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

  // Track failed login attempts and lockout times
  private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
  private final Map<String, Instant> lockoutTimes = new ConcurrentHashMap<>();

  private final TokenService tokenService;
  private final UserDetailsServiceImpl userDetailsService;
  private final PremiumUserService premiumUserService;
  private final AdminService adminService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public AuthController(
      TokenService tokenService,
      UserDetailsServiceImpl userDetailsService,
      PremiumUserService premiumUserService,
      AdminService adminService,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager) {
    this.tokenService = tokenService;
    this.userDetailsService = userDetailsService;
    this.premiumUserService = premiumUserService;
    this.adminService = adminService;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/token")
  public ResponseEntity<ApiResponse> token(Authentication authentication) {
    if (authentication.isAuthenticated()) {
      Map<String, Object> data = generateTokenResponse(authentication);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new ApiResponse(true, "Authentication successful", data));
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse(false, "Authentication failed", null));
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse> refresh(@RequestParam String refreshToken) {
    // Validate the refresh token
    if (!tokenService.validateToken(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse(false, "Invalid or blacklisted refresh token", null));
    }

    try {
      String username = tokenService.getUsernameFromToken(refreshToken);
      if (username == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse(false, "Invalid refresh token", null));
      }

      // Check if the user still exists
      try {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // User exists, generate a new token with their authorities
        Authentication auth = new UsernamePasswordAuthenticationToken(
            username, null, userDetails.getAuthorities());

        Map<String, Object> data = generateTokenResponse(auth);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(true, "Token refreshed successfully", data));
      } catch (UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse(false, "User no longer exists", null));
      }
    } catch (Exception e) {
      logger.error("Error processing refresh token", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse(false, "Invalid refresh token", null));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      tokenService.blacklistToken(token);
      return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully", null));
    }
    return ResponseEntity.badRequest()
        .body(new ApiResponse(false, "Invalid token", null));
  }

  /**
   * Login endpoint for users.
   * @return A token if authentication is successful
   */
  @PostMapping("/user/login")
  public ResponseEntity<ApiResponse> userLogin(@Valid @RequestBody LoginRequest loginRequest) {
    return processLogin(loginRequest, "ROLE_PREMIUM", "User is not a premium user",
        "User logged in successfully");
  }

  /**
   * Login endpoint for admins.
   * @return A token if authentication is successful
   */
  @PostMapping("/admin/login")
  public ResponseEntity<ApiResponse> adminLogin(@Valid @RequestBody LoginRequest loginRequest) {
    return processLogin(loginRequest, "ROLE_ADMIN", "User is not an admin",
        "Admin logged in successfully");
  }

  /**
   * Common login processing logic for different user types
   *
   * @param loginRequest        The login credentials
   * @param requiredRole        The role required for this login type
   * @param unauthorizedMessage Message to show when user doesn't have required role
   * @param successMessage      Message to show on successful login
   * @return A token if authentication is successful
   */
  private ResponseEntity<ApiResponse> processLogin(LoginRequest loginRequest, String requiredRole,
      String unauthorizedMessage, String successMessage) {
    String username = loginRequest.getUsername();

    // Check if a user is locked out
    if (isUserLockedOut(username)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(new ApiResponse(false, "Account temporarily locked due to too many failed attempts",
              null));
    }

    try {
      // Use AuthenticationManager to authenticate the user
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
      );

      // Check if a user has a required role
      boolean hasRequiredRole = authentication.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals(requiredRole));

      if (!hasRequiredRole) {
        recordFailedLoginAttempt(username);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ApiResponse(false, unauthorizedMessage, null));
      }

      // Reset failed login attempts on successful login
      resetFailedLoginAttempts(username);

      Map<String, Object> data = generateTokenResponse(authentication);

      return ResponseEntity.ok(new ApiResponse(true, successMessage, data));
    } catch (BadCredentialsException e) {
      recordFailedLoginAttempt(username);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse(false, "Invalid credentials", null));
    } catch (AuthenticationException e) {
      recordFailedLoginAttempt(username);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse(false, "Authentication failed: " + e.getMessage(), null));
    }
  }

  /**
   * Profile viewing endpoint for users.
   * @return The user profile
   */
  @GetMapping("/user/profile")
  @PreAuthorize("hasRole('PREMIUM')")
  public ResponseEntity<ApiResponse> getUserProfile(Authentication authentication) {
    return getProfile(authentication,
        premiumUserService::getPremiumUserByUsername,
        "Error retrieving user profile",
        "User profile not found");
  }

  /**
   * Profile viewing endpoint for admins
   * @return The admin profile
   */
  @GetMapping("/admin/profile")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse> getAdminProfile(Authentication authentication) {
    return getProfile(authentication,
        adminService::getAdminByUsername,
        "Error retrieving admin profile",
        "Admin profile not found");
  }

  /**
   * Common profile retrieval logic
   *
   * @param authentication       The authentication object
   * @param profileRetriever     Function to retrieve the profile by username
   * @param errorLogMessage      Message to log on error
   * @param errorResponseMessage Message to return on error
   * @return The profile response
   */
  private <T> ResponseEntity<ApiResponse> getProfile(Authentication authentication,
      Function<String, T> profileRetriever,
      String errorLogMessage,
      String errorResponseMessage) {
    try {
      String username = authentication.getName();
      T profile = profileRetriever.apply(username);
      return ResponseEntity.ok(new ApiResponse(true, "Profile retrieved successfully", profile));
    } catch (Exception e) {
      logger.error(errorLogMessage, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse(false, errorResponseMessage, null));
    }
  }

  /**
   * Registration endpoint for users
   * @param premiumUserDTO The user data
   * @return The created user
   */
  @PostMapping("/user/register")
  public ResponseEntity<ApiResponse> registerUser(
      @Valid @RequestBody PremiumUserDTO premiumUserDTO) {
    try {
      // Set the role to PREMIUM and use the raw password
      PremiumUserDTO userWithRole = new PremiumUserDTO(
          null, // id will be generated
          premiumUserDTO.getFirstName(),
          premiumUserDTO.getLastName(),
          premiumUserDTO.getEmail(),
          premiumUserDTO.getMobile(),
          premiumUserDTO.getDiveCertification(),
          premiumUserDTO.getUsername(),
          premiumUserDTO.getPassword(), // Use raw password
          "PREMIUM", // Set role to PREMIUM
          null, // createdAt will be set by the database
          null  // updatedAt will be set by the database
      );

      PremiumUserDTO createdUser = premiumUserService.createPremiumUser(userWithRole);

      // Create an authentication token for the new user
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          createdUser.getUsername(), null,
          Collections.singletonList(new SimpleGrantedAuthority("ROLE_PREMIUM"))
      );

      // Generate tokens
      String accessToken = tokenService.generateToken(authentication);
      String refreshToken = tokenService.generateRefreshToken(createdUser.getUsername());

      Map<String, Object> data = new HashMap<>();
      data.put("user", createdUser);
      data.put("accessToken", accessToken);
      data.put("refreshToken", refreshToken);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new ApiResponse(true, "User registered successfully", data));
    } catch (DuplicateResourceException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse(false, e.getMessage(), null));
    } catch (Exception e) {
      logger.error("Error registering user", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse(false, "Error registering: " + e.getMessage(), null));
    }
  }

  /**
   * Registration endpoint for admins.
   * @param adminDTO The admin data
   * @return The created admin
   */
  @PostMapping("/admin/register")
  @PreAuthorize("hasRole('ADMIN')") // Only existing admins can create new admins
  public ResponseEntity<ApiResponse> registerAdmin(@Valid @RequestBody AdminDTO adminDTO) {
    try {
      // Encode the password before saving
      String encodedPassword = passwordEncoder.encode(adminDTO.getPassword());

      // Set the role to ADMIN and use the encoded password
      AdminDTO adminWithRole = new AdminDTO(
          null, // id will be generated
          adminDTO.getFirstName(),
          adminDTO.getLastName(),
          adminDTO.getEmail(),
          adminDTO.getMobile(),
          adminDTO.getUsername(),
          encodedPassword, // Use encoded password
          "ADMIN", // Set role to ADMIN
          adminDTO.getRoleLimitation(),
          null, // createdAt will be set by the database
          null  // updatedAt will be set by the database
      );

      AdminDTO createdAdmin = adminService.createAdmin(adminWithRole);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new ApiResponse(true, "Admin registered successfully", createdAdmin));
    } catch (DuplicateResourceException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse(false, e.getMessage(), null));
    } catch (Exception e) {
      logger.error("Error registering admin", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse(false, "Error registering admin: " + e.getMessage(), null));
    }
  }

  // --- Helper methods for rate limiting ---

  /**
   * Record a failed login attempt for a user
   */
  private void recordFailedLoginAttempt(String username) {
    if (username == null || username.isEmpty()) {
      return; // Ignore attempts with no username
    }

    int attempts = loginAttempts.getOrDefault(username, 0) + 1;
    loginAttempts.put(username, attempts);

    // If max attempts reached, set lockout time
    if (attempts >= MAX_LOGIN_ATTEMPTS) {
      lockoutTimes.put(username, Instant.now().plus(LOCKOUT_DURATION));
      logger.warn("User account locked out due to too many failed attempts: {}", username);
    }
  }

  /**
   * Reset failed login attempts for a user
   */
  private void resetFailedLoginAttempts(String username) {
    if (username != null && !username.isEmpty()) {
      loginAttempts.remove(username);
      lockoutTimes.remove(username);
    }
  }

  /**
   * Check if a user is currently locked out
   */
  private boolean isUserLockedOut(String username) {
    if (username == null || username.isEmpty() || !lockoutTimes.containsKey(username)) {
      return false;
    }

    Instant lockoutEndTime = lockoutTimes.get(username);
    if (Instant.now().isAfter(lockoutEndTime)) {
      // The lockout period has expired
      lockoutTimes.remove(username);
      return false;
    }

    return true;
  }

  /**
   * Periodically clean up expired lockouts and stale login attempt records Runs every hour by
   * default
   */
  @Scheduled(fixedRateString = "${oceandive.security.lockout-cleanup-interval:3600000}")
  public void cleanupLockouts() {
    Instant now = Instant.now();
    int lockedAccountsBefore = lockoutTimes.size();
    int attemptRecordsBefore = loginAttempts.size();

    // Clean up expired lockouts
    lockoutTimes.entrySet().removeIf(entry -> entry.getValue().isBefore(now));

    // Clean up login attempts (older than 24 hours)
    // This helps prevent memory leaks from users who had 1-2 failed attempts but never got locked
    loginAttempts.entrySet().removeIf(entry -> {
      String username = entry.getKey();
      // Keep entries for currently locked accounts
      if (lockoutTimes.containsKey(username)) {
        return false;
      }

      return entry.getValue() < MAX_LOGIN_ATTEMPTS - 1;
    });

    int lockedAccountsRemoved = lockedAccountsBefore - lockoutTimes.size();
    int attemptRecordsRemoved = attemptRecordsBefore - loginAttempts.size();

    if (lockedAccountsRemoved > 0 || attemptRecordsRemoved > 0) {
      logger.info("Security cleanup: removed {} expired lockouts and {} stale attempt records",
          lockedAccountsRemoved, attemptRecordsRemoved);
    }
  }

  /**
   * Helper to generate token response map for access and refresh tokens.
   */
  private Map<String, Object> generateTokenResponse(Authentication authentication) {
    String accessToken = tokenService.generateToken(authentication);
    String refreshToken = tokenService.generateRefreshToken(authentication.getName());
    Map<String, Object> data = new HashMap<>();
    data.put("accessToken", accessToken);
    data.put("refreshToken", refreshToken);
    data.put("expiresIn", tokenService.getTokenRemainingValiditySeconds(accessToken));
    return data;
  }
}
package ch.oceandive.utils;

import ch.oceandive.exceptionHandler.DuplicateResourceException;
import ch.oceandive.repository.AdminRepo;
import ch.oceandive.repository.PremiumUserRepo;
import java.util.function.Consumer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserValidationUtil {

  private final PremiumUserRepo premiumUserRepo;
  private final AdminRepo adminRepo;

  public UserValidationUtil(PremiumUserRepo premiumUserRepo, AdminRepo adminRepo) {
    this.premiumUserRepo = premiumUserRepo;
    this.adminRepo = adminRepo;
  }
 // Validates whether the given username is unique across both Admin and PremiumUser.
  public void validateUniqueUsername(String username) {
    if (premiumUserRepo.findByUsername(username) != null || adminRepo.findByUsername(username) != null) {
      throw new DuplicateResourceException("Username already exists: " + username);
    }
  }

  // Validates whether the given email is unique across both Admin and PremiumUser.
  public void validateUniqueEmail(String email) {
    if (premiumUserRepo.findByEmail(email) != null || adminRepo.findByEmail(email) != null) {
      throw new DuplicateResourceException("Email already exists: " + email);
    }
  }
  // Update fields if the new values are not null and are different from the old values.
  public static <T> void updateIfNotNull(T newValue, T oldValue, Consumer<T> updater) {
    if (newValue != null && !newValue.equals(oldValue)) {
      updater.accept(newValue);
    }
  }
  // Updates a password field if it is provided (not null and not empty).
  public static void updatePasswordIfNotEmpty(String newPassword, Consumer<String> updater, PasswordEncoder encoder) {
    if (newPassword != null && !newPassword.isEmpty()) {
      updater.accept(encoder.encode(newPassword));
    }
  }

}
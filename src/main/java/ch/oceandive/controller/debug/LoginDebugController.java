package ch.oceandive.controller.debug;

import ch.oceandive.model.PremiumUser;
import ch.oceandive.model.Admin;
import ch.oceandive.repository.PremiumUserRepo;
import ch.oceandive.repository.AdminRepo;
import ch.oceandive.service.PremiumUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Debug controller. This controller is only active in the development profile.
 */
@RestController
@RequestMapping("/api/debug")
@Profile("dev") // Only active in development
public class LoginDebugController {

  private static final Logger logger = LoggerFactory.getLogger(LoginDebugController.class);


  private final PremiumUserRepo premiumUserRepo;
  private final AdminRepo adminRepo;
  private final PasswordEncoder passwordEncoder;

  public LoginDebugController(PremiumUserRepo premiumUserRepo, AdminRepo adminRepo,
      PasswordEncoder passwordEncoder) {
    this.premiumUserRepo = premiumUserRepo;
    this.adminRepo = adminRepo;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/check-user/{username}")
  public Map<String, Object> checkUser(@PathVariable String username) {
    Map<String, Object> result = new HashMap<>();

    try {
      // Check Premium User
      PremiumUser premiumUser = premiumUserRepo.findByUsername(username);
      if (premiumUser != null) {
        result.put("userFound", true);
        result.put("userType", "PREMIUM");
        result.put("username", premiumUser.getUsername());
        result.put("email", premiumUser.getEmail());
        result.put("role", premiumUser.getRole());
        result.put("passwordExists", premiumUser.getPassword() != null);
        result.put("passwordLength",
            premiumUser.getPassword() != null ? premiumUser.getPassword().length() : 0);
        result.put("passwordHash", premiumUser.getPassword() != null
            ?
            premiumUser.getPassword().substring(0, Math.min(10, premiumUser.getPassword().length()))
                + "..."
            : "null");
        result.put("isValidBcrypt",
            premiumUser.getPassword() != null && premiumUser.getPassword().startsWith("$2"));

        String rawTestPassword = "Password-1"; // Replace with the known valid raw password for testing

        // Debug the password verification process
        boolean isValidPassword = premiumUser.getPassword() != null &&
            passwordEncoder.matches(rawTestPassword, premiumUser.getPassword());
        result.put("isValidPassword", isValidPassword);

        // Return response immediately since PremiumUser is found
        return result;
      }
    } catch (Exception e) {
      logger.error("Error checking PremiumUser repository", e);
      result.put("premiumUserError", e.getMessage());
    }

    try {
      // Check Admin
      Admin admin = adminRepo.findByUsername(username);
      if (admin != null) {
        result.put("userFound", true);
        result.put("userType", "ADMIN");
        result.put("username", admin.getUsername());
        result.put("email", admin.getEmail());
        result.put("role", admin.getRole());
        result.put("passwordExists", admin.getPassword() != null);
        result.put("passwordLength",
            admin.getPassword() != null ? admin.getPassword().length() : 0);
        result.put("passwordHash", admin.getPassword() != null
            ? admin.getPassword().substring(0, Math.min(10, admin.getPassword().length())) + "..."
            : "null");
        result.put("isValidBcrypt",
            admin.getPassword() != null && admin.getPassword().startsWith("$2"));

        // Return response immediately since Admin is found
        return result;
      }
    } catch (Exception e) {
      logger.error("Error checking Admin repository", e);
      result.put("adminError", e.getMessage());
    }

    // If neither PremiumUser nor Admin is found, return a not found response
    result.put("userFound", false);
    result.put("message", "User not found in database");
    return result;
  }
}
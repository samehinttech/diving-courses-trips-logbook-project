package ch.oceandive.controller.debug;

import ch.oceandive.model.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug/auth")
public class AuthDebugController {

  private static final Logger logger = LoggerFactory.getLogger(AuthDebugController.class);

  @Autowired
  private UserDetailsServiceImpl userDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private AuthenticationManager authenticationManager;

  @PostMapping("/test-login")
  public Map<String, Object> testLogin(@RequestBody Map<String, String> credentials) {
    Map<String, Object> result = new HashMap<>();
    String username = credentials.get("username");
    String password = credentials.get("password");

    logger.info("Testing login for username: {}", username);

    try {
      // Step 1: Load user details
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      result.put("step1_userFound", true);
      result.put("step1_username", userDetails.getUsername());
      result.put("step1_authorities", userDetails.getAuthorities().toString());
      result.put("step1_passwordLength", userDetails.getPassword() != null ? userDetails.getPassword().length() : 0);

      // Step 2: Test password encoding
      boolean passwordMatches = passwordEncoder.matches(password, userDetails.getPassword());
      result.put("step2_passwordMatches", passwordMatches);
      result.put("step2_storedPasswordPrefix", userDetails.getPassword() != null ? userDetails.getPassword().substring(0, 7) : "null");

      // Step 3: Test authentication manager
      try {
        Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authResult = authenticationManager.authenticate(authRequest);
        result.put("step3_authenticationSuccess", true);
        result.put("step3_authenticated", authResult.isAuthenticated());
        result.put("step3_principal", authResult.getPrincipal().toString());
        result.put("step3_authorities", authResult.getAuthorities().toString());
      } catch (Exception authEx) {
        result.put("step3_authenticationSuccess", false);
        result.put("step3_authError", authEx.getMessage());
        result.put("step3_authErrorType", authEx.getClass().getSimpleName());
      }

    } catch (Exception e) {
      result.put("step1_userFound", false);
      result.put("error", e.getMessage());
      result.put("errorType", e.getClass().getSimpleName());
      logger.error("Error in test login", e);
    }

    return result;
  }

  @GetMapping("/test-password-encoding")
  public Map<String, Object> testPasswordEncoding(@RequestParam String password) {
    Map<String, Object> result = new HashMap<>();

    // Encode the password multiple times to see if it's consistent
    String encoded1 = passwordEncoder.encode(password);
    String encoded2 = passwordEncoder.encode(password);

    result.put("password", password);
    result.put("encoded1", encoded1);
    result.put("encoded2", encoded2);
    result.put("encoded1MatchesPassword", passwordEncoder.matches(password, encoded1));
    result.put("encoded2MatchesPassword", passwordEncoder.matches(password, encoded2));
    result.put("encoderClass", passwordEncoder.getClass().getSimpleName());

    return result;
  }
}
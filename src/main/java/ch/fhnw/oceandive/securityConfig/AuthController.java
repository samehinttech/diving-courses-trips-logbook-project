package ch.fhnw.oceandive.securityConfig;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final TokenService tokenService;
  private final AuthenticationManager authenticationManager;

  public AuthController(TokenService tokenService, AuthenticationManager authenticationManager) {
    this.tokenService = tokenService;
    this.authenticationManager = authenticationManager;
  }

  /**
   * Authenticate the user and generate a JWT token.
   * {@code LOGIN}
   */

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody CustomUserDetails customUserDetails) {
    try {
      // Validate input
      if (customUserDetails == null || customUserDetails.getUsername() == null || customUserDetails.getPassword() == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username or password cannot be null.");
      }
      // Create an authentication token with username and password
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken(
              customUserDetails.getUsername(), customUserDetails.getPassword());
      // Authenticate the user
      Authentication authentication = authenticationManager.authenticate(authenticationToken);
      // Set the authentication in the SecurityContext
      SecurityContextHolder.getContext().setAuthentication(authentication);
      // Generate JWT token
      String token = tokenService.generateToken(authentication);
      // Create a response with token and user details
      Map<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put("username", authentication.getName());
      response.put("roles", authentication.getAuthorities());
      return ResponseEntity.ok(response);
    } catch (BadCredentialsException e) {
      // Detailed response for invalid credentials
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
    } catch (AuthenticationException e) {
      // General authentication failure handling
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authentication failed", "details", e.getMessage()));
    } catch (Exception e) {
      // Handle unexpected exceptions
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "An internal server error occurred", "details", e.getMessage()));
    }
  }

  /**
   * Retrieve information about the currently authenticated user.
   */
  @GetMapping("/user")
  public ResponseEntity<?> getUser(Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
    }

    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("username", authentication.getName());
    userInfo.put("authorities", authentication.getAuthorities());

    return ResponseEntity.ok(userInfo);
  }

  /**
   * {@code LOGOUT} the user by clearing the security context and invalidating the token.
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
    try {
      // Clear the security context
      SecurityContextHolder.clearContext();
      // Validate and invalidate the provided token, if available
      Map<String, Object> response = new HashMap<>();
      response.put("loggedOut", true);
      if (token != null && !token.isEmpty()) {
        boolean invalidated = tokenService.invalidateToken(token);
        response.put("tokenInvalidated", invalidated);
      }
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "An error occurred during logout", "details", e.getMessage()));
    }
  }

  /**
   * Refresh the JWT token using the provided refresh token.
   */
  @GetMapping("/refresh")
  public ResponseEntity<?> refresh(@RequestHeader("Authorization") String token) {
    try {
      String newToken = tokenService.refreshToken(token);
      Map<String, String> response = new HashMap<>();
      response.put("token", newToken);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token refresh failed: " + e.getMessage());
    }
  }
}

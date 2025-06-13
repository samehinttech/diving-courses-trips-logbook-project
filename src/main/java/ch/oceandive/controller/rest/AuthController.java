package ch.oceandive.controller.rest;

import ch.oceandive.dto.AdminDTO;
import ch.oceandive.dto.Response;
import ch.oceandive.dto.LoginRequest;
import ch.oceandive.dto.PremiumUserDTO;
import ch.oceandive.dto.RegistrationRequest;
import ch.oceandive.exceptionHandler.DuplicateResourceException;
import ch.oceandive.model.UserDetailsServiceImpl;
import ch.oceandive.security.TokenService;
import ch.oceandive.service.AdminService;
import ch.oceandive.service.PremiumUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/*
  * REST controller for authentication operations including login, registration, tokens
  * and logout.
  * Provides endpoints for both user and admin roles
 */

@RestController
@RequestMapping("/api/auth") // Base path for authentication operations
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
  private final AuthenticationManager authenticationManager;

  public AuthController(
      TokenService tokenService,
      UserDetailsServiceImpl userDetailsService,
      PremiumUserService premiumUserService,
      AdminService adminService,
      AuthenticationManager authenticationManager) {
    this.tokenService = tokenService;
    this.userDetailsService = userDetailsService;
    this.premiumUserService = premiumUserService;
    this.adminService = adminService;
    this.authenticationManager = authenticationManager;
  }

  // ====================== Authentication operations ======================
  //Generate access token for authenticated user
  @PostMapping("/token")
  public ResponseEntity<Response> token(Authentication authentication) {
    if (authentication.isAuthenticated()) {
      Map<String, Object> data = generateTokenResponse(authentication);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new Response(true, "Authentication successful", data));
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new Response(false, "Authentication failed", null));
    }
  }
// Refresh the access token
  @PostMapping("/refresh")
  public ResponseEntity<Response> refresh(@Parameter(description = "Refresh token") @RequestParam String refreshToken) {
    // Validate the refresh token
    if (!tokenService.validateToken(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new Response(false, "Try to login once again", null));
    }
    try {
      String username = tokenService.getUsernameFromToken(refreshToken);
      if (username == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new Response(false, "Invalid user information", null));
      }
      // Check if the user still exists
      try {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // User exists, generate a new token with their authorities
        Authentication auth = new UsernamePasswordAuthenticationToken(
            username, null, userDetails.getAuthorities());
        Map<String, Object> data = generateTokenResponse(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new Response(true, "You are logged in  successfully", data));
      } catch (UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new Response(false, "User no longer exists", null));
      }
    } catch (Exception e) {
      logger.error("Error processing refresh token", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new Response(false, "Invalid credentials", null));
    }
  }
  // Logout user and blocklist token (15-minute session duration)
  @PostMapping("/logout")
  public ResponseEntity<Response> logout(@RequestHeader("Authorization") String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      tokenService.blacklistToken(token);
      return ResponseEntity.ok(new Response(true, "Logged out successfully", null));
    }
    return ResponseEntity.badRequest()
        .body(new Response(false, "Invalid credentials", null));
  }
  // Login endpoints for different user roles
  @PostMapping("/user/login")
  public ResponseEntity<Response> userLogin(@Valid @RequestBody LoginRequest loginRequest) {
    return processLogin(loginRequest, "ROLE_PREMIUM", "You don' have access to this resource",
        "User are logged in successfully");
  }
  @PostMapping("/admin/login")
  public ResponseEntity<Response> adminLogin(@Valid @RequestBody LoginRequest loginRequest) {
    return processLogin(loginRequest, "ROLE_ADMIN", "User is not an admin",
        "Admin logged in successfully");
  }

  //Helper method to process login for different user types
  private ResponseEntity<Response> processLogin(LoginRequest loginRequest, String requiredRole,
      String unauthorizedMessage, String successMessage) {
    String username = loginRequest.getUsername();
    // Check if a user is locked out
    if (isUserLockedOut(username)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(new Response(false, "Account temporarily locked due to too many failed attempts",
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
            .body(new Response(false, unauthorizedMessage, null));
      }
      // Reset failed login attempts on successful login
      resetFailedLoginAttempts(username);
      Map<String, Object> data = generateTokenResponse(authentication);
      return ResponseEntity.ok(new Response(true, successMessage, data));
    } catch (BadCredentialsException e) {
      recordFailedLoginAttempt(username);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new Response(false, "Invalid credentials", null));
    } catch (AuthenticationException e) {
      recordFailedLoginAttempt(username);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new Response(false, "Authentication failed: " + e.getMessage(), null));
    }
  }

  // Profile retrieval endpoints for different user roles
  @GetMapping("/user/my-profile")
  @PreAuthorize("hasRole('PREMIUM')")
  public ResponseEntity<Response> getUserProfile(Authentication authentication) {
    return getProfile(authentication,
        premiumUserService::getPremiumUserByUsername,
        "Error retrieving user profile",
        "User profile not found");
  }

  @GetMapping("/admin/my-profile")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Response> getAdminProfile(Authentication authentication) {
    return getProfile(authentication,
        adminService::getAdminByUsername,
        "Error retrieving admin profile",
        "Admin profile not found");
  }

  // Helper method to retrieve user or admin profile
  private <T> ResponseEntity<Response> getProfile(Authentication authentication,
      Function<String, T> profileRetriever,
      String errorLogMessage,
      String errorResponseMessage) {
    try {
      String username = authentication.getName();
      T profile = profileRetriever.apply(username);
      return ResponseEntity.ok(new Response(true, "Profile retrieved successfully", profile));
    } catch (Exception e) {
      logger.error(errorLogMessage, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new Response(false, errorResponseMessage, null));
    }
  }

  // Registration endpoints for user and admin roles (However, Admin logic is not implemented yet)
  @PostMapping("/user/register")
  public ResponseEntity<Response> registerUser(
      @Valid @RequestBody RegistrationRequest registrationRequest) {
    try {
      // Set the role to PREMIUM and use the raw password
      PremiumUserDTO userWithRole = new PremiumUserDTO(
          null, // id will be generated
          registrationRequest.getFirstName(),
          registrationRequest.getLastName(),
          registrationRequest.getEmail(),
          registrationRequest.getMobile(),
          registrationRequest.getDiveCertification(),
          registrationRequest.getUsername(),
          registrationRequest.getPassword(), // Use raw password
          "ROLE_PREMIUM", // Set role to PREMIUM
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
          .body(new Response(true, "User registered successfully", data));
    } catch (DuplicateResourceException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new Response(false, e.getMessage(), null));
    } catch (Exception e) {
      logger.error("Error registering user", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new Response(false, "Error registering: " + e.getMessage(), null));
    }
  }

  @PostMapping("/admin/register")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Response> registerAdmin(
      @Valid @RequestBody RegistrationRequest registrationRequest) {
    try {
      // Set the role to ADMIN and use the encoded password
      AdminDTO adminWithRole = new AdminDTO(
          null, // id will be generated
          registrationRequest.getFirstName(),
          registrationRequest.getLastName(),
          registrationRequest.getEmail(),
          registrationRequest.getMobile(),
          registrationRequest.getUsername(),
          registrationRequest.getPassword(),
          "ROLE_ADMIN", // Set role to ADMIN
          registrationRequest.getRoleLimitation(),
          null, // createdAt will be set by the database
          null  // updatedAt will be set by the database
      );

      AdminDTO createdAdmin = adminService.createAdmin(adminWithRole);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new Response(true, "Admin registered successfully", createdAdmin));
    } catch (DuplicateResourceException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new Response(false, e.getMessage(), null));
    } catch (Exception e) {
      logger.error("Error registering admin", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new Response(false, "Error registering admin: " + e.getMessage(), null));
    }
  }

// =========== Helper methods for rate limiting ====================

  // Record a failed login attempt for a user
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
 // Reset failed login attempts for a user
  private void resetFailedLoginAttempts(String username) {
    if (username != null && !username.isEmpty()) {
      loginAttempts.remove(username);
      lockoutTimes.remove(username);
    }
  }
   //Check if a user is currently locked out
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

 // Periodically clean up expired lockouts and stale login attempt records Runs every hour by default
  @Scheduled(fixedRateString = "${oceandive.security.lockout-cleanup-interval:3600000}") // Default to 1 hour
  public void cleanupLockouts() {
    Instant now = Instant.now();
    int lockedAccountsBefore = lockoutTimes.size();
    int attemptRecordsBefore = loginAttempts.size();
    // Clean up expired lockouts
    lockoutTimes.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    // Clean up login attempts (older than 24 hours)
    // This helps to prevent memory leaks from users who had 1-2 failed attempts but never got locked
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

 // Helper to generate a token response map for access and refresh tokens.
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
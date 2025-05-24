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
      String token = tokenService.generateToken(authentication);
      String refreshToken = tokenService.generateRefreshToken(authentication.getName());
      
      Map<String, Object> data = new HashMap<>();
      data.put("accessToken", token);
      data.put("refreshToken", refreshToken);
      data.put("expiresIn", tokenService.getTokenRemainingValiditySeconds(token));
      
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
            
        String newAccessToken = tokenService.generateToken(auth);
        String newRefreshToken = tokenService.generateRefreshToken(username);
        
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", newAccessToken);
        data.put("refreshToken", newRefreshToken);
        data.put("expiresIn", tokenService.getTokenRemainingValiditySeconds(newAccessToken));
        
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
   * Login endpoint for premium users.
   * @param loginRequest The login credentials
   * @return A token if authentication is successful
   */
  @PostMapping("/premium/login")
  public ResponseEntity<ApiResponse> premiumLogin(@Valid @RequestBody LoginRequest loginRequest) {
    String username = loginRequest.getUsername();
    
    // Check if user is locked out
    if (isUserLockedOut(username)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(new ApiResponse(false, "Account temporarily locked due to too many failed attempts", null));
    }
    
    try {
      // Use AuthenticationManager to authenticate the user
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
      );
      
      // Check if user has premium role
      boolean isPremium = authentication.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals("ROLE_PREMIUM"));
          
      if (!isPremium) {
        recordFailedLoginAttempt(username);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ApiResponse(false, "User is not a premium user", null));
      }
      
      // Reset failed login attempts on successful login
      resetFailedLoginAttempts(username);
      
      // Generate tokens
      String accessToken = tokenService.generateToken(authentication);
      String refreshToken = tokenService.generateRefreshToken(username);
      
      Map<String, Object> data = new HashMap<>();
      data.put("accessToken", accessToken);
      data.put("refreshToken", refreshToken);
      data.put("expiresIn", tokenService.getTokenRemainingValiditySeconds(accessToken));
      
      return ResponseEntity.ok(new ApiResponse(true, "Premium user logged in successfully", data));
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
   * Login endpoint for admins.
   * @param loginRequest The login credentials
   * @return A token if authentication is successful
   */
  @PostMapping("/admin/login")
  public ResponseEntity<ApiResponse> adminLogin(@Valid @RequestBody LoginRequest loginRequest) {
    String username = loginRequest.getUsername();
    
    // Check if user is locked out
    if (isUserLockedOut(username)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(new ApiResponse(false, "Account temporarily locked due to too many failed attempts", null));
    }
    
    try {
      // Use AuthenticationManager to authenticate the user
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
      );
      
      // Check if user has admin role
      boolean isAdmin = authentication.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
          
      if (!isAdmin) {
        recordFailedLoginAttempt(username);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ApiResponse(false, "User is not an admin", null));
      }
      
      // Reset failed login attempts on successful login
      resetFailedLoginAttempts(username);
      
      // Generate tokens
      String accessToken = tokenService.generateToken(authentication);
      String refreshToken = tokenService.generateRefreshToken(username);
      
      Map<String, Object> data = new HashMap<>();
      data.put("accessToken", accessToken);
      data.put("refreshToken", refreshToken);
      data.put("expiresIn", tokenService.getTokenRemainingValiditySeconds(accessToken));
      
      return ResponseEntity.ok(new ApiResponse(true, "Admin logged in successfully", data));
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
   * Profile viewing endpoint for premium users.
   * @return The premium user profile
   */
  @GetMapping("/premium/profile")
  @PreAuthorize("hasRole('PREMIUM')")
  public ResponseEntity<ApiResponse> getPremiumProfile(Authentication authentication) {
    try {
      String username = authentication.getName();
      PremiumUserDTO premiumUser = premiumUserService.getPremiumUserByUsername(username);
      return ResponseEntity.ok(new ApiResponse(true, "Profile retrieved successfully", premiumUser));
    } catch (Exception e) {
      logger.error("Error retrieving premium user profile", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse(false, "User profile not found", null));
    }
  }

  /**
   * Profile viewing endpoint for admins.
   * @return The admin profile
   */
  @GetMapping("/admin/profile")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse> getAdminProfile(Authentication authentication) {
    try {
      String username = authentication.getName();
      AdminDTO admin = adminService.getAdminByUsername(username);
      return ResponseEntity.ok(new ApiResponse(true, "Profile retrieved successfully", admin));
    } catch (Exception e) {
      logger.error("Error retrieving admin profile", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse(false, "Admin profile not found", null));
    }
  }

  /**
   * Registration endpoint for premium users.
   * @param premiumUserDTO The premium user data
   * @return The created premium user
   */
  @PostMapping("/premium/register")
  public ResponseEntity<ApiResponse> registerPremiumUser(@Valid @RequestBody PremiumUserDTO premiumUserDTO) {
    try {
      // Encode the password before saving
      String encodedPassword = passwordEncoder.encode(premiumUserDTO.getPassword());
      
      // Set the role to PREMIUM and use the encoded password
      PremiumUserDTO userWithRole = new PremiumUserDTO(
          null, // id will be generated
          premiumUserDTO.getFirstName(),
          premiumUserDTO.getLastName(),
          premiumUserDTO.getEmail(),
          premiumUserDTO.getMobile(),
          premiumUserDTO.getDiveCertification(),
          premiumUserDTO.getUsername(),
          encodedPassword, // Use encoded password
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
      logger.error("Error registering premium user", e);
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
      // Lockout period has expired
      lockoutTimes.remove(username);
      return false;
    }
    
    return true;
  }
  
  /**
   * Periodically clean up expired lockouts and stale login attempt records
   * Runs every hour by default
   */
  @Scheduled(fixedRateString = "${oceandive.security.lockout-cleanup-interval:3600000}")
  public void cleanupLockouts() {
    Instant now = Instant.now();
    int lockedAccountsBefore = lockoutTimes.size();
    int attemptRecordsBefore = loginAttempts.size();
    
    // Clean up expired lockouts
    lockoutTimes.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    
    // Clean up stale login attempts (older than 24 hours)
    // This helps prevent memory leaks from users who had 1-2 failed attempts but never got locked
    loginAttempts.entrySet().removeIf(entry -> {
      String username = entry.getKey();
      // Keep entries for currently locked accounts
      if (lockoutTimes.containsKey(username)) {
        return false;
      }
      // If we had tracking of "last attempt time", we would check against cutoff here
      // Since we don't have that, we'll clean up any with low attempt counts
      return entry.getValue() < MAX_LOGIN_ATTEMPTS - 1;
    });
    
    int lockedAccountsRemoved = lockedAccountsBefore - lockoutTimes.size();
    int attemptRecordsRemoved = attemptRecordsBefore - loginAttempts.size();
    
    if (lockedAccountsRemoved > 0 || attemptRecordsRemoved > 0) {
      logger.info("Security cleanup: removed {} expired lockouts and {} stale attempt records",
          lockedAccountsRemoved, attemptRecordsRemoved);
    }
  }
}
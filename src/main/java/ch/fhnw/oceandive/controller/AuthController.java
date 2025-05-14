package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.dto.PremiumUserDTO;
import ch.fhnw.oceandive.dto.AdminDTO;
import ch.fhnw.oceandive.exceptionHandler.DuplicateResourceException;
import ch.fhnw.oceandive.model.UserDetailsServiceImpl;
import ch.fhnw.oceandive.security.TokenService;
import ch.fhnw.oceandive.service.AdminService;
import ch.fhnw.oceandive.service.PremiumUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final TokenService tokenService;
  private final UserDetailsServiceImpl userDetailsService;
  private final PremiumUserService premiumUserService;
  private final AdminService adminService;

  @Autowired
  public AuthController(TokenService tokenService, UserDetailsServiceImpl userDetailsService,
                       PremiumUserService premiumUserService, AdminService adminService) {
    this.tokenService = tokenService;
    this.userDetailsService = userDetailsService;
    this.premiumUserService = premiumUserService;
    this.adminService = adminService;
  }

  @PostMapping("/token")
  public ResponseEntity<String> token(Authentication authentication) {
    if (authentication.isAuthenticated()) {
      String token = tokenService.generateToken(authentication);
      return ResponseEntity.status(201).body(token);
    } else {
      throw new UsernameNotFoundException("Invalid user request!");
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<String> refresh(@RequestParam String refreshToken) {
    // Validate the refresh token
    if (!tokenService.validateToken(refreshToken)) {
      throw new IllegalArgumentException("Invalid or blacklisted refresh token");
    }

    JwtDecoder jwtDecoder = tokenService.getJwtDecoder();
    try {
      var jwt = jwtDecoder.decode(refreshToken);
      String username = jwt.getSubject();

      // Check if the user still exists
      try {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // User exists, generate a new token with their authorities
        String token = tokenService.generateToken(
            new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities()));
        return ResponseEntity.status(201).body(token);
      } catch (UsernameNotFoundException e) {
        // User no longer exists
        throw new IllegalArgumentException("User no longer exists");
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid refresh token");
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      tokenService.blacklistToken(token);
      return ResponseEntity.ok("Logged out successfully");
    }
    return ResponseEntity.badRequest().body("Invalid token");
  }

  /**
   * Login endpoint for premium users.
   * @return A token if authentication is successful
   */
  @PostMapping("/premium/login")
  public ResponseEntity<?> premiumLogin(@RequestParam String username, @RequestParam String password) {
    try {
      // Load user details
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      // Check if the password matches
      if (!userDetails.getPassword().equals(password)) {
        return ResponseEntity.status(401).body("Invalid credentials");
      }

      // Check if a user has a premium role
      boolean isPremium = userDetails.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals("ROLE_PREMIUM"));

      if (!isPremium) {
        return ResponseEntity.status(403).body("User is not a premium user");
      }

      // Create an authentication token
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          username, null, userDetails.getAuthorities());

      // Generate token
      String token = tokenService.generateToken(authentication);

      Map<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put("message", "Premium user logged in successfully");

      return ResponseEntity.ok(response);
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.status(401).body("Invalid credentials");
    }
  }

  /**
   * Login endpoint for admins.
   * @return A token if authentication is successful
   */
  @PostMapping("/admin/login")
  public ResponseEntity<?> adminLogin(@RequestParam String username, @RequestParam String password) {
    try {
      // Load user details
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      // Check if the password matches
      if (!userDetails.getPassword().equals(password)) {
        return ResponseEntity.status(401).body("Invalid credentials");
      }

      // Check if a user has an admin role
      boolean isAdmin = userDetails.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

      if (!isAdmin) {
        return ResponseEntity.status(403).body("User is not an admin");
      }

      // Create an authentication token
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          username, null, userDetails.getAuthorities());

      // Generate token
      String token = tokenService.generateToken(authentication);

      Map<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put("message", "Admin logged in successfully");

      return ResponseEntity.ok(response);
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.status(401).body("Invalid credentials");
    }
  }

  /**
   * Profile viewing endpoint for premium users.
   * @return The premium user profile
   */
  @GetMapping("/premium/profile")
  @PreAuthorize("hasRole('PREMIUM')")
  public ResponseEntity<?> getPremiumProfile(Authentication authentication) {
    try {
      String username = authentication.getName();
      PremiumUserDTO premiumUser = premiumUserService.getPremiumUserByUsername(username);
      return ResponseEntity.ok(premiumUser);
    } catch (Exception e) {
      return ResponseEntity.status(404).body("User profile not found");
    }
  }

  /**
   * Profile viewing endpoint for admins.
   * @return The admin profile
   */
  @GetMapping("/admin/profile")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> getAdminProfile(Authentication authentication) {
    try {
      String username = authentication.getName();
      AdminDTO admin = adminService.getAdminByUsername(username);
      return ResponseEntity.ok(admin);
    } catch (Exception e) {
      return ResponseEntity.status(404).body("Admin profile not found");
    }
  }

  /**
   * Registration endpoint for premium users.
   * @return The created premium user
   */
  @PostMapping("/premium/register")
  public ResponseEntity<?> registerPremiumUser(@Valid @RequestBody PremiumUserDTO premiumUserDTO) {
    try {
      // Set the role to PREMIUM
      PremiumUserDTO userWithRole = new PremiumUserDTO(
          null, // id will be generated
          premiumUserDTO.getFirstName(),
          premiumUserDTO.getLastName(),
          premiumUserDTO.getEmail(),
          premiumUserDTO.getMobile(),
          premiumUserDTO.getDiveCertification(),
          premiumUserDTO.getUsername(),
          premiumUserDTO.getPassword(),
          "PREMIUM", // Set role to PREMIUM
          null, // createdAt will be set by the database
          null  // updatedAt will be set by the database
      );

      PremiumUserDTO createdUser = premiumUserService.createPremiumUser(userWithRole);

      // Create an authentication token for the new user
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          createdUser.getUsername(), null, java.util.Collections.singletonList(
              new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PREMIUM")
          )
      );

      // Generate token
      String token = tokenService.generateToken(authentication);

      Map<String, Object> response = new HashMap<>();
      response.put("user", createdUser);
      response.put("token", token);
      response.put("message", "User registered successfully");
      return ResponseEntity.status(201).body(response);
    } catch (DuplicateResourceException e) {
      return ResponseEntity.status(409).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error registering: " + e.getMessage());
    }
  }

  /**
   * Registration endpoint for admins.
   * @return The created admin
   */
  @PostMapping("/admin/register")
  @PreAuthorize("hasRole('ADMIN')") // Only existing admins can create new admins
  public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminDTO adminDTO) {
    try {
      // Set the role to ADMIN
      AdminDTO adminWithRole = new AdminDTO(
          null, // id will be generated
          adminDTO.getFirstName(),
          adminDTO.getLastName(),
          adminDTO.getEmail(),
          adminDTO.getMobile(),
          adminDTO.getUsername(),
          adminDTO.getPassword(),
          "ADMIN", // Set role to ADMIN
          adminDTO.getRoleLimitation(),
          null, // createdAt will be set by the database
          null  // updatedAt will be set by the database
      );
      AdminDTO createdAdmin = adminService.createAdmin(adminWithRole);
      return ResponseEntity.status(201).body(createdAdmin);
    } catch (DuplicateResourceException e) {
      return ResponseEntity.status(409).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error registering admin: " + e.getMessage());
    }
  }
}

package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.dto.UserDTO;
import ch.fhnw.oceandive.exceptionHandler.DuplicateResourceException;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.model.Role;
import ch.fhnw.oceandive.model.Role.RoleName;
import ch.fhnw.oceandive.model.UserEntity;
import ch.fhnw.oceandive.repository.RoleRepository;
import ch.fhnw.oceandive.repository.UserRepository;
import ch.fhnw.oceandive.securityConfig.TokenService;
import java.time.LocalDateTime;
import java.util.Map;
import javax.management.relation.RoleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final BCryptPasswordEncoder passwordEncoder;



  @Autowired
  public UserService(UserRepository userRepository, RoleRepository roleRepository,
      BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Extract and validate credentials from the login request
   */
  public Map<String, String> extractAndValidateCredentials(Map<String, String> loginRequest) {
    String username = loginRequest.get("username");
    String password = loginRequest.get("password");
    if (username == null || password == null) {
      return null;
    }
    return Map.of("username", username, "password", password);
  }

  /**
   * Get all users (admin function)
   */
  public List<UserDTO> getAllUsers() {
    return userRepository.findAllUsers().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Get user by username (admin view)
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public UserDTO getUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .map(this::convertToDTO)
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with username: " + username));
  }

  /**
   * Get user by username (client view with limited information)
   */
  public UserDTO getUserByUsernamePublic(String username) {
    return userRepository.findByUsername(username)
        .map(this::convertToPublicDTO)
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with username: " + username));
  }

  /**
   * Get user by ID (admin view)
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public UserDTO getUserById(String id) {
    return userRepository.findById(id)
        .map(this::convertToDTO)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
  }

  /**
   * Get user by ID (client view with limited information)
   */
  public UserDTO getUserByIdPublic(String id) {
    return userRepository.findById(id)
        .map(this::convertToPublicDTO)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
  }

  /**
   * Get current authenticated user Returns admin view if user is admin, otherwise returns client
   * view
   */
  public UserDTO getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // Return the admin view if the user is an admin
    if (authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
      return userRepository.findByUsername(username)
          .map(this::convertToDTO)
          .orElseThrow(
              () -> new ResourceNotFoundException("User not found with username: " + username));
    }
    // Return public view for regular users
    return getUserByUsernamePublic(username);
  }

  /**
   * Register a new user
   */
  @Transactional
  public UserDTO registerUser(
      String username,
      String email,
      String password,
      String firstName,
      String lastName,
      String diveCertification
  ) throws RoleNotFoundException {
    // Validate input fields
    validateUsername(username);
    validateEmail(email);
    validatePassword(password);
    // Check if a username or email already exists
    if (userRepository.existsByUsername(username)) {
      throw new DuplicateResourceException("Username already exists: " + username);
    }
    if (userRepository.existsByEmail(email)) {
      throw new DuplicateResourceException("Email already exists: " + email);
    }

    // Create a new user entity
    UserEntity user = new UserEntity();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    user.setDiveCertification(DiveCertification.valueOf(diveCertification));
    user.setIssuedOn(LocalDateTime.now());

    // Assign default user role
    Role userRole = roleRepository.findByRoleName(Role.RoleName.ROLE_USER_ACCOUNT)
        .orElseThrow(() -> new RoleNotFoundException("Default user role not found"));
    user.addRole(userRole);

    // Save the user and convert to DTO
    UserEntity savedUser = userRepository.save(user);
    return convertToDTO(savedUser);
  }

  private void validateUsername(String username) {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }
    if (username.length() < 3 || username.length() > 20) {
      throw new IllegalArgumentException("Username must be between 3 and 20 characters long");
    }
  }

  private void validateEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }
    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) { // Basic regex for email validation
      throw new IllegalArgumentException("Invalid email format: " + email);
    }
  }

  private void validatePassword(String password) {
    if (password == null || password.length() < 8) {
      throw new IllegalArgumentException("Password must be at least 8 characters long");
    }
    if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(
        ".*\\d.*")) {
      throw new IllegalArgumentException(
          "Password must contain uppercase, lowercase, and numeric characters");
    }
  }

  /**
   * Update user - only updates allowed fields
   */
  @Transactional
  public UserDTO updateUser(String username, UserDTO userDTO) {
    // Get a current authenticated user
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    // Check if a user has permission to update this profile
    if (!currentUsername.equals(username) &&
        authentication.getAuthorities().stream()
            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
      throw new AccessDeniedException("You do not have permission to update this user");
    }

    // Find the user to update
    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with username: " + username));

    // Update fields
    user.setFirstName(userDTO.getFirstName());
    user.setLastName(userDTO.getLastName());
    user.setDiveCertification(userDTO.getDiveCertification());

    // Email update requires checking for uniqueness
    if (!user.getEmail().equals(userDTO.getEmail())) {
      if (userRepository.existsByEmail(userDTO.getEmail())) {
        throw new IllegalArgumentException("Email already exists");
      }
      user.setEmail(userDTO.getEmail());
    }

    // Save the updated user
    UserEntity updatedUser = userRepository.save(user);
    return convertToDTO(updatedUser);
  }

  /**
   * Change user password
   */
  @Transactional
  public void changePassword(String username, String oldPassword, String newPassword) {
    // Check current user authorization
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    if (!currentUsername.equals(username) &&
        authentication.getAuthorities().stream()
            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
      throw new AccessDeniedException("You do not have permission to change this user's password");
    }

    // Find user
    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with username: " + username));

    // Verify the old password if the user is not an admin
    if (currentUsername.equals(username) && !passwordEncoder.matches(oldPassword,
        user.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }

    // Update password
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  /**
   * Delete user (admin function)
   */
  @Transactional
  public void deleteUser(String username) {
    userRepository.deleteByUsername(username);
  }

  /**
   * Add a role to the user (admin function)
   */
  @Transactional
  public UserDTO addRoleToUser(String username, String roleName) {
    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with username: " + username));

    Role role = roleRepository.findByRoleName(Role.RoleName.valueOf(roleName))
        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

    user.addRole(role);
    UserEntity updatedUser = userRepository.save(user);
    return convertToDTO(updatedUser);
  }

  /**
   * Remove a role from the user (admin function)
   */
  @Transactional
  public UserDTO removeRoleFromUser(String username, String roleName) {
    UserEntity user = userRepository.findByUsername(username)
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with username: " + username));

    Role role = roleRepository.findByRoleName(Role.RoleName.valueOf(roleName))
        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

    user.removeRole(role);
    UserEntity updatedUser = userRepository.save(user);
    return convertToDTO(updatedUser);
  }

  /**
   * Convert UserEntity to UserDTO with full admin access
   */
  private UserDTO convertToDTO(UserEntity user) {
    // Convert roles to role names
    List<RoleName> roleNames = user.getRoles().stream()
        .map(Role::getRoleName)
        .toList();

    return new UserDTO(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getUsername(),
        null, // Null not to be exposed in DTOs
        user.getDiveCertification(),
        user.getIssuedOn(),
        user.getModifiedOn(),
        user.getUserType(),
        user.getBookingsCount()
    );
  }

  /**
   * Convert UserEntity to public UserDTO (client view with limited information)
   */
  private UserDTO convertToPublicDTO(UserEntity user) {
    return new UserDTO(
        user.getFirstName(),
        user.getLastName(),
        user.getUsername(),
        user.getDiveCertification()
    );
  }
}

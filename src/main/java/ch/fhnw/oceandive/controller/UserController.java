package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.dto.UserDTO;
import ch.fhnw.oceandive.securityConfig.AuthController;
import ch.fhnw.oceandive.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import javax.management.relation.RoleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "User Management", description = "API for managing users")
public class UserController {

    private final UserService userService;
    private AuthController authController;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;

    }

    /**
     * User registration endpoint (publicly accessible)
     */
    @PostMapping("/auth/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO)
        throws RoleNotFoundException {
        // Register the new user and retrieve the created UserDTO
        UserDTO registeredUser = userService.registerUser(
            userDTO.getUsername(),
            userDTO.getEmail(),
            userDTO.getPassword(),
            userDTO.getFirstName(),
            userDTO.getLastName(),
            userDTO.getDiveCertification() != null ? userDTO.getDiveCertification().name() : null
        );
        // Return the created user DTO with HTTP 201 status
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    /**
     * Get current user profile (authenticated users only)
     */
    @GetMapping("/user/profile")
    @Operation(
        summary = "Get current user profile", 
        description = "Retrieve the profile of the currently authenticated user",
        security = @SecurityRequirement(name = "jwt")
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        UserDTO user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /**
     * Update the current user profile (authenticated users only)
     */
    @PutMapping("/user/profile")
    @Operation(
        summary = "Update current user profile", 
        description = "Update the profile of the currently authenticated user",
        security = @SecurityRequirement(name = "jwt")
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(@Valid @RequestBody UserDTO userDTO) {
        String username = userService.getCurrentUser().getUsername();
        UserDTO updatedUser = userService.updateUser(username, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Change password for current user (authenticated users only)
     */
    @PostMapping("/user/change-password")
    @Operation(
        summary = "Change password", 
        description = "Change password for the current user",
        security = @SecurityRequirement(name = "jwt")
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@RequestBody Map<String, String> passwordMap) {
        String username = userService.getCurrentUser().getUsername();
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        userService.changePassword(username, oldPassword, newPassword);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Get all users (admin only)
     */
    @GetMapping("/admin/users")
    @Operation(
        summary = "Get all users", 
        description = "Retrieve a list of all registered users (admin only)",
        security = @SecurityRequirement(name = "jwt")
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by username (admin only)
     */
    @GetMapping("/admin/users/{username}")
    @Operation(
        summary = "Get user by username", 
        description = "Retrieve a specific user by username (admin only)",
        security = @SecurityRequirement(name = "jwt")
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user (admin only)
     */
    @PutMapping("/admin/users/{username}")
    @Operation(
        summary = "Update user", 
        description = "Update a specific user by username (admin only)",
        security = @SecurityRequirement(name = "jwt")
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String username, @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(username, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user (admin only)
     */
    @DeleteMapping("/admin/users/{username}")
    @Operation(
        summary = "Delete user", 
        description = "Delete a specific user by username (admin only)",
        security = @SecurityRequirement(name = "jwt")
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Add role to user (admin only)
     */
    @PostMapping("/admin/users/{username}/roles/{roleName}")
    @Operation(
        summary = "Add role to user", 
        description = "Add a role to a specific user (admin only)",
        security = @SecurityRequirement(name = "jwt")
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> addRoleToUser(@PathVariable String username, @PathVariable String roleName) {
        UserDTO updatedUser = userService.addRoleToUser(username, roleName);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Remove role from user (admin only)
     */
    @DeleteMapping("/admin/users/{username}/roles/{roleName}")
    @Operation(
        summary = "Remove role from user", 
        description = "Remove a role from a specific user (admin only)",
        security = @SecurityRequirement(name = "jwt")
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> removeRoleFromUser(@PathVariable String username, @PathVariable String roleName) {
        UserDTO updatedUser = userService.removeRoleFromUser(username, roleName);
        return ResponseEntity.ok(updatedUser);
    }
}

package ch.oceandive.service;

import ch.oceandive.dto.AdminDTO;
import ch.oceandive.exceptionHandler.DuplicateResourceException;
import ch.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.oceandive.model.Admin;
import ch.oceandive.repository.AdminRepo;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Admin entities.
 */
@Service
public class AdminService {

    private final PasswordEncoder passwordEncoder;
    private final AdminRepo adminRepo;

    @Autowired
    public AdminService(AdminRepo adminRepo, PasswordEncoder passwordEncoder) {
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all admins.
     *
     * @return List of AdminDTO objects
     */
    public List<AdminDTO> getAllAdmins() {
        return adminRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get an admin by ID.
     * 
     * @throws ResourceNotFoundException if the admin is not found
     */
    public AdminDTO getAdminById(Long id) {
        Admin admin = adminRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        return convertToDTO(admin);
    }

    /**
     * Get an admin by username.
     * 
     * @throws ResourceNotFoundException if the admin is not found
     */
    public AdminDTO getAdminByUsername(String username) {
        Admin admin = adminRepo.findByUsername(username);
        if (admin == null) {
            throw new ResourceNotFoundException("Admin not found with username: " + username);
        }
        return convertToDTO(admin);
    }

    /**
     * Get an admin by email.
     * 
     * @throws ResourceNotFoundException if the admin is not found
     */
    public AdminDTO getAdminByEmail(String email) {
        Admin admin = adminRepo.findByEmail(email);
        if (admin == null) {
            throw new ResourceNotFoundException("Admin not found with email: " + email);
        }
        return convertToDTO(admin);
    }

    /**
     * Create a new admin.
     * 
     * @throws DuplicateResourceException if the username or email already exists
     */
    @Transactional
    public AdminDTO createAdmin(AdminDTO adminDTO) {
              // Check if a username already exists
        if (adminRepo.findByUsername(adminDTO.getUsername()) != null) {
            throw new DuplicateResourceException("Username already exists: " + adminDTO.getUsername());
        }

        // Check if email already exists
        if (adminRepo.findByEmail(adminDTO.getEmail()) != null) {
            throw new DuplicateResourceException("Email already exists: " + adminDTO.getEmail());
        }

        Admin admin = convertToEntity(adminDTO);
        // Hash the password before saving
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        Admin savedAdmin = adminRepo.save(admin);
        return convertToDTO(savedAdmin);
    }

    /**
     * Update an existing admin.
     * 
     * @throws ResourceNotFoundException  if the admin is not found
     * @throws DuplicateResourceException if the updated username or email already
     *                                    exists for another admin
     */
    @Transactional
    public AdminDTO updateAdmin(Long id, AdminDTO adminDTO) {

        // Find the existing admin or throw an exception
        Admin existingAdmin = adminRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        // Check if username already exists for another admin
        if (adminDTO.getUsername() != null) {
            Admin adminWithSameUsername = adminRepo.findByUsername(adminDTO.getUsername());
            if (adminWithSameUsername != null && !adminWithSameUsername.getId().equals(id)) {
                throw new DuplicateResourceException("Username already exists: " + adminDTO.getUsername());
            }
        }

        // Check if email already exists for another admin
        if (adminDTO.getEmail() != null) {
            Admin adminWithSameEmail = adminRepo.findByEmail(adminDTO.getEmail());
            if (adminWithSameEmail != null && !adminWithSameEmail.getId().equals(id)) {
                throw new DuplicateResourceException("Email already exists: " + adminDTO.getEmail());
            }
        }

        // Update admin fields - only update non-null fields
        if (adminDTO.getFirstName() != null) {
            existingAdmin.setFirstName(adminDTO.getFirstName());
        }
        if (adminDTO.getLastName() != null) {
            existingAdmin.setLastName(adminDTO.getLastName());
        }
        if (adminDTO.getEmail() != null) {
            existingAdmin.setEmail(adminDTO.getEmail());
        }
        if (adminDTO.getMobile() != null) {
            existingAdmin.setMobile(adminDTO.getMobile());
        }
        if (adminDTO.getUsername() != null) {
            existingAdmin.setUsername(adminDTO.getUsername());
        }

        // Only update password if it's provided and not empty
        if (adminDTO.getPassword() != null && !adminDTO.getPassword().isEmpty()) {
            existingAdmin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        }

        if (adminDTO.getRole() != null) {
            existingAdmin.setRole(adminDTO.getRole());
        }
        if (adminDTO.getRoleLimitation() != null) {
            existingAdmin.setRoleLimitation(adminDTO.getRoleLimitation());
        }

        // Save and return the updated admin
        Admin updatedAdmin = adminRepo.save(existingAdmin);
        return convertToDTO(updatedAdmin);
    }

    /**
     * Delete an admin by ID.
     * 
     * @throws ResourceNotFoundException if the admin is not found
     */
    @Transactional
    public void deleteAdmin(Long id) {
        if (!adminRepo.existsById(id)) {
            throw new ResourceNotFoundException("Admin not found with id: " + id);
        }
        adminRepo.deleteById(id);
    }

    /**
     * Convert an Admin entity to an AdminDTO.
     * 
     * @return The AdminDTO object
     */
    private AdminDTO convertToDTO(Admin admin) {
        return new AdminDTO(
                admin.getId(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getEmail(),
                admin.getMobile(),
                admin.getUsername(),
                null, // Password is not returned that it is not exposed in the DTO
                admin.getRole(),
                admin.getRoleLimitation(),
                admin.getCreatedAt(),
                admin.getUpdatedAt());
    }

    /**
     * Convert an AdminDTO to an Admin entity.
     * 
     * @return The Admin entity
     */
    private Admin convertToEntity(AdminDTO adminDTO) {
        Admin admin = new Admin(
                adminDTO.getFirstName(),
                adminDTO.getLastName(),
                adminDTO.getEmail(),
                adminDTO.getMobile(),
                adminDTO.getPassword(),
                adminDTO.getRole(),
                adminDTO.getRoleLimitation());
        // Set username separately as it's not in the constructor
        admin.setUsername(adminDTO.getUsername());
        // ID should only be set for existing entities
        if (adminDTO.getId() != null) {
            admin.setId(adminDTO.getId());
        }

        return admin;
    }

}

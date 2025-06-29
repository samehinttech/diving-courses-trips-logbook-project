package ch.oceandive.service;

import ch.oceandive.dto.PremiumUserDTO;
import ch.oceandive.exceptionHandler.DuplicateResourceException;
import ch.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.repository.PremiumUserRepo;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PremiumUserService {

    private final PremiumUserRepo premiumUserRepo;
    private final PasswordEncoder passwordEncoder;

    public PremiumUserService(PremiumUserRepo premiumUserRepo, PasswordEncoder passwordEncoder) {
        this.premiumUserRepo = premiumUserRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // CRUD operations for PremiumUser
    /**
     * Get all premium users.
     *
     * @return List of PremiumUserDTO objects
     */
    public List<PremiumUserDTO> getAllPremiumUsers() {
        return premiumUserRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all premium users with pagination.
     *
     * @param pageable pagination information
     * @return Page of PremiumUserDTO objects
     */
    public Page<PremiumUserDTO> getAllPremiumUsers(Pageable pageable) {
        return premiumUserRepo.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get a premium user by ID.
     *
     * @throws ResourceNotFoundException if the premium user is not found
     */
    public PremiumUserDTO getPremiumUserById(Long id) {
        PremiumUser premiumUser = premiumUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(premiumUser);
    }

    /**
     * Get a premium user by username.
     *
     * @throws ResourceNotFoundException if the premium user is not found
     */
    public PremiumUserDTO getPremiumUserByUsername(String username) {
        PremiumUser premiumUser = premiumUserRepo.findByUsername(username);
        if (premiumUser == null) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        return convertToDTO(premiumUser);
    }

    public PremiumUser getPremiumUserEntityByUsername(String username) {
        PremiumUser premiumUser = premiumUserRepo.findByUsername(username);
        if (premiumUser == null) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        return premiumUser;
    }

    public PremiumUserDTO getPremiumUserByEmail(String email) {
        PremiumUser premiumUser = premiumUserRepo.findByEmail(email);
        if (premiumUser == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
        return convertToDTO(premiumUser);
    }

    @Transactional
    public PremiumUserDTO createPremiumUser(PremiumUserDTO premiumUserDTO) {
        // Validate user data before processing
        validatePremiumUserFields(premiumUserDTO);

        if (premiumUserRepo.findByUsername(premiumUserDTO.getUsername()) != null) {
            throw new DuplicateResourceException("Username already exists: " + premiumUserDTO.getUsername());
        }

        if (premiumUserRepo.findByEmail(premiumUserDTO.getEmail()) != null) {
            throw new DuplicateResourceException("Email already exists: " + premiumUserDTO.getEmail());
        }

        PremiumUser premiumUser = convertToEntity(premiumUserDTO);
        // Hash the password before saving
        premiumUser.setPassword(passwordEncoder.encode(premiumUser.getPassword()));
        PremiumUser savedPremiumUser = premiumUserRepo.save(premiumUser);
        return convertToDTO(savedPremiumUser);
    }

    @Transactional
    public void updatePremiumUser(Long id, PremiumUserDTO premiumUserDTO) {

        PremiumUser existingUser = premiumUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (premiumUserDTO.getUsername() != null &&
                !existingUser.getUsername().equals(premiumUserDTO.getUsername()) &&
            !premiumUserRepo.findByUsernameAndIdNot(premiumUserDTO.getUsername(), id).isEmpty()) {
            throw new DuplicateResourceException("Username already exists: " + premiumUserDTO.getUsername());
        }

        if (premiumUserDTO.getEmail() != null &&
                !existingUser.getEmail().equals(premiumUserDTO.getEmail()) &&
            !premiumUserRepo.findByEmailAndIdNot(premiumUserDTO.getEmail(), id).isEmpty()) {
            throw new DuplicateResourceException("Email already exists: " + premiumUserDTO.getEmail());
        }

        // Update non-null fields
        if (premiumUserDTO.getFirstName() != null) {
            existingUser.setFirstName(premiumUserDTO.getFirstName());
        }
        if (premiumUserDTO.getLastName() != null) {
            existingUser.setLastName(premiumUserDTO.getLastName());
        }
        if (premiumUserDTO.getEmail() != null) {
            existingUser.setEmail(premiumUserDTO.getEmail());
        }
        if (premiumUserDTO.getMobile() != null) {
            existingUser.setMobile(premiumUserDTO.getMobile());
        }
        if (premiumUserDTO.getDiveCertification() != null) {
            existingUser.setDiveCertification(premiumUserDTO.getDiveCertification());
        }
        if (premiumUserDTO.getUsername() != null) {
            existingUser.setUsername(premiumUserDTO.getUsername());
        }
        if (premiumUserDTO.getRole() != null) {
            existingUser.setRole(premiumUserDTO.getRole());
        }

        // Only update password if provided and not empty
        if (premiumUserDTO.getPassword() != null && !premiumUserDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(premiumUserDTO.getPassword()));
        }

        PremiumUser updatedUser = premiumUserRepo.save(existingUser);
        convertToDTO(updatedUser);
    }

    @Transactional
    public void deletePremiumUser(Long id) {
        if (!premiumUserRepo.existsById(id)) {
            throw new ResourceNotFoundException("Premium user not found with id: " + id);
        }
        premiumUserRepo.deleteById(id);
    }

    private PremiumUserDTO convertToDTO(PremiumUser premiumUser) {
        return new PremiumUserDTO(
                premiumUser.getId(),
                premiumUser.getFirstName(),
                premiumUser.getLastName(),
                premiumUser.getEmail(),
                premiumUser.getMobile(),
                premiumUser.getDiveCertification(),
                premiumUser.getUsername(),
                null, // null to not expose password in DTOs
                premiumUser.getRole(),
                premiumUser.getCreatedAt(),
                premiumUser.getUpdatedAt());
    }
    private PremiumUser convertToEntity(PremiumUserDTO premiumUserDTO) {
        PremiumUser premiumUser = new PremiumUser(
                premiumUserDTO.getFirstName(),
                premiumUserDTO.getLastName(),
                premiumUserDTO.getEmail(),
                premiumUserDTO.getMobile(),
                premiumUserDTO.getDiveCertification(),
                premiumUserDTO.getUsername(),
                premiumUserDTO.getPassword(),
                premiumUserDTO.getRole());
        if (premiumUserDTO.getId() != null) {
            premiumUser.setId(premiumUserDTO.getId());
        }
        return premiumUser;
    }
    // Validate required premium user fields
    private void validatePremiumUserFields(PremiumUserDTO premiumUserDTO) {
        // Check required fields
        if (premiumUserDTO.getEmail() == null || premiumUserDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (premiumUserDTO.getUsername() == null || premiumUserDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (premiumUserDTO.getPassword() == null || premiumUserDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (premiumUserDTO.getFirstName() == null || premiumUserDTO.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (premiumUserDTO.getLastName() == null || premiumUserDTO.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
    }
}

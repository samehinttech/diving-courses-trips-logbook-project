package ch.oceandive.service;

import ch.fhnw.oceandive.dto.GuestUserDTO;
import ch.fhnw.oceandive.exceptionHandler.DuplicateResourceException;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.GuestUser;
import ch.fhnw.oceandive.repository.GuestUserRepo;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing GuestUser entities.
 */
@Service
public class GuestUserService {

    private final GuestUserRepo guestUserRepo;

    public GuestUserService(GuestUserRepo guestUserRepo) {
        this.guestUserRepo = guestUserRepo;
    }

    // Get all guest users.

    public List<GuestUserDTO> getAllGuestUsers() {
        return guestUserRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

   // Get a guest user by ID.
    public GuestUserDTO getGuestUserById(Long id) {
        // Using consistent pattern for null handling
        return convertToDTO(guestUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest user not found with id: " + id)));
    }

 // Get a guest user entity by ID.
    public GuestUser getGuestUserEntityById(Long id) {
        return guestUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest user not found with id: " + id));
    }

    // Get a guest user by email.
    public GuestUserDTO getGuestUserByEmail(String email) {
        // Using Optional for consistent null handling
        return Optional.ofNullable(guestUserRepo.findByEmail(email))
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Guest user not found with email: " + email));
    }

   // Get a guest user by mobile number.
    public GuestUserDTO getGuestUserByMobile(String mobile) {
        // Using Optional for consistent null handling
        return Optional.ofNullable(guestUserRepo.findByMobile(mobile))
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Guest user not found with mobile: " + mobile));
    }

    // Create a new guest user.
    @Transactional
    public GuestUserDTO createGuestUser(GuestUserDTO guestUserDTO) {
        // Validate user data before processing
        validateGuestUserFields(guestUserDTO);

        // Check if email already exists
        if (guestUserRepo.findByEmail(guestUserDTO.getEmail()) != null) {
            throw new DuplicateResourceException("Email already exists: " + guestUserDTO.getEmail());
        }
        // Check if mobile already exists
        if (guestUserDTO.getMobile() != null && !guestUserDTO.getMobile().isEmpty() && 
            guestUserRepo.findByMobile(guestUserDTO.getMobile()) != null) {
            throw new DuplicateResourceException("Mobile number already exists: " + guestUserDTO.getMobile());
        }

        GuestUser guestUser = convertToEntity(guestUserDTO);
        GuestUser savedGuestUser = guestUserRepo.save(guestUser);
        return convertToDTO(savedGuestUser);
    }

    // Update an existing guest user.
    @Transactional
    public GuestUserDTO updateGuestUser(Long id, GuestUserDTO guestUserDTO) {
        // Validate user data before processing
        validateGuestUserFields(guestUserDTO);

        GuestUser existingUser = guestUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest user not found with id: " + id));

        // Check if email already exists for another user
        if (!existingUser.getEmail().equals(guestUserDTO.getEmail()) &&
            !guestUserRepo.findByEmailAndIdNot(guestUserDTO.getEmail(), id).isEmpty()) {
            throw new DuplicateResourceException("Email already exists: " + guestUserDTO.getEmail());
        }

        // Check if mobile already exists for another user
        if (guestUserDTO.getMobile() != null && !guestUserDTO.getMobile().isEmpty() && 
            !existingUser.getMobile().equals(guestUserDTO.getMobile()) &&
            !guestUserRepo.findByMobileAndIdNot(guestUserDTO.getMobile(), id).isEmpty()) {
            throw new DuplicateResourceException("Mobile number already exists: " + guestUserDTO.getMobile());
        }

        // Update user fields
        updateUserFields(existingUser, guestUserDTO);

        GuestUser updatedUser = guestUserRepo.save(existingUser);
        return convertToDTO(updatedUser);
    }

   // Delete a guest user by ID.
    @Transactional
    public void deleteGuestUser(Long id) {
        if (!guestUserRepo.existsById(id)) {
            throw new ResourceNotFoundException("Guest user not found with id: " + id);
        }
        guestUserRepo.deleteById(id);
    }

    // Convert a GuestUser entity to a GuestUserDTO.
    private GuestUserDTO convertToDTO(GuestUser guestUser) {
        return new GuestUserDTO(
                guestUser.getId(),
                guestUser.getFirstName(),
                guestUser.getLastName(),
                guestUser.getEmail(),
                guestUser.getMobile(),
                guestUser.getDiveCertification(),
                guestUser.getRole()
        );
    }

    // Convert a GuestUserDTO to a GuestUser entity.
    private GuestUser convertToEntity(GuestUserDTO guestUserDTO) {
        GuestUser guestUser = new GuestUser(
                guestUserDTO.getFirstName(),
                guestUserDTO.getLastName(),
                guestUserDTO.getEmail(),
                guestUserDTO.getMobile(),
                guestUserDTO.getDiveCertification(),
                guestUserDTO.getRole()
        );

        // ID should only be set for existing entities
        if (guestUserDTO.getId() != null) {
            guestUser.setId(guestUserDTO.getId());
        }

        return guestUser;
    }

    // Helper method to update user fields
    private void updateUserFields(GuestUser existingUser, GuestUserDTO guestUserDTO) {
        existingUser.setFirstName(guestUserDTO.getFirstName());
        existingUser.setLastName(guestUserDTO.getLastName());
        existingUser.setEmail(guestUserDTO.getEmail());
        existingUser.setMobile(guestUserDTO.getMobile());
        existingUser.setDiveCertification(guestUserDTO.getDiveCertification());
        existingUser.setRole(guestUserDTO.getRole());
    }

    // Validate required user fields
    private void validateGuestUserFields(GuestUserDTO guestUserDTO) {
        // Check required fields
        if (guestUserDTO.getEmail() == null || guestUserDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }


        if (guestUserDTO.getFirstName() == null || guestUserDTO.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        if (guestUserDTO.getLastName() == null || guestUserDTO.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
    }

    // Get all guest users with pagination.
    public Page<GuestUserDTO> getAllGuestUsers(Pageable pageable) {
        return guestUserRepo.findAll(pageable)
                .map(this::convertToDTO);
    }
}

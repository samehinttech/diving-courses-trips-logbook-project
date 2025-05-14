package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.dto.GuestUserDTO;
import ch.fhnw.oceandive.exceptionHandler.DuplicateResourceException;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.GuestUser;
import ch.fhnw.oceandive.repository.GuestUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing GuestUser entities.
 */
@Service
public class GuestUserService {

    private final GuestUserRepo guestUserRepo;

    @Autowired
    public GuestUserService(GuestUserRepo guestUserRepo) {
        this.guestUserRepo = guestUserRepo;
    }

    /**
     * Get all guest users.
     *
     * @return List of GuestUserDTO objects
     */
    public List<GuestUserDTO> getAllGuestUsers() {
        return guestUserRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a guest user by ID.
     * @throws ResourceNotFoundException if the guest user is not found
     */
    public GuestUserDTO getGuestUserById(Long id) {
        GuestUser guestUser = guestUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest user not found with id: " + id));
        return convertToDTO(guestUser);
    }

    /**
     * Get a guest user entity by ID.
     * @return The GuestUser entity
     * @throws ResourceNotFoundException if the guest user is not found
     */
    public GuestUser getGuestUserEntityById(Long id) {
        return guestUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest user not found with id: " + id));
    }

    /**
     * Get a guest user by email.
     * @return The GuestUserDTO object
     * @throws ResourceNotFoundException if the guest user is not found
     */
    public GuestUserDTO getGuestUserByEmail(String email) {
        GuestUser guestUser = guestUserRepo.findByEmail(email);
        if (guestUser == null) {
            throw new ResourceNotFoundException("Guest user not found with email: " + email);
        }
        return convertToDTO(guestUser);
    }

    /**
     * Get a guest user by mobile number.
     * @return The GuestUserDTO object
     * @throws ResourceNotFoundException if the guest user is not found
     */
    public GuestUserDTO getGuestUserByMobile(String mobile) {
        GuestUser guestUser = guestUserRepo.findByMobile(mobile);
        if (guestUser == null) {
            throw new ResourceNotFoundException("Guest user not found with mobile: " + mobile);
        }
        return convertToDTO(guestUser);
    }

    /**
     * Create a new guest user.
     * @return The created GuestUserDTO object
     * @throws DuplicateResourceException if a user with the same email already exists
     */
    @Transactional
    public GuestUserDTO createGuestUser(GuestUserDTO guestUserDTO) {
        // Check if email already exists
        if (guestUserRepo.findByEmail(guestUserDTO.getEmail()) != null) {
            throw new DuplicateResourceException("Email already exists: " + guestUserDTO.getEmail());
        }

        GuestUser guestUser = convertToEntity(guestUserDTO);
        GuestUser savedGuestUser = guestUserRepo.save(guestUser);
        return convertToDTO(savedGuestUser);
    }

    /**
     * Update an existing guest user.
     * @return The updated GuestUserDTO object
     * @throws ResourceNotFoundException if the guest user is not found
     * @throws DuplicateResourceException if the updated email already exists for another user
     */
    @Transactional
    public GuestUserDTO updateGuestUser(Long id, GuestUserDTO guestUserDTO) {
        GuestUser existingUser = guestUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest user not found with id: " + id));

        // Check if email already exists for another user
        if (!existingUser.getEmail().equals(guestUserDTO.getEmail()) &&
                guestUserRepo.findByEmailAndIdNot(guestUserDTO.getEmail(), id).size() > 0) {
            throw new DuplicateResourceException("Email already exists: " + guestUserDTO.getEmail());
        }

        // Update user fields
        existingUser.setFirstName(guestUserDTO.getFirstName());
        existingUser.setLastName(guestUserDTO.getLastName());
        existingUser.setEmail(guestUserDTO.getEmail());
        existingUser.setMobile(guestUserDTO.getMobile());
        existingUser.setDiveCertification(guestUserDTO.getDiveCertification());
        existingUser.setRole(guestUserDTO.getRole());

        GuestUser updatedUser = guestUserRepo.save(existingUser);
        return convertToDTO(updatedUser);
    }

    /**
     * Delete a guest user by ID.
     *
     * @param id The ID of the guest user to delete
     * @throws ResourceNotFoundException if the guest user is not found
     */
    @Transactional
    public void deleteGuestUser(Long id) {
        if (!guestUserRepo.existsById(id)) {
            throw new ResourceNotFoundException("Guest user not found with id: " + id);
        }
        guestUserRepo.deleteById(id);
    }

    /**
     * Convert a GuestUser entity to a GuestUserDTO.
     *
     * @param guestUser The GuestUser entity
     * @return The GuestUserDTO object
     */
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

    /**
     * Convert a GuestUserDTO to a GuestUser entity.
     * @return The GuestUser entity
     */
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
}

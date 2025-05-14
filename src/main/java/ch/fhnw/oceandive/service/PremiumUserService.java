package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.dto.PremiumUserDTO;
import ch.fhnw.oceandive.exceptionHandler.DuplicateResourceException;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.model.PremiumUser;
import ch.fhnw.oceandive.repository.PremiumUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing PremiumUser entities.
 */
@Service
public class PremiumUserService {

    private final PremiumUserRepo premiumUserRepo;

    @Autowired
    public PremiumUserService(PremiumUserRepo premiumUserRepo) {
        this.premiumUserRepo = premiumUserRepo;
    }

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
     * Get a premium user by ID.
     * @return The PremiumUserDTO object
     * @throws ResourceNotFoundException if the premium user is not found
     */
    public PremiumUserDTO getPremiumUserById(Long id) {
        PremiumUser premiumUser = premiumUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Premium user not found with id: " + id));
        return convertToDTO(premiumUser);
    }

    /**
     * Get a premium user by username.
     * @return The PremiumUserDTO object
     * @throws ResourceNotFoundException if the premium user is not found
     */
    public PremiumUserDTO getPremiumUserByUsername(String username) {
        PremiumUser premiumUser = premiumUserRepo.findByUsername(username);
        if (premiumUser == null) {
            throw new ResourceNotFoundException("Premium user not found with username: " + username);
        }
        return convertToDTO(premiumUser);
    }

    /**
     * Get a premium user entity by username.
     * @return The PremiumUser entity
     * @throws ResourceNotFoundException if the premium user is not found
     */
    public PremiumUser getPremiumUserEntityByUsername(String username) {
        PremiumUser premiumUser = premiumUserRepo.findByUsername(username);
        if (premiumUser == null) {
            throw new ResourceNotFoundException("Premium user not found with username: " + username);
        }
        return premiumUser;
    }

    /**
     * Get a premium user by email.
     * @return The PremiumUserDTO object
     * @throws ResourceNotFoundException if the premium user is not found
     */
    public PremiumUserDTO getPremiumUserByEmail(String email) {
        PremiumUser premiumUser = premiumUserRepo.findByEmail(email);
        if (premiumUser == null) {
            throw new ResourceNotFoundException("Premium user not found with email: " + email);
        }
        return convertToDTO(premiumUser);
    }

    /**
     * Create a new premium user.
     * @return The created PremiumUserDTO object
     * @throws DuplicateResourceException if a user with the same username or email already exists
     */
    @Transactional
    public PremiumUserDTO createPremiumUser(PremiumUserDTO premiumUserDTO) {
        // Check if username already exists
        if (premiumUserRepo.findByUsername(premiumUserDTO.getUsername()) != null) {
            throw new DuplicateResourceException("Username already exists: " + premiumUserDTO.getUsername());
        }

        // Check if email already exists
        if (premiumUserRepo.findByEmail(premiumUserDTO.getEmail()) != null) {
            throw new DuplicateResourceException("Email already exists: " + premiumUserDTO.getEmail());
        }

        PremiumUser premiumUser = convertToEntity(premiumUserDTO);
        PremiumUser savedPremiumUser = premiumUserRepo.save(premiumUser);
        return convertToDTO(savedPremiumUser);
    }

    /**
     * Update an existing premium user.
     * @return The updated PremiumUserDTO object
     * @throws ResourceNotFoundException if the premium user is not found
     * @throws DuplicateResourceException if the updated username or email already exists for another user
     */
    @Transactional
    public PremiumUserDTO updatePremiumUser(Long id, PremiumUserDTO premiumUserDTO) {
        PremiumUser existingUser = premiumUserRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Premium user not found with id: " + id));

        // Check if username already exists for another user
        if (!existingUser.getUsername().equals(premiumUserDTO.getUsername()) &&
                premiumUserRepo.findByUsernameAndIdNot(premiumUserDTO.getUsername(), id).size() > 0) {
            throw new DuplicateResourceException("Username already exists: " + premiumUserDTO.getUsername());
        }

        // Check if email already exists for another user
        if (!existingUser.getEmail().equals(premiumUserDTO.getEmail()) &&
                premiumUserRepo.findByEmailAndIdNot(premiumUserDTO.getEmail(), id).size() > 0) {
            throw new DuplicateResourceException("Email already exists: " + premiumUserDTO.getEmail());
        }

        // Update user fields
        existingUser.setFirstName(premiumUserDTO.getFirstName());
        existingUser.setLastName(premiumUserDTO.getLastName());
        existingUser.setEmail(premiumUserDTO.getEmail());
        existingUser.setMobile(premiumUserDTO.getMobile());
        existingUser.setDiveCertification(premiumUserDTO.getDiveCertification());
        existingUser.setUsername(premiumUserDTO.getUsername());
        existingUser.setPassword(premiumUserDTO.getPassword()); // Note: In a real app, you'd hash the password
        existingUser.setRole(premiumUserDTO.getRole());

        PremiumUser updatedUser = premiumUserRepo.save(existingUser);
        return convertToDTO(updatedUser);
    }

    /**
     * Delete a premium user by ID.
     * @throws ResourceNotFoundException if the premium user is not found
     */
    @Transactional
    public void deletePremiumUser(Long id) {
        if (!premiumUserRepo.existsById(id)) {
            throw new ResourceNotFoundException("Premium user not found with id: " + id);
        }
        premiumUserRepo.deleteById(id);
    }

    /**
     * Convert a PremiumUser entity to a PremiumUserDTO.
     * @return The PremiumUserDTO object
     */
    private PremiumUserDTO convertToDTO(PremiumUser premiumUser) {
        return new PremiumUserDTO(
                premiumUser.getId(),
                premiumUser.getFirstName(),
                premiumUser.getLastName(),
                premiumUser.getEmail(),
                premiumUser.getMobile(),
                premiumUser.getDiveCertification(),
                premiumUser.getUsername(),
                premiumUser.getPassword(),
                premiumUser.getRole(),
                premiumUser.getCreatedAt(),
                premiumUser.getUpdatedAt()
        );
    }

    /**
     * Convert a PremiumUserDTO to a PremiumUser entity.
     * @return The PremiumUser entity
     */
    private PremiumUser convertToEntity(PremiumUserDTO premiumUserDTO) {
        PremiumUser premiumUser = new PremiumUser(
                premiumUserDTO.getFirstName(),
                premiumUserDTO.getLastName(),
                premiumUserDTO.getEmail(),
                premiumUserDTO.getMobile(),
                premiumUserDTO.getDiveCertification(),
                premiumUserDTO.getUsername(),
                premiumUserDTO.getPassword(),
                premiumUserDTO.getRole()
        );

        // ID should only be set for existing entities
        if (premiumUserDTO.getId() != null) {
            premiumUser.setId(premiumUserDTO.getId());
        }

        return premiumUser;
    }
}

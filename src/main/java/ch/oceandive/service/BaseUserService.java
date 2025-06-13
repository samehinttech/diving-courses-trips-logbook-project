package ch.oceandive.service;

import ch.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.oceandive.exceptionHandler.ValidationException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

// This is class is a boilerplate code after many troubles with users' hierarchy and inheritance.
// will need to get cleaned in the future

/**
 * Base user service class that provides common functionality Admin and User services.
 *
 * @param <T>  The entity type
 * @param <D>  The DTO type
 * @param <ID> The ID type
 */
public abstract class BaseUserService<T, D, ID> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final PasswordEncoder passwordEncoder;
  protected final UserValidationService userValidationService;

  // Common error messages
  protected static final String USER_NOT_FOUND = "%s not found with id: %s";

  public BaseUserService(PasswordEncoder passwordEncoder,
      UserValidationService userValidationService) {
    this.passwordEncoder = passwordEncoder;
    this.userValidationService = userValidationService;
  }

  // Password encoding utility method
  protected String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }

  // Validate basic user fields for CREATE operations (password required)
  protected void validateBasicUserFieldsForCreate(String email, String username, String password,
      String firstName, String lastName) {
    if (email == null || email.trim().isEmpty()) {
      throw new ValidationException("Email cannot be empty");
    }
    if (username == null || username.trim().isEmpty()) {
      throw new ValidationException("Username cannot be empty");
    }
    if (password == null || password.isEmpty()) {
      throw new ValidationException("Password cannot be empty");
    }
    if (firstName == null || firstName.trim().isEmpty()) {
      throw new ValidationException("First name cannot be empty");
    }
    if (lastName == null || lastName.trim().isEmpty()) {
      throw new ValidationException("Last name cannot be empty");
    }
  }

  // Validate basic user fields for UPDATE operations
  // Password is only to reset but not to change for now
  protected void validateBasicUserFieldsForUpdate(String email, String username,
      String firstName, String lastName) {
    if (email == null || email.trim().isEmpty()) {
      throw new ValidationException("Email cannot be empty");
    }
    if (username == null || username.trim().isEmpty()) {
      throw new ValidationException("Username cannot be empty");
    }
    if (firstName == null || firstName.trim().isEmpty()) {
      throw new ValidationException("First name cannot be empty");
    }
    if (lastName == null || lastName.trim().isEmpty()) {
      throw new ValidationException("Last name cannot be empty");
    }
  }
  // Generic method for creating a new user entity
  @Transactional
  protected D createUser(D dto, String entityType) {
    String username = getUsernameFromDto(dto);
    logger.info("Creating new {} with username: {}", entityType, username);
    // Validate user fields - subclass-specific (for CREATE)
    validateUserFieldsForCreate(dto);
    // Validate unique username and email across all user types
    userValidationService.validateUniqueUsername(username, null);
    userValidationService.validateUniqueEmail(getEmailFromDto(dto), null);
    T entity = convertToEntity(dto);
    // Set an encoded password
    setPasswordForEntity(entity, encodePassword(getPasswordFromDto(dto)));
    T savedEntity = saveEntity(entity);
    logger.info("Created {} with ID: {}", entityType, getEntityId(savedEntity));
    return convertToDTO(savedEntity);
  }

  // Generic method for updating an existing user entity
  @Transactional
  protected D updateUser(ID id, D dto, String entityType) {
    logger.info("Updating {} with ID: {}", entityType, id);
    // Find the existing entity or throw an exception
    T existingEntity = findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException(String.format(USER_NOT_FOUND, entityType, id)));

    // Validate user fields for UPDATE
    validateUserFieldsForUpdate(dto);

    // Validate username and email uniqueness if they're being changed
    String newUsername = getUsernameFromDto(dto);
    String currentUsername = getUsernameFromEntity(existingEntity);

    if (newUsername != null && !newUsername.equals(currentUsername)) {
      userValidationService.validateUniqueUsername(newUsername, (Long) id);
    }
    String newEmail = getEmailFromDto(dto);
    String currentEmail = getEmailFromEntity(existingEntity);
    if (newEmail != null && !newEmail.equals(currentEmail)) {
      userValidationService.validateUniqueEmail(newEmail, (Long) id);
    }
    // Update entity fields
    updateEntityFields(existingEntity, dto);
    // Save and return the updated entity
    T updatedEntity = saveEntity(existingEntity);
    logger.info("Updated {} with ID: {}", entityType, getEntityId(updatedEntity));
    return convertToDTO(updatedEntity);
  }
  // Generic delete method for removing an entity by ID
  @Transactional
  public void deleteById(ID id, String entityType) {
    if (!existsById(id)) {
      throw new ResourceNotFoundException(String.format(USER_NOT_FOUND, entityType, id));
    }
    logger.info("Deleting {} with ID: {}", entityType, id);
    performDelete(id);
  }

  // Required abstract methods that subclasses must implement
  // Get data from DTOs
  protected abstract String getUsernameFromDto(D dto);
  protected abstract String getEmailFromDto(D dto);
  protected abstract String getPasswordFromDto(D dto);
  // Get data from entities
  protected abstract String getUsernameFromEntity(T entity);
  protected abstract String getEmailFromEntity(T entity);
  protected abstract ID getEntityId(T entity);
  // Password handling
  protected abstract void setPasswordForEntity(T entity, String encodedPassword);
  // Entity operations
  protected abstract Optional<T> findById(ID id);
  protected abstract T saveEntity(T entity);
  protected abstract boolean existsById(ID id);
  protected abstract void performDelete(ID id);
  // Update operations
  protected abstract void updateEntityFields(T entity, D dto);
  // Validation methods - subclasses must implement both
  protected abstract void validateUserFieldsForCreate(D dto);
  protected abstract void validateUserFieldsForUpdate(D dto);

  // Conversion from entity to DTO and vice versa
  protected abstract D convertToDTO(T entity);
  protected abstract T convertToEntity(D dto);
}
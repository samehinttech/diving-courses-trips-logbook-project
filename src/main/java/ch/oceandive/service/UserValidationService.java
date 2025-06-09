package ch.oceandive.service;

import ch.oceandive.exceptionHandler.DuplicateResourceException;
import ch.oceandive.model.Admin;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.repository.AdminRepo;
import ch.oceandive.repository.PremiumUserRepo;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;



@Service
public class UserValidationService {

  private static final Logger logger = LoggerFactory.getLogger(UserValidationService.class);

  private static final String USERNAME_EXISTS = "Username already exists: %s";
  private static final String EMAIL_EXISTS = "Email already exists: %s";

  private final PremiumUserRepo premiumUserRepo;
  private final AdminRepo adminRepo;

  public UserValidationService(PremiumUserRepo premiumUserRepo, AdminRepo adminRepo) {
    this.premiumUserRepo = premiumUserRepo;
    this.adminRepo = adminRepo;
  }

  public void validateUniqueUsername(String username, Long excludeId) {
    validateUniquenessInPremiumUserRepo(
        username, excludeId, "username", USERNAME_EXISTS, premiumUserRepo::findByUsername, premiumUserRepo::findByUsernameAndIdNot
    );
    validateUniquenessInAdminRepo(
        username, excludeId, "username", USERNAME_EXISTS, adminRepo::findByUsername, adminRepo::findByUsernameAndIdNot
    );
  }

  public void validateUniqueEmail(String email, Long excludeId) {
    validateUniquenessInPremiumUserRepo(
        email, excludeId, "email", EMAIL_EXISTS, premiumUserRepo::findByEmail, premiumUserRepo::findByEmailAndIdNot
    );
    validateUniquenessInAdminRepo(
        email, excludeId, "email", EMAIL_EXISTS, adminRepo::findByEmail, adminRepo::findByEmailAndIdNot
    );
  }

  private void validateUniquenessInPremiumUserRepo(
      String value,
      Long excludeId,
      String fieldName,
      String errorMessage,
      Function<String, PremiumUser> findMethod,
      BiFunction<String, Long, List<PremiumUser>> exclusionMethod
  ) {
    logger.debug("Validating {} uniqueness in PremiumUserRepo: {} (excluding ID: {})", fieldName, value, excludeId);

    if (excludeId == null) {
      if (findMethod.apply(value) != null) {
        logger.warn("{} '{}' already exists in PremiumUser table", fieldName, value);
        throw new DuplicateResourceException(String.format(errorMessage, value));
      }
    } else {
      if (!exclusionMethod.apply(value, excludeId).isEmpty()) {
        logger.warn("{} '{}' already exists in PremiumUser table (excluding ID: {})", fieldName, value, excludeId);
        throw new DuplicateResourceException(String.format(errorMessage, value));
      }
    }
  }

  private void validateUniquenessInAdminRepo(
      String value,
      Long excludeId,
      String fieldName,
      String errorMessage,
      Function<String, Admin> findMethod,
      BiFunction<String, Long, List<Admin>> exclusionMethod
  ) {
    logger.debug("Validating {} uniqueness in AdminRepo: {} (excluding ID: {})", fieldName, value, excludeId);

    if (excludeId == null) {
      if (findMethod.apply(value) != null) {
        logger.warn("{} '{}' already exists in Admin table", fieldName, value);
        throw new DuplicateResourceException(String.format(errorMessage, value));
      }
    } else {
      if (!exclusionMethod.apply(value, excludeId).isEmpty()) {
        logger.warn("{} '{}' already exists in Admin table (excluding ID: {})", fieldName, value, excludeId);
        throw new DuplicateResourceException(String.format(errorMessage, value));
      }
    }
  }
}
package ch.oceandive.service;

import ch.oceandive.model.Course;
import ch.oceandive.model.DiveCertification;
import ch.oceandive.model.DiveCertificationHolder;
import ch.oceandive.model.Trip;
import ch.oceandive.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CertificationValidationService {

  private static final Logger logger = LoggerFactory.getLogger(
      CertificationValidationService.class);

  public CertificationValidationService() {
    // Default constructor for Spring autowiring
  }

  public ValidationResult validateTripBooking(DiveCertification userCertification, Trip trip) {
    logger.debug("Validating trip booking for user with {} certification for trip requiring {}",
        userCertification, trip.getMinCertificationRequired());

    ValidationResult result = new ValidationResult("TRIP", trip);

    // Handle null certifications
    if (userCertification == null) {
      result.setValid(false);
      result.addMessage("User certification is required for trip booking");
      return result;
    }

    if (trip.getMinCertificationRequired() == null) {
      result.setValid(true);
      return result;
    }
    // check if the trip is for a specific certification valid for this user
    boolean isValid = hasMinimumCertification(userCertification,
        trip.getMinCertificationRequired());
    result.setValid(isValid);

    if (isValid) {
      logger.info("Trip booking validation PASSED for user with {} certification",
          userCertification);
    } else {
      logger.warn("Trip booking validation FAILED for user with {} certification",
          userCertification);
      result.addMessage(
          String.format("User certification level %s is insufficient for required level %s",
              userCertification.getDisplayName(),
              trip.getMinCertificationRequired().getDisplayName()));
    }

    return result;
  }

  // Same logic as validateTripBooking, but for courses
  public ValidationResult validateCourseEnrollment(DiveCertification userCertification,
      Course course) {
    logger.debug(
        "Validating course enrollment for user with {} certification for course requiring {}",
        userCertification, course.getMinCertificationRequired());

    ValidationResult result = new ValidationResult("COURSE", course);

    // Handle null certifications properly
    if (userCertification == null) {
      result.setValid(false);
      result.addMessage("User certification is required for course enrollment");
      return result;
    }

    if (course.getMinCertificationRequired() == null) {
      result.setValid(true);
      return result;
    }
    if (course.getName() != null && DiveCertification.fromString(course.getName()) != null) {
      // Check if user has the prerequisite certification for this course
      DiveCertification courseCert = DiveCertification.fromString(course.getName());
      DiveCertification requiredCert = course.getMinCertificationRequired();

      // If the course is for a certification and the user already has it or higher, they don't need it
      if (userCertification.getLevel() >= courseCert.getLevel()) {
        result.setValid(false);
        result.addMessage("You already have this certification or higher");
        return result;
      }
      // Check if the user meets the prerequisites
      boolean isValid = hasMinimumCertification(userCertification, requiredCert);
      result.setValid(isValid);

      if (!isValid) {
        result.addMessage(
            String.format("User certification level %s is insufficient for required level %s",
                userCertification.getDisplayName(),
                requiredCert.getDisplayName()));
      }
    } else {
      // Regular course validation
      boolean isValid = hasMinimumCertification(userCertification,
          course.getMinCertificationRequired());
      result.setValid(isValid);

      if (!isValid) {
        result.addMessage(
            String.format("User certification level %s is insufficient for required level %s",
                userCertification.getDisplayName(),
                course.getMinCertificationRequired().getDisplayName()));
      }
    }

    if (result.isValid()) {
      logger.info("Course enrollment validation PASSED for user with {} certification",
          userCertification);
    } else {
      logger.warn("Course enrollment validation FAILED for user with {} certification: {}",
          userCertification, result.getAllMessages());
    }

    return result;
  }

  public ValidationResult validateCertificationLevel(DiveCertification userCertification,
      DiveCertification requiredCertification) {
    logger.debug("Validating certification level: user has {}, required {}",
        userCertification, requiredCertification);

    ValidationResult result = new ValidationResult("CERTIFICATION");
    if (userCertification == null || requiredCertification == null) {
      result.setValid(userCertification == null && requiredCertification == null);
      if (!result.isValid()) {
        result.addMessage("Both user and required certifications must be specified");
      }
      return result;
    }
    boolean isValid = hasMinimumCertification(userCertification, requiredCertification);
    result.setValid(isValid);

    if (!isValid) {
      result.addMessage(
          String.format("User certification level %s is insufficient for required level %s",
              userCertification.getDisplayName(),
              requiredCertification.getDisplayName()));
    }

    return result;
  }


  public boolean hasMinimumCertification(DiveCertification userCertification,
      DiveCertification requiredCertification) {
    if (userCertification == null) {
      return requiredCertification == null || requiredCertification == DiveCertification.NON_DIVER;
    }

    if (requiredCertification == null || requiredCertification == DiveCertification.NON_DIVER) {
      return true;
    }

    return userCertification.getLevel() >= requiredCertification.getLevel();
  }

  public boolean validateCertification(DiveCertification userCertification,
      DiveCertification requiredCertification) {
    return hasMinimumCertification(userCertification, requiredCertification);
  }

  //Method as util to validate both PremiumUser and GuestUser
  public ValidationResult validateUserForTrip(DiveCertificationHolder user, Trip trip) {
    if (user == null || user.getDiveCertification() == null) {
      ValidationResult result = new ValidationResult("TRIP", trip);
      result.setValid(false);
      result.addMessage("User and certification information is required");
      return result;
    }
    return validateTripBooking(user.getDiveCertification(), trip);
  }

  // Same as validateUserForTrip, but for courses
  public ValidationResult validateUserForCourse(DiveCertificationHolder user, Course course) {
    if (user == null || user.getDiveCertification() == null) {
      ValidationResult result = new ValidationResult("COURSE", course);
      result.setValid(false);
      result.addMessage("User and certification information is required");
      return result;
    }

    return validateCourseEnrollment(user.getDiveCertification(), course);
  }
}

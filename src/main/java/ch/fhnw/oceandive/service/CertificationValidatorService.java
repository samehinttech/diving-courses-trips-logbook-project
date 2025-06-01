package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.model.*;
import ch.fhnw.oceandive.validation.ValidationResult;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CertificationValidatorService {

    private static final Logger logger = LoggerFactory.getLogger(CertificationValidatorService.class);
    private final KieContainer kieContainer;

    @Autowired
    public CertificationValidatorService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public boolean validateCertification(DiveCertification userCertification,
        DiveCertification requiredCertification) {
        if (userCertification == null) {
            userCertification = DiveCertification.NON_DIVER;
        }

        CertificationValidator validator = new CertificationValidator();
        validator.setUserCertification(userCertification);
        validator.setRequiredCertification(requiredCertification);

        KieSession kieSession = null;
        try {
            kieSession = kieContainer.newKieSession();
            kieSession.insert(validator);
            int rulesActivated = kieSession.fireAllRules();

            logger.debug("Certification validation: {} -> {} = {}, rules activated: {}",
                userCertification, requiredCertification, validator.isValid(), rulesActivated);

            return validator.isValid();
        } catch (Exception e) {
            logger.error("Error during certification validation", e);
            return false;
        } finally {
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }

    public ValidationResult validateTripBooking(Trip trip, DiveCertificationHolder user) {
        ValidationResult result = new ValidationResult();

        KieSession kieSession = null;
        try {
            kieSession = kieContainer.newKieSession();
            kieSession.insert(trip);
            kieSession.insert(user);
            kieSession.insert(result);

            int rulesActivated = kieSession.fireAllRules();

            logger.debug("Trip booking validation for {} with cert {}: {}, rules activated: {}",
                user.getFirstName(), user.getDiveCertification(), result.isValid(), rulesActivated);

            return result;
        } catch (Exception e) {
            logger.error("Error during trip booking validation", e);
            result.addMessage("Validation error: " + e.getMessage());
            return result;
        } finally {
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }

    public ValidationResult validateCourseEnrollment(Course course, DiveCertificationHolder user) {
        ValidationResult result = new ValidationResult();

        KieSession kieSession = null;
        try {
            kieSession = kieContainer.newKieSession();
            kieSession.insert(course);
            kieSession.insert(user);
            kieSession.insert(result);

            int rulesActivated = kieSession.fireAllRules();

            logger.debug("Course enrollment validation for {} with cert {}: {}, rules activated: {}",
                user.getFirstName(), user.getDiveCertification(), result.isValid(), rulesActivated);

            return result;
        } catch (Exception e) {
            logger.error("Error during course enrollment validation", e);
            result.addMessage("Validation error: " + e.getMessage());
            return result;
        } finally {
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }
}
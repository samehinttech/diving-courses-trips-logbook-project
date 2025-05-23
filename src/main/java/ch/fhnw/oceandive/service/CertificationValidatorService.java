package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.model.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for validating dive certification requirements using Drools rules engine.
 * Provides methods for both simple certification-level checks and complex object-based validation.
 */
@Service
public class CertificationValidatorService {

    private static final Logger logger = LoggerFactory.getLogger(CertificationValidatorService.class);
    private final KieContainer kieContainer;

    @Autowired
    public CertificationValidatorService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    /**
     * Validates if a user with the given certification can perform an activity 
     * requiring the specified minimum certification level
     * @return true if the user can perform the activity, false otherwise
     */
    public boolean validateCertification(DiveCertification userCertification, 
                                         DiveCertification requiredCertification) {
        CertificationValidator validator = new CertificationValidator();
        validator.setUserCertification(userCertification);
        validator.setRequiredCertification(requiredCertification);
        
        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(validator);
            kieSession.fireAllRules();
            return validator.isValid();
        } finally {
            kieSession.dispose();
        }
    }
    
    /**
     * Validates if a user can book a specific trip based on certification and any other rules
     * @return ValidationResult containing validation status and any error messages
     */
    public ValidationResult validateTripBooking(Trip trip, DiveCertificationHolder user) {
        ValidationResult result = new ValidationResult();
        
        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(trip);
            kieSession.insert(user);
            kieSession.insert(result);
            kieSession.fireAllRules();
            
            logger.debug("Trip booking validation for User {} with cert {}: {}", 
                       user.getFirstName(), user.getDiveCertification(), result.isValid());
            return result;
        } finally {
            kieSession.dispose();
        }
    }
    
    /**
     * Validates if a user can enroll in a specific course based on certification and any other rules
     * @return ValidationResult containing validation status and any error messages
     */
    public ValidationResult validateCourseEnrollment(Course course, DiveCertificationHolder user) {
        ValidationResult result = new ValidationResult();
        
        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(course);
            kieSession.insert(user);
            kieSession.insert(result);
            kieSession.fireAllRules();
            
            logger.debug("Course enrollment validation for User {} with cert {}: {}", 
                       user.getFirstName(), user.getDiveCertification(), result.isValid());
            return result;
        } finally {
            kieSession.dispose();
        }
    }
}
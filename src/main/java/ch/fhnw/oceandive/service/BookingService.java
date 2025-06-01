package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.exceptionHandler.BusinessRuleViolationException;
import ch.fhnw.oceandive.model.*;
import ch.fhnw.oceandive.repository.*;

import ch.fhnw.oceandive.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final SecureRandom random = new SecureRandom();
    private final CourseRepo courseRepo;
    private final TripRepo tripRepo;
    private final CertificationValidatorService certificationValidator;

    @Autowired
    public BookingService(CourseRepo courseRepo, TripRepo tripRepo, 
                         CertificationValidatorService certificationValidator) {
        this.courseRepo = courseRepo;
        this.tripRepo = tripRepo;
        this.certificationValidator = certificationValidator;
    }

    public String generateBookingReference(String name, String personName) {
        // some logic for null checks
        name = name == null ? "COURSE" : name;
        personName = personName == null ? "USER" : personName;
        int randomInt = 100 + random.nextInt(900);
        return name.replaceAll("\\s+", "").toUpperCase() + "_" +
                personName.replaceAll("\\s+", "").toUpperCase() + "_" + randomInt;
    }

    /**
     * Book a course for a user with dive certification.
     * 
     * @throws BusinessRuleViolationException if the course is fully booked or the
     *                                        user doesn't have the required
     *                                        certification
     */
    @Transactional
    public String bookCourse(Course course, DiveCertificationHolder user) {
        if (course.isFullyBooked()) {
            throw new BusinessRuleViolationException("Course is fully booked.");
        }

        // Use the validation service
        ValidationResult validationResult = certificationValidator.validateCourseEnrollment(course, user);
        if (!validationResult.isValid()) {
            throw new BusinessRuleViolationException(
                validationResult.getFirstMessage().isEmpty() ? 
                "User does not have the required certification level for this course." :
                validationResult.getFirstMessage()
            );
        }

        String bookingReference = generateBookingReference(course.getName(), user.getFirstName());
        course.incrementBookings();
        courseRepo.save(course);
        logger.info("Course booked with reference: {}", bookingReference);
        return bookingReference;
    }

    /**
     * Book a trip for a user with dive certification.
     * 
     * @throws BusinessRuleViolationException if the trip is fully booked or the
     *                                        user doesn't have the required
     *                                        certification
     */
    @Transactional
    public String bookTrip(Trip trip, DiveCertificationHolder user) {
        if (trip.isFullyBooked()) {
            throw new BusinessRuleViolationException("Trip is fully booked.");
        }

        // Use the validation service
        ValidationResult validationResult = certificationValidator.validateTripBooking(trip, user);
        if (!validationResult.isValid()) {
            throw new BusinessRuleViolationException(
                validationResult.getFirstMessage().isEmpty() ? 
                "User does not have the required certification level for this trip." :
                validationResult.getFirstMessage()
            );
        }

        String bookingReference = generateBookingReference(trip.getLocation(), user.getFirstName());
        trip.incrementBookings();
        tripRepo.save(trip);
        logger.info("Trip booked with reference: {}", bookingReference);
        return bookingReference;
    }

    // Maintain backward compatibility with existing code
    @Transactional
    public String bookCourse(Course course, PremiumUser user) {
        return bookCourse(course, (DiveCertificationHolder) user);
    }

    @Transactional
    public String bookCourse(Course course, GuestUser guest) {
        return bookCourse(course, (DiveCertificationHolder) guest);
    }

    @Transactional
    public String bookTrip(Trip trip, PremiumUser user) {
        return bookTrip(trip, (DiveCertificationHolder) user);
    }

    @Transactional
    public String bookTrip(Trip trip, GuestUser guest) {
        return bookTrip(trip, (DiveCertificationHolder) guest);
    }
}
package ch.oceandive.service;

import ch.oceandive.exceptionHandler.BusinessRuleViolationException;
import ch.oceandive.model.*;
import ch.oceandive.repository.*;
import ch.oceandive.validation.ValidationResult;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final SecureRandom random = new SecureRandom();
    private final CourseRepo courseRepo;
    private final TripRepo tripRepo;
    private final CertificationValidationService certificationValidator;

    @Autowired
    public BookingService(CourseRepo courseRepo, TripRepo tripRepo,
        CertificationValidationService certificationValidator) {
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
     * Private helper method that contains the common booking logic for courses.
     */
    private String performCourseBooking(Course course, DiveCertificationHolder user) {
        if (course.isFullyBooked()) {
            throw new BusinessRuleViolationException("Course is fully booked.");
        }

        // Use the validation service
        ValidationResult validationResult = certificationValidator.validateCourseEnrollment(user.getDiveCertification(), course);
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
     * Private helper method that contains the common booking logic for trips.
     */
    private String performTripBooking(Trip trip, DiveCertificationHolder user) {
        if (trip.isFullyBooked()) {
            throw new BusinessRuleViolationException("Trip is fully booked.");
        }

        // Use the validation service
        ValidationResult validationResult = certificationValidator.validateTripBooking(user.getDiveCertification(), trip);
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

    /**
     * Book a course for a user with dive certification.
     *
     * @throws BusinessRuleViolationException if the course is fully booked or the
     *                                        user doesn't have the required
     *                                        certification
     */
    @Transactional
    public String bookCourse(Course course, DiveCertificationHolder user) {
        return performCourseBooking(course, user);
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
        return performTripBooking(trip, user);
    }

    // Maintain backward compatibility with existing code
    @Transactional
    public String bookCourse(Course course, PremiumUser user) {
        return performCourseBooking(course, user);
    }

    @Transactional
    public String bookCourse(Course course, GuestUser guest) {
        return performCourseBooking(course, guest);
    }

    @Transactional
    public String bookTrip(Trip trip, PremiumUser user) {
        return performTripBooking(trip, user);
    }

    @Transactional
    public String bookTrip(Trip trip, GuestUser guest) {
        return performTripBooking(trip, guest);
    }
}
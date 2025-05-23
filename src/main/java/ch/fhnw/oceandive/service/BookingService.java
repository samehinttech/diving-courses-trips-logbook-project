package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.exceptionHandler.BusinessRuleViolationException;
import ch.fhnw.oceandive.model.*;
import ch.fhnw.oceandive.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class BookingService {
    /**
     * Logger for logging information and errors while booking courses and trips
     * This logger is used to log booking references and any exceptions that occur
     * during the booking process
     * It helps in debugging and tracking the flow of booking operations
     */
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final SecureRandom random = new SecureRandom(); // I used SecureRandom for better randomness 
    private final CourseRepo courseRepo;
    private final TripRepo tripRepo;

    @Autowired
    public BookingService(CourseRepo courseRepo, TripRepo tripRepo) {
        this.courseRepo = courseRepo;
        this.tripRepo = tripRepo;
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

        // Validate user certification level
        if (user.getDiveCertification().ordinal() < course.getMinCertificationRequired().ordinal()) {
            throw new BusinessRuleViolationException(
                    "User does not have the required certification level for this course.");
        }

        String bookingReference = generateBookingReference(course.getLocation(), user.getFirstName());
        course.incrementBookings();
        courseRepo.save(course); // Save the updated course
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

        // Validate user certification level
        if (user.getDiveCertification().ordinal() < trip.getMinCertificationRequired().ordinal()) {
            throw new BusinessRuleViolationException(
                    "User does not have the required certification level for this trip.");
        }

        String bookingReference = generateBookingReference(trip.getName(), user.getFirstName());
        trip.incrementBookings();
        tripRepo.save(trip); // Save the updated trip
        logger.info("Trip booked with reference: {}", bookingReference);
        return bookingReference;
    }

    // Maintain backward compatibility with existing code
    // These methods are just wrappers around the main booking methods to handle
    // different user types
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

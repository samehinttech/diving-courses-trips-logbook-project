package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.exceptionHandler.BusinessRuleViolationException;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.GuestUser;
import ch.fhnw.oceandive.model.PremiumUser;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.service.BookingService;
import ch.fhnw.oceandive.service.CourseService;
import ch.fhnw.oceandive.service.GuestUserService;
import ch.fhnw.oceandive.service.PremiumUserService;
import ch.fhnw.oceandive.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for booking courses and trips.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final CourseService courseService;
    private final TripService tripService;
    private final PremiumUserService premiumUserService;
    private final GuestUserService guestUserService;

    @Autowired
    public BookingController(BookingService bookingService, CourseService courseService,
                            TripService tripService, PremiumUserService premiumUserService,
                            GuestUserService guestUserService) {
        this.bookingService = bookingService;
        this.courseService = courseService;
        this.tripService = tripService;
        this.premiumUserService = premiumUserService;
        this.guestUserService = guestUserService;
    }

    /**
     * POST /api/bookings/courses/{courseId}/premium: Book a course for a premium user.
     * Requires authentication.
     *
     * @param courseId the ID of the course to book
     * @return the ResponseEntity with status 200 (OK) and the booking reference in the body
     * @throws ResourceNotFoundException if the course is not found
     * @throws BusinessRuleViolationException if the course is fully booked, or the user doesn't have the required certification
     */
    @PostMapping("/courses/{courseId}/premium")
    public ResponseEntity<Map<String, String>> bookCourseForPremiumUser(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        Course course = courseService.getCourseById(courseId);

        String bookingReference = bookingService.bookCourse(course, premiumUser);

        Map<String, String> response = new HashMap<>();
        response.put("bookingReference", bookingReference);
        response.put("message", "Course booked successfully");

        return ResponseEntity.status(201).body(response);
    }

    /**
     * POST /api/bookings/trips/{tripId}/premium: Book a trip for a premium user.
     * Requires authentication.
     *
     * @param tripId the ID of the trip to book
     * @return the ResponseEntity with status 200 (OK) and the booking reference in the body
     * @throws ResourceNotFoundException if the trip is not found
     * @throws BusinessRuleViolationException if the trip is fully booked, or the user doesn't have the required certification
     */
    @PostMapping("/trips/{tripId}/premium")
    public ResponseEntity<Map<String, String>> bookTripForPremiumUser(@PathVariable Long tripId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        Trip trip = tripService.getTripById(tripId);

        String bookingReference = bookingService.bookTrip(trip, premiumUser);

        Map<String, String> response = new HashMap<>();
        response.put("bookingReference", bookingReference);
        response.put("message", "Trip booked successfully");

        return ResponseEntity.status(201).body(response);
    }

    /**
     * POST /api/bookings/courses/{courseId}/guest: Book a course for a guest user.
     * Public endpoint is accessible to all users.
     *
     * @param courseId the ID of the course to book
     * @param guestUserId the ID of the guest user
     * @return the ResponseEntity with status 200 (OK) and the booking reference in the body
     * @throws ResourceNotFoundException if the course or guest user is not found
     * @throws BusinessRuleViolationException if the course is fully booked, or the user doesn't have the required certification
     */
    @PostMapping("/courses/{courseId}/guest")
    public ResponseEntity<Map<String, String>> bookCourseForGuestUser(
            @PathVariable Long courseId,
            @RequestParam Long guestUserId) {

        GuestUser guestUser = guestUserService.getGuestUserEntityById(guestUserId);
        Course course = courseService.getCourseById(courseId);

        String bookingReference = bookingService.bookCourse(course, guestUser);

        Map<String, String> response = new HashMap<>();
        response.put("bookingReference", bookingReference);
        response.put("message", "Course booked successfully");

        return ResponseEntity.status(201).body(response);
    }

    /**
     * POST /api/bookings/trips/{tripId}/guest: Book a trip for a guest user.
     * Public endpoint is accessible to all users.
     *
     * @param tripId the ID of the trip to book
     * @param guestUserId the ID of the guest user
     * @return the ResponseEntity with status 200 (OK) and the booking reference in body
     * @throws ResourceNotFoundException if the trip or guest user is not found
     * @throws BusinessRuleViolationException if the trip is fully booked or the user doesn't have the required certification
     */
    @PostMapping("/trips/{tripId}/guest")
    public ResponseEntity<Map<String, String>> bookTripForGuestUser(
            @PathVariable Long tripId,
            @RequestParam Long guestUserId) {

        GuestUser guestUser = guestUserService.getGuestUserEntityById(guestUserId);
        Trip trip = tripService.getTripById(tripId);

        String bookingReference = bookingService.bookTrip(trip, guestUser);

        Map<String, String> response = new HashMap<>();
        response.put("bookingReference", bookingReference);
        response.put("message", "Trip booked successfully");

        return ResponseEntity.status(201).body(response);
    }
}

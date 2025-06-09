package ch.oceandive.controller.rest;

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
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * POST /api/bookings/courses/{courseId}/user: Book a course for a user.
     * Requires authentication.
     *
     * @param courseId the ID of the course to book
     * @return the ResponseEntity with status 200 (OK) and the booking reference in the body
     * @throws ResourceNotFoundException if the course is not found
     * @throws BusinessRuleViolationException if the course is fully booked, or the user doesn't have the required certification
     */
    @PostMapping("/courses/{courseId}/user")
    public ResponseEntity<Map<String, String>> bookCourseForUser(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);

        Course course = courseService.getCourseById(courseId);
        return bookCourseForEntity(course, premiumUser);
    }

    /**
     * POST /api/bookings/trips/{tripId}/user: Book a trip for a user.
     * Requires authentication.
     *
     * @param tripId the ID of the trip to book
     * @return the ResponseEntity with status 200 (OK) and the booking reference in the body
     * @throws ResourceNotFoundException if the trip is not found
     * @throws BusinessRuleViolationException if the trip is fully booked, or the user doesn't have the required certification
     */
    @PostMapping("/trips/{tripId}/user")
    public ResponseEntity<Map<String, String>> bookTripForUser(@PathVariable Long tripId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);

        Trip trip = tripService.getTripById(tripId);
        return bookTripForEntity(trip, premiumUser);
    }

    /**
     * Helper method to book a course for any entity type
     * 
     * @param course the course to book
     * @param entity the entity (user or guest) booking the course
     * @return the ResponseEntity with booking reference
     */
    private ResponseEntity<Map<String, String>> bookCourseForEntity(Course course, Object entity) {
        String bookingReference;
        if (entity instanceof PremiumUser) {
            bookingReference = bookingService.bookCourse(course, (PremiumUser) entity);
        } else if (entity instanceof GuestUser) {
            bookingReference = bookingService.bookCourse(course, (GuestUser) entity);
        } else {
            throw new IllegalArgumentException("Unsupported entity type");
        }

        Map<String, String> response = new HashMap<>();
        response.put("bookingReference", bookingReference);
        response.put("message", "Course booked successfully");

        return ResponseEntity.status(201).body(response);
    }

    /**
     * Helper method to book a trip for any entity type
     * 
     * @param trip the trip to book
     * @param entity the entity (user or guest) booking the trip
     * @return the ResponseEntity with booking reference
     */
    private ResponseEntity<Map<String, String>> bookTripForEntity(Trip trip, Object entity) {
        String bookingReference;
        if (entity instanceof PremiumUser) {
            bookingReference = bookingService.bookTrip(trip, (PremiumUser) entity);
        } else if (entity instanceof GuestUser) {
            bookingReference = bookingService.bookTrip(trip, (GuestUser) entity);
        } else {
            throw new IllegalArgumentException("Unsupported entity type");
        }

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

        return bookCourseForEntity(course, guestUser);
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

        return bookTripForEntity(trip, guestUser);
    }
}

package ch.oceandive.controller.rest;

import ch.oceandive.model.Course;
import ch.oceandive.model.GuestUser;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.model.Trip;
import ch.oceandive.service.BookingService;
import ch.oceandive.service.CourseService;
import ch.oceandive.service.GuestUserService;
import ch.oceandive.service.PremiumUserService;
import ch.oceandive.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Controller for handling booking operations for courses and trips.
 * Supports both premium users and guest users.
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

    // Book a course for a logged-in user, The course ID is for the backend to identify the course
    @PostMapping("/courses/{courseId}/user")
    public ResponseEntity<Map<String, String>> bookCourseForUser(
        @Parameter(description = "Course ID") @PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        Course course = courseService.getCourseById(courseId);
        return bookCourseForEntity(course, premiumUser);
    }
    // Book a trip for a logged-in user the trip ID is for the backend to identify the trip
    @PostMapping("/trips/{tripId}/user")
    @Operation(summary = "Book trip for user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trip booked successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Trip not found"),
        @ApiResponse(responseCode = "409", description = "Business rule violation")
    })
    public ResponseEntity<Map<String, String>> bookTripForUser(
        @Parameter(description = "Trip ID") @PathVariable Long tripId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);

        Trip trip = tripService.getTripById(tripId);
        return bookTripForEntity(trip, premiumUser);
    }

 // Helper method to book a course for any user type
    private ResponseEntity<Map<String, String>> bookCourseForEntity(Course course, Object entity) {
        String bookingReference;
        if (entity instanceof PremiumUser) {
            bookingReference = bookingService.bookCourse(course, (PremiumUser) entity);
        } else if (entity instanceof GuestUser) {
            bookingReference = bookingService.bookCourse(course, (GuestUser) entity);
        } else {
            throw new IllegalArgumentException("Unsupported user type");
        }
        Map<String, String> response = new HashMap<>();
        response.put("bookingReference", bookingReference);
        response.put("message", "Course booked successfully");
        return ResponseEntity.status(201).body(response);
    }
   // Helper method to book a trip for any user type
    private ResponseEntity<Map<String, String>> bookTripForEntity(Trip trip, Object entity) {
        String bookingReference;
        if (entity instanceof PremiumUser) {
            bookingReference = bookingService.bookTrip(trip, (PremiumUser) entity);
        } else if (entity instanceof GuestUser) {
            bookingReference = bookingService.bookTrip(trip, (GuestUser) entity);
        } else {
            throw new IllegalArgumentException("Unsupported user type");
        }
        Map<String, String> response = new HashMap<>();
        response.put("bookingReference", bookingReference);
        response.put("message", "Trip booked successfully");
        return ResponseEntity.status(201).body(response);
    }

    // Book a course or trip for guest users, and the same (Course ID or Trip ID) is used to identify the course or trip
    @PostMapping("/courses/{courseId}/guest")
    @Operation(summary = "Book course for a public user (guest user)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Course booked successfully"),
        @ApiResponse(responseCode = "404", description = "Course or guest not found"),
        @ApiResponse(responseCode = "409", description = "Business rule violation")
    })
    public ResponseEntity<Map<String, String>> bookCourseForGuestUser(
        @Parameter(description = "Course ID") @PathVariable Long courseId,
        @Parameter(description = "Guest user ID") @RequestParam Long guestUserId) {
        GuestUser guestUser = guestUserService.getGuestUserEntityById(guestUserId);
        Course course = courseService.getCourseById(courseId);
        return bookCourseForEntity(course, guestUser);
    }
    @PostMapping("/trips/{tripId}/guest")
    @Operation(summary = "Book trip for guest")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trip booked successfully"),
        @ApiResponse(responseCode = "404", description = "Trip or guest not found"),
        @ApiResponse(responseCode = "409", description = "Business rule violation")
    })
    public ResponseEntity<Map<String, String>> bookTripForGuestUser(
        @Parameter(description = "Trip ID") @PathVariable Long tripId,
        @Parameter(description = "Guest user ID") @RequestParam Long guestUserId) {
        GuestUser guestUser = guestUserService.getGuestUserEntityById(guestUserId);
        Trip trip = tripService.getTripById(tripId);

        return bookTripForEntity(trip, guestUser);
    }
}
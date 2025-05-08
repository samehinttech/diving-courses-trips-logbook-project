package ch.fhnw.oceandive.controller.user_side;

import ch.fhnw.oceandive.dto.BookingDTO;
import ch.fhnw.oceandive.model.Booking.BookingStatus;
import ch.fhnw.oceandive.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing bookings for authenticated users.
 */
@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("isAuthenticated()")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * GET /api/bookings : Get all bookings for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings(Authentication authentication) {
        String userId = authentication.getName();
        List<BookingDTO> bookings = bookingService.getAllBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/bookings/{id} : Get the booking with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id, Authentication authentication) {
        try {
            BookingDTO booking = bookingService.getBookingById(id);
            
            // Check if the booking belongs to the authenticated user
            if (!booking.getUserId().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/bookings/status/{status} : Get bookings with a specific status for the authenticated user.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getBookingsByStatus(@PathVariable String status, Authentication authentication) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            String userId = authentication.getName();
            List<BookingDTO> bookings = bookingService.getBookingsByUserAndStatus(userId, bookingStatus);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }

    /**
     * POST /api/bookings/trip/{tripId} : Create a new booking for a trip.
     */
    @PostMapping("/trip/{tripId}")
    public ResponseEntity<?> createTripBooking(@PathVariable Long tripId, 
                                              @Valid @RequestBody BookingDTO bookingDTO, 
                                              Authentication authentication) {
        try {
            String userId = authentication.getName();
            BookingDTO createdBooking = bookingService.createBooking(bookingDTO, userId, tripId, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/bookings/course/{courseId} : Create a new booking for a course.
     */
    @PostMapping("/course/{courseId}")
    public ResponseEntity<?> createCourseBooking(@PathVariable Long courseId, 
                                                @Valid @RequestBody BookingDTO bookingDTO, 
                                                Authentication authentication) {
        try {
            String userId = authentication.getName();
            BookingDTO createdBooking = bookingService.createBooking(bookingDTO, userId, null, courseId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/bookings/{id}/cancel : Cancel a booking.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, Authentication authentication) {
        try {
            BookingDTO booking = bookingService.getBookingById(id);
            
            // Check if the booking belongs to the authenticated user
            if (!booking.getUserId().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            
            // Check if the booking can be cancelled
            if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
                return ResponseEntity.badRequest().body("Booking cannot be cancelled");
            }
            
            BookingDTO updatedBooking = bookingService.updateBookingStatus(id, BookingStatus.CANCELLED);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            if (e instanceof jakarta.persistence.EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/bookings/count : Get the number of bookings for the authenticated user.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getBookingCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = bookingService.countBookingsByUser(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
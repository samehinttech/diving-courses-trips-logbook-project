package ch.fhnw.oceandive.controller.admin_side;

import ch.fhnw.oceandive.dto.BookingDTO;
import ch.fhnw.oceandive.model.Booking.BookingStatus;
import ch.fhnw.oceandive.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing bookings by administrators.
 */
@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminBookingController {

    private final BookingService bookingService;

    @Autowired
    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * GET /api/admin/bookings/{id} : Get the booking with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        try {
            BookingDTO booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/admin/bookings/user/{userId} : Get all bookings for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getAllBookingsByUser(@PathVariable String userId) {
        List<BookingDTO> bookings = bookingService.getAllBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/admin/bookings/user/{userId}/status/{status} : Get bookings with a specific status for a user.
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<?> getBookingsByUserAndStatus(@PathVariable String userId, @PathVariable String status) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            List<BookingDTO> bookings = bookingService.getBookingsByUserAndStatus(userId, bookingStatus);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }

    /**
     * GET /api/admin/bookings/trip/{tripId} : Get all bookings for a specific trip.
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<BookingDTO>> getAllBookingsByTrip(@PathVariable Long tripId) {
        List<BookingDTO> bookings = bookingService.getAllBookingsByTrip(tripId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/admin/bookings/course/{courseId} : Get all bookings for a specific course.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<BookingDTO>> getAllBookingsByCourse(@PathVariable Long courseId) {
        List<BookingDTO> bookings = bookingService.getAllBookingsByCourse(courseId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/admin/bookings/status/{status} : Get all bookings with a specific status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getAllBookingsByStatus(@PathVariable String status) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            List<BookingDTO> bookings = bookingService.getAllBookingsByStatus(bookingStatus);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }

    /**
     * GET /api/admin/bookings/after : Get all bookings created after a specific date.
     */
    @GetMapping("/after")
    public ResponseEntity<List<BookingDTO>> getAllBookingsAfterDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        List<BookingDTO> bookings = bookingService.getAllBookingsAfterDate(date);
        return ResponseEntity.ok(bookings);
    }

    /**
     * POST /api/admin/bookings/user/{userId}/trip/{tripId} : Create a new booking for a trip.
     */
    @PostMapping("/user/{userId}/trip/{tripId}")
    public ResponseEntity<?> createTripBooking(@PathVariable String userId, @PathVariable Long tripId, 
                                              @Valid @RequestBody BookingDTO bookingDTO) {
        try {
            BookingDTO createdBooking = bookingService.createBooking(bookingDTO, userId, tripId, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/admin/bookings/user/{userId}/course/{courseId} : Create a new booking for a course.
     */
    @PostMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<?> createCourseBooking(@PathVariable String userId, @PathVariable Long courseId, 
                                                @Valid @RequestBody BookingDTO bookingDTO) {
        try {
            BookingDTO createdBooking = bookingService.createBooking(bookingDTO, userId, null, courseId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/admin/bookings/{id}/status/{status} : Update a booking's status.
     */
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            BookingDTO updatedBooking = bookingService.updateBookingStatus(id, bookingStatus);
            return ResponseEntity.ok(updatedBooking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        } catch (Exception e) {
            if (e instanceof jakarta.persistence.EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/admin/bookings/{id} : Delete a booking.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e instanceof jakarta.persistence.EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/admin/bookings/count/trip/{tripId} : Get the number of bookings for a trip.
     */
    @GetMapping("/count/trip/{tripId}")
    public ResponseEntity<Map<String, Long>> getBookingCountByTrip(@PathVariable Long tripId) {
        long count = bookingService.countBookingsByTrip(tripId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * GET /api/admin/bookings/count/course/{courseId} : Get the number of bookings for a course.
     */
    @GetMapping("/count/course/{courseId}")
    public ResponseEntity<Map<String, Long>> getBookingCountByCourse(@PathVariable Long courseId) {
        long count = bookingService.countBookingsByCourse(courseId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * GET /api/admin/bookings/count/user/{userId} : Get the number of bookings for a user.
     */
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<Map<String, Long>> getBookingCountByUser(@PathVariable String userId) {
        long count = bookingService.countBookingsByUser(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
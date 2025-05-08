package ch.fhnw.oceandive.controller.client_side;

import ch.fhnw.oceandive.dto.BookingDTO;
import ch.fhnw.oceandive.dto.PublicBookingDTO;
import ch.fhnw.oceandive.dto.UserDTO;
import ch.fhnw.oceandive.model.Booking.BookingStatus;
import ch.fhnw.oceandive.service.BookingService;
import ch.fhnw.oceandive.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for public bookings (no authentication required)
 * Allows guests to book trips and courses without creating an account
 */
@RestController
@RequestMapping("/api/public/bookings")
public class PublicBookingController {

    private final BookingService bookingService;
    private final UserService userService;

    @Autowired
    public PublicBookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    /**
     * POST /api/public/bookings/trip/{tripId} : Create a new booking for a trip without authentication
     */
    @PostMapping("/trip/{tripId}")
    public ResponseEntity<?> createPublicTripBooking(
            @PathVariable Long tripId,
            @Valid @RequestBody PublicBookingDTO publicBookingDTO) {
        try {
            // Create a temporary user or find an existing user by email
            UserDTO userDTO = userService.createOrGetTemporaryUser(
                publicBookingDTO.getEmail(),
                publicBookingDTO.getFirstName(),
                publicBookingDTO.getLastName(),
                publicBookingDTO.getPhoneNumber(),
                publicBookingDTO.getDiveCertification()
            );
            
            // Create booking using the temporary user
            BookingDTO bookingDTO = new BookingDTO(
                null, 
                userDTO.getId(), 
                tripId, 
                null, 
                publicBookingDTO.getNumberOfBookings(),
                LocalDateTime.now(),
                BookingStatus.PENDING,
                userDTO.getUsername(),
                null,
                null
            );
            
            BookingDTO createdBooking = bookingService.createBooking(
                bookingDTO, 
                userDTO.getId(), 
                tripId, 
                null
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/public/bookings/course/{courseId} : Create a new booking for a course without authentication
     */
    @PostMapping("/course/{courseId}")
    public ResponseEntity<?> createPublicCourseBooking(
            @PathVariable Long courseId,
            @Valid @RequestBody PublicBookingDTO publicBookingDTO) {
        try {
            // Create a temporary user or find an existing user by email
            UserDTO userDTO = userService.createOrGetTemporaryUser(
                publicBookingDTO.getEmail(),
                publicBookingDTO.getFirstName(),
                publicBookingDTO.getLastName(),
                publicBookingDTO.getPhoneNumber(),
                publicBookingDTO.getDiveCertification()
            );
            
            // Create booking using the temporary user
            BookingDTO bookingDTO = new BookingDTO(
                null, 
                userDTO.getId(), 
                null, 
                courseId, 
                publicBookingDTO.getNumberOfBookings(),
                LocalDateTime.now(),
                BookingStatus.PENDING,
                userDTO.getUsername(),
                null,
                null
            );
            
            BookingDTO createdBooking = bookingService.createBooking(
                bookingDTO, 
                userDTO.getId(), 
                null, 
                courseId
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
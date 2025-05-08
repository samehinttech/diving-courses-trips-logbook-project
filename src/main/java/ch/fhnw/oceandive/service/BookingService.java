package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.dto.BookingDTO;
import ch.fhnw.oceandive.model.Booking;
import ch.fhnw.oceandive.model.Booking.BookingStatus;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.model.UserEntity;
import ch.fhnw.oceandive.repository.BookingRepository;
import ch.fhnw.oceandive.repository.CourseRepository;
import ch.fhnw.oceandive.repository.TripRepository;
import ch.fhnw.oceandive.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing bookings in the application.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, UserRepository userRepository,
                         TripRepository tripRepository, CourseRepository courseRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Get a booking by ID.
     * @throws EntityNotFoundException if no booking with the given ID exists
     */
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
        return BookingDTO.fromEntity(booking);
    }

    /**
     * Get all bookings for a user.
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookingsByUser(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        return bookingRepository.findAllByUser(user).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for a user with a specific status.
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByUserAndStatus(String userId, BookingStatus status) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        return bookingRepository.findAllByUserAndStatus(user, status).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for a trip.
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookingsByTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + tripId));
        
        return bookingRepository.findAllByTrip(trip).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for a course.
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookingsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        
        return bookingRepository.findAllByCourse(course).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a new booking.
     */
    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO, String userId, Long tripId, Long courseId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Trip trip = null;
        if (tripId != null) {
            trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + tripId));
            
            // Check if there are available spots
            if (trip.getAvailableSpots() < bookingDTO.getNumberOfBookings()) {
                throw new IllegalArgumentException("Not enough available spots for this trip");
            }
            
            // Update available spots
            trip.setAvailableSpots(trip.getAvailableSpots() - bookingDTO.getNumberOfBookings());
            tripRepository.save(trip);
        }
        
        Course course = null;
        if (courseId != null) {
            course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
            
            // Check if there are available spots
            if (course.getSpotsAvailable() < bookingDTO.getNumberOfBookings()) {
                throw new IllegalArgumentException("Not enough available spots for this course");
            }
            
            // Update available spots
            course.setSpotsAvailable(course.getSpotsAvailable() - bookingDTO.getNumberOfBookings());
            courseRepository.save(course);
        }
        
        // Create booking with current date if not provided
        LocalDateTime bookingDate = bookingDTO.getBookingDate() != null ? 
                bookingDTO.getBookingDate() : LocalDateTime.now();
        
        // Set status to PENDING if not provided
        BookingStatus status = bookingDTO.getStatus() != null ? 
                bookingDTO.getStatus() : BookingStatus.PENDING;
        
        // Create new booking DTO with updated values
        BookingDTO updatedBookingDTO = new BookingDTO(
                bookingDTO.getId(),
                userId,
                tripId,
                courseId,
                bookingDTO.getNumberOfBookings(),
                bookingDate,
                status,
                user.getUsername(),
                trip != null ? trip.getTripTitle() : null,
                course != null ? course.getCourseTitle() : null
        );
        
        Booking booking = BookingDTO.toEntity(updatedBookingDTO, user, trip, course);
        Booking savedBooking = bookingRepository.save(booking);
        
        // Update user's booking count
        user.setBookingsCount(user.getBookingsCount() + 1);
        userRepository.save(user);
        
        return BookingDTO.fromEntity(savedBooking);
    }

    /**
     * Update a booking's status.
     */
    @Transactional
    public BookingDTO updateBookingStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
        
        // If cancelling or rejecting a previously confirmed booking, restore available spots
        if ((status == BookingStatus.CANCELLED || status == BookingStatus.REJECTED) && 
                booking.getStatus() == BookingStatus.CONFIRMED) {
            
            if (booking.getTrip() != null) {
                Trip trip = booking.getTrip();
                trip.setAvailableSpots(trip.getAvailableSpots() + booking.getNumberOfBookings());
                tripRepository.save(trip);
            }
            
            if (booking.getCourse() != null) {
                Course course = booking.getCourse();
                course.setSpotsAvailable(course.getSpotsAvailable() + booking.getNumberOfBookings());
                courseRepository.save(course);
            }
        }
        
        booking.setStatus(status);
        Booking savedBooking = bookingRepository.save(booking);
        return BookingDTO.fromEntity(savedBooking);
    }

    /**
     * Delete a booking.
     */
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
        
        // Restore available spots if the booking was confirmed
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            if (booking.getTrip() != null) {
                Trip trip = booking.getTrip();
                trip.setAvailableSpots(trip.getAvailableSpots() + booking.getNumberOfBookings());
                tripRepository.save(trip);
            }
            
            if (booking.getCourse() != null) {
                Course course = booking.getCourse();
                course.setSpotsAvailable(course.getSpotsAvailable() + booking.getNumberOfBookings());
                courseRepository.save(course);
            }
        }
        
        // Update user's booking count
        UserEntity user = booking.getUser();
        if (user != null && user.getBookingsCount() > 0) {
            user.setBookingsCount(user.getBookingsCount() - 1);
            userRepository.save(user);
        }
        
        bookingRepository.deleteById(id);
    }

    /**
     * Get all bookings with a specific status.
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookingsByStatus(BookingStatus status) {
        return bookingRepository.findAllByStatus(status).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings created after a specific date.
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookingsAfterDate(LocalDateTime date) {
        return bookingRepository.findAllByBookingDateAfter(date).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Count the number of bookings for a trip.
     */
    @Transactional(readOnly = true)
    public long countBookingsByTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + tripId));
        
        return bookingRepository.countByTrip(trip);
    }

    /**
     * Count the number of bookings for a course.
     */
    @Transactional(readOnly = true)
    public long countBookingsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        
        return bookingRepository.countByCourse(course);
    }

    /**
     * Count the number of bookings for a user.
     */
    @Transactional(readOnly = true)
    public long countBookingsByUser(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        return bookingRepository.countByUser(user);
    }
}
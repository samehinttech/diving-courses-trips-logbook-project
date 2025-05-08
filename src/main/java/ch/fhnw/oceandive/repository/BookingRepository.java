package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.Booking;
import ch.fhnw.oceandive.model.Booking.BookingStatus;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing bookings in the application.
 */
@Repository
@Component
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    /**
     * Find all bookings for a specific user.
     */
    List<Booking> findAllByUser(UserEntity user);
    
    /**
     * Find all bookings for a specific user with a specific status.
     */
    List<Booking> findAllByUserAndStatus(UserEntity user, BookingStatus status);
    
    /**
     * Find all bookings for a specific trip.
     */
    List<Booking> findAllByTrip(Trip trip);
    
    /**
     * Find all bookings for a specific trip with a specific status.
     */
    List<Booking> findAllByTripAndStatus(Trip trip, BookingStatus status);
    
    /**
     * Find all bookings for a specific course.
     */
    List<Booking> findAllByCourse(Course course);
    
    /**
     * Find all bookings for a specific course with a specific status.
     */
    List<Booking> findAllByCourseAndStatus(Course course, BookingStatus status);
    
    /**
     * Find all bookings created after a specific date.
     */
    List<Booking> findAllByBookingDateAfter(LocalDateTime date);
    
    /**
     * Find all bookings with a specific status.
     */
    List<Booking> findAllByStatus(BookingStatus status);
    
    /**
     * Count the number of bookings for a specific trip.
     */
    long countByTrip(Trip trip);
    
    /**
     * Count the number of bookings for a specific course.
     */
    long countByCourse(Course course);
    
    /**
     * Count the number of bookings for a specific user.
     */
    long countByUser(UserEntity user);
}
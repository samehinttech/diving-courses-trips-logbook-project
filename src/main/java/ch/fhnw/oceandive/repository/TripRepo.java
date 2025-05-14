package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Trip entities.
 */
@Repository
public interface TripRepo extends JpaRepository<Trip, Long> {
    
    /**
     * Find trips with start date after the given date.
     * 
     * @param date The date to compare with
     * @return List of trips starting after the given date
     */
    List<Trip> findByStartDateAfter(LocalDate date);
    
    /**
     * Find trips with start date before the given date.
     * 
     * @param date The date to compare with
     * @return List of trips starting before the given date
     */
    List<Trip> findByStartDateBefore(LocalDate date);
    
    /**
     * Find trips with start date between the given dates.
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of trips starting between the given dates
     */
    List<Trip> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find trips by name containing the given string (case insensitive).
     * 
     * @param name The name to search for
     * @return List of trips with names containing the given string
     */
    List<Trip> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find trips that are not fully booked.
     * 
     * @return List of trips that are not fully booked
     */
    @Query("SELECT t FROM Trip t WHERE t.currentBookings < t.capacity")
    List<Trip> findAvailableTrips();
}
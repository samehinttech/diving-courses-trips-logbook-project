package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Trip entities.
 */
@Repository
public interface TripRepo extends JpaRepository<Trip, Long> {

    // Find trips with start date after the given date
    List<Trip> findByStartDateAfter(LocalDate date);

    // Find trips with start date before the given date
    List<Trip> findByStartDateBefore(LocalDate date);

    // Find trips with start date between the given dates
    List<Trip> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    // Find trips with a location containing the given string (case insensitive)
    List<Trip> findByLocationContainingIgnoreCase(String location);

    // Find trips with available capacity (current bookings less than capacity)
    @Query("SELECT t FROM Trip t WHERE t.currentBookings < t.capacity")
    List<Trip> findAvailableTrips();

    // Find trips by minimum certification required
    List<Trip> findByMinCertificationRequired(DiveCertification minCertificationRequired);

    // Get all trips with pagination
    @Query("SELECT t FROM Trip t")
    Page<Trip> getAllTrips(Pageable pageable);

}
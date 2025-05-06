package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing Trip entities.
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    /**
     * Find trips by title (case-insensitive partial match).
     */
    List<Trip> findByTripTitleContainingIgnoreCase(String tripTitle);
    
    /**
     * Find trips within a date range.
     */
    List<Trip> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find trips by required certification.
     */
    List<Trip> findByRequiredCertification(DiveCertification requiredCertification);
    
    /**
     * Find all active trips.
     */
    List<Trip> findAllByIsActiveTrue();
    
    /**
     * Find all deleted trips.
     */
    List<Trip> findAllByIsDeletedTrue();
    
    /**
     * Find all active and not deleted trips.
     */
    List<Trip> findAllByIsActiveTrueAndIsDeletedFalse();
    
    /**
     * Find trips by location (case-insensitive partial match).
     */
    List<Trip> findByLocationContainingIgnoreCase(String location);
}

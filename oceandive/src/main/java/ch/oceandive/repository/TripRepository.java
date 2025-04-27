package ch.oceandive.repository;

import ch.oceandive.model.DiveCertification
import ch.oceandive.model.Trip
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Trip entity.
 * Provides Read operation for the Trip entity.
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    /**
     * Find all available trips.
     * @return a list of available trips
     */
    List<Trip> findByIsAvailableTrue();
    
    /**
     * Find all available trips that start after the given date.
     * @param date the date to search from
     * @return a list of available trips that start after the given date
     */
    List<Trip> findByStartDateAfterAndIsAvailableTrue(LocalDate date);
    
    /**
     * Find all available trips that require a specific certification level.
     * @param certification the certification level
     * @return a list of available trips that require the specified certification level
     */
    List<Trip> findByRequiredCertificationAndIsAvailableTrue(DiveCertification certification);
    
    /**
     * Find all available trips that require a certification level less than or equal to the given level.
     * @param certification the certification level
     * @return a list of available trips that require a certification level less than or equal to the given level
     */
    @Query("SELECT t FROM Trip t WHERE t.minimumCertification <= :certification AND t.isAvailable = true")
    List<Trip> findByRequiredCertificationLessThanEqualAndIsAvailableTrue(@Param("certification") DiveCertification certification);
    
    /**
     * Find all available trips by location (case-insensitive).
     * @param location the location to search for
     * @return a list of available trips at the specified location
     */
    List<Trip> findByLocationContainingIgnoreCaseAndIsAvailableTrue(String location);
    
    /**
     * Find all available trips within the given date range.
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of available trips within the given date range
     */
    List<Trip> findByStartDateGreaterThanEqualAndEndDateLessThanEqualAndIsAvailableTrue(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find all available trips that have spots available.
     * @return a list of available trips that have spots available
     */
    List<Trip> findBySpotsAvailableGreaterThanAndIsAvailableTrue(Integer spotsAvailable);
}
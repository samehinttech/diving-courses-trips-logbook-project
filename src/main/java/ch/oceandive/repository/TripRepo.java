package ch.oceandive.repository;

import ch.oceandive.model.DiveCertification;
import ch.oceandive.model.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface TripRepo extends JpaRepository<Trip, Long> {

    List<Trip> findByStartDateAfter(LocalDate date);

    List<Trip> findByStartDateBefore(LocalDate date);

    List<Trip> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    List<Trip> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT t FROM Trip t WHERE t.currentBookings < t.capacity")
    List<Trip> findAvailableTrips();

    List<Trip> findByMinCertificationRequired(DiveCertification minCertificationRequired);

    @Query("SELECT t FROM Trip t")
    Page<Trip> getAllTrips(Pageable pageable);
}

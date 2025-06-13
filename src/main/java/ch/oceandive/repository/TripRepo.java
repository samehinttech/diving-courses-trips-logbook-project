package ch.oceandive.repository;

import ch.oceandive.model.PublicationStatus;
import ch.oceandive.model.DiveCertification;
import ch.oceandive.model.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface TripRepo extends JpaRepository<Trip, Long> {


  // Find a trip by slug.
  Optional<Trip> findBySlug(String slug);

  //Find published trips by their ascending display order.
  List<Trip> findByStatusOrderByDisplayOrderAsc(PublicationStatus status);

  // Get all trips with pagination.
  @Query("SELECT t FROM Trip t ORDER BY t.displayOrder ASC, t.startDate ASC")
  Page<Trip> getAllTrips(Pageable pageable);

  // Find trips starting after a specific date.
  List<Trip> findByStartDateAfter(LocalDate date);

  // Find trips starting before a specific date.
  List<Trip> findByStartDateBefore(LocalDate date);

  // Find trips starting between two dates.
  List<Trip> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

  // Find upcoming trips (starting after today).
  @Query("SELECT t FROM Trip t WHERE t.startDate > CURRENT_DATE AND t.status = 'PUBLISHED' ORDER BY t.startDate ASC")
  List<Trip> findUpcomingTrips();

  // Find active trips (currently ongoing).
  @Query("SELECT t FROM Trip t WHERE t.startDate <= CURRENT_DATE AND t.endDate >= CURRENT_DATE AND t.status = 'PUBLISHED'")
  List<Trip> findActiveTrips();

  // Find past trips (already completed).
  @Query("SELECT t FROM Trip t WHERE t.endDate < CURRENT_DATE ORDER BY t.endDate DESC")
  List<Trip> findPastTrips();

  // Find trips by location
  @Query("SELECT t FROM Trip t WHERE LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%')) AND t.status = 'PUBLISHED' ORDER BY t.startDate ASC")
  List<Trip> findByLocationContaining(@Param("location") String location);

  //Find trips that are not fully booked.
  @Query("SELECT t FROM Trip t WHERE t.currentBookings < t.capacity AND t.status = 'PUBLISHED'")
  List<Trip> findAvailableTrips();

  // Find trips with available spots in upcoming dates.
  @Query("SELECT t FROM Trip t WHERE t.currentBookings < t.capacity AND t.startDate > CURRENT_DATE AND t.status = 'PUBLISHED' ORDER BY t.startDate ASC")
  List<Trip> findAvailableUpcomingTrips();

  //Find trips with low booking rates (less than 50% capacity).
  @Query("SELECT t FROM Trip t WHERE (t.currentBookings * 100.0 / t.capacity) < 50.0 AND t.status = 'PUBLISHED'")
  List<Trip> findTripsWithLowBookings();

  // Find trips by minimum certification required
  List<Trip> findByMinCertificationRequired(DiveCertification certification);

  // Find trips accessible with a given certification level.
  @Query("SELECT t FROM Trip t WHERE t.minCertificationRequired <= :certification AND t.status = 'PUBLISHED' ORDER BY t.startDate ASC")
  List<Trip> findTripsForCertificationLevel(
      @Param("certification") DiveCertification certification);

  // Find trips within the price range.
  @Query("SELECT t FROM Trip t WHERE t.price BETWEEN :minPrice AND :maxPrice AND t.status = 'PUBLISHED' ORDER BY t.price ASC")
  List<Trip> findTripsByPriceRange(@Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice);

  //Find featured trips. (Like PUBLISHED, ARCHIVED, etc.)
  List<Trip> findByFeaturedTrueAndStatusOrderByDisplayOrderAsc(PublicationStatus status);

  //Advanced search with multiple criteria. (location, date range, certification, availability, price range)
  @Query("SELECT t FROM Trip t WHERE " +
      "(:location IS NULL OR LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
      "(:startDate IS NULL OR t.startDate >= :startDate) AND " +
      "(:endDate IS NULL OR t.startDate <= :endDate) AND " +
      "(:minCertification IS NULL OR t.minCertificationRequired <= :minCertification) AND " +
      "(:availableOnly = false OR t.currentBookings < t.capacity) AND " +
      "(:minPrice IS NULL OR t.price >= :minPrice) AND " +
      "(:maxPrice IS NULL OR t.price <= :maxPrice) AND " +
      "t.status = 'PUBLISHED' " +
      "ORDER BY t.startDate ASC")
  List<Trip> searchTrips(@Param("location") String location,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("minCertification") DiveCertification minCertification,
      @Param("availableOnly") boolean availableOnly,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice);

  //Find similar trips based on location and certification.
  @Query("SELECT t FROM Trip t WHERE " +
      "t.id != :excludeId AND " +
      "(LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
      "t.minCertificationRequired = :certification) AND " +
      "t.status = 'PUBLISHED' " +
      "ORDER BY t.startDate ASC")
  List<Trip> findSimilarTrips(@Param("excludeId") Long excludeId,
      @Param("location") String location,
      @Param("certification") DiveCertification certification,
      Pageable pageable);

  //Get total capacity across all trips.
  @Query("SELECT SUM(t.capacity) FROM Trip t WHERE t.status = 'PUBLISHED'")
  Integer getTotalCapacity();

  //Get total current bookings across all trips.
  @Query("SELECT SUM(t.currentBookings) FROM Trip t WHERE t.status = 'PUBLISHED'")
  Integer getTotalBookings();


  // Get average trip price.
  @Query("SELECT AVG(t.price) FROM Trip t WHERE t.price IS NOT NULL AND t.status = 'PUBLISHED'")
  BigDecimal getAveragePrice();


  // Get distinct locations.
  @Query("SELECT DISTINCT t.location FROM Trip t WHERE t.status = 'PUBLISHED' ORDER BY t.location")
  List<String> findDistinctLocations();

  // Find trips with invalid data (for maintenance).
  @Query("SELECT t FROM Trip t WHERE " +
      "t.startDate > t.endDate OR " +
      "t.currentBookings > t.capacity OR " +
      "t.capacity <= 0 OR " +
      "t.currentBookings < 0")
  List<Trip> findTripsWithInvalidData();

  // Find expired trips that should be archived.
  @Query("SELECT t FROM Trip t WHERE t.endDate < :cutoffDate AND t.status != 'ARCHIVED'")
  List<Trip> findExpiredTrips(@Param("cutoffDate") LocalDate cutoffDate);

  //Find trips needing slug regeneration.
  @Query("SELECT t FROM Trip t WHERE t.slug IS NULL OR t.slug = ''")
  List<Trip> findTripsWithoutSlug();

  // Find popular destinations (locations with most trips).
  @Query("SELECT t.location, COUNT(t) as tripCount FROM Trip t WHERE t.status = 'PUBLISHED' GROUP BY t.location ORDER BY tripCount DESC")
  List<Object[]> findPopularDestinations();

  // Find the most booked trips.
  @Query("SELECT t FROM Trip t WHERE t.status = 'PUBLISHED' ORDER BY (t.currentBookings * 100.0 / t.capacity) DESC")
  List<Trip> findMostBookedTrips(Pageable pageable);

}
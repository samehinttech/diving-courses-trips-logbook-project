package ch.oceandive.service;

import ch.oceandive.dto.TripDTO;
import ch.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.oceandive.utils.PublicationStatus;
import ch.oceandive.utils.DiveCertification;
import ch.oceandive.model.Trip;
import ch.oceandive.repository.TripRepo;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TripService {

    private static final Logger logger = LoggerFactory.getLogger(TripService.class);

    private final TripRepo tripRepo;
    private final CertificationValidationService certificationValidator;

    public TripService(TripRepo tripRepo, CertificationValidationService certificationValidator) {
        this.tripRepo = tripRepo;
        this.certificationValidator = certificationValidator;
    }

    // ===== BASIC CRUD OPERATIONS =====

    /**
     * Get all trips.
     * @return List of all trips
     */
    public List<Trip> getAllTrips() {
        return tripRepo.findByStatusOrderByDisplayOrderAsc(PublicationStatus.PUBLISHED);
    }

    /**
     * Get all trips including unpublished ones (admin only).
     * @return List of all trips regardless of status
     */
    public List<Trip> getAllTripsIncludingUnpublished() {
        return tripRepo.findAll();
    }

    /**
     * Get a trip by ID.
     * @param id the trip ID
     * @return The trip
     * @throws ResourceNotFoundException if the trip is not found
     */
    public Trip getTripById(Long id) {
        return tripRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
    }

    /**
     * Get a trip by slug.
     * @param slug the trip slug
     * @return The trip
     * @throws ResourceNotFoundException if the trip is not found
     */
    public Trip getTripBySlug(String slug) {
        return tripRepo.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Trip not found with slug: " + slug));
    }

    /**
     * Get all trips with pagination.
     * @param pageable pagination information
     * @return Page of trips
     */
    public Page<Trip> getAllTrips(Pageable pageable) {
        return tripRepo.getAllTrips(pageable);
    }

    /**
     * Create a new trip.
     * @param trip the trip to create
     * @return The created trip
     */
    @Transactional
    public Trip createTrip(Trip trip) {
        logger.info("Creating new trip: {}", trip.getLocation());

        // Validate trip data
        validateTripData(trip);

        // Ensure currentBookings starts at 0
        trip.setCurrentBookings(0);

        // Ensure status is set
        if (trip.getStatus() == null) {
            trip.setStatus(PublicationStatus.PUBLISHED);
        }

        Trip savedTrip = tripRepo.save(trip);
        logger.info("Trip created successfully with ID: {}", savedTrip.getId());

        return savedTrip;
    }

    /**
     * Create a new trip from DTO.
     * @param tripDTO the trip DTO
     * @return The created trip
     */
    @Transactional
    public Trip createTrip(TripDTO tripDTO) {
        Trip trip = tripDTO.toEntity();
        return createTrip(trip);
    }

    /**
     * Update an existing trip.
     * @param id the trip ID
     * @param tripDetails the updated trip data
     * @return The updated trip
     * @throws ResourceNotFoundException if the trip is not found
     */
    @Transactional
    public Trip updateTrip(Long id, Trip tripDetails) {
        logger.info("Updating trip with ID: {}", id);

        Trip trip = getTripById(id);

        // Validate trip data
        validateTripData(tripDetails);

        // Update fields
        updateTripFields(trip, tripDetails);

        Trip updatedTrip = tripRepo.save(trip);
        logger.info("Trip updated successfully: {}", updatedTrip.getLocation());

        return updatedTrip;
    }

    /**
     * Update an existing trip from DTO.
     * @param id the trip ID
     * @param tripDTO the updated trip DTO
     * @return The updated trip
     */
    @Transactional
    public Trip updateTrip(Long id, TripDTO tripDTO) {
        Trip tripDetails = tripDTO.toEntity();
        return updateTrip(id, tripDetails);
    }

    /**
     * Delete a trip by ID
     * @param id the trip ID
     * @throws ResourceNotFoundException if the trip is not found
     * @throws IllegalStateException if the trip has active bookings
     */
    @Transactional
    public void deleteTrip(Long id) {
        logger.info("Deleting trip with ID: {}", id);

        Trip trip = getTripById(id);

        if (trip.getCurrentBookings() > 0) {
            throw new IllegalStateException("Cannot delete trip with active bookings");
        }

        tripRepo.delete(trip);
        logger.info("Trip deleted successfully: {}", trip.getLocation());
    }

    /**
     * Archive a trip instead of deleting it.
     * @param id the trip ID
     * @return The archived trip
     */
    @Transactional
    public Trip archiveTrip(Long id) {
        logger.info("Archiving trip with ID: {}", id);

        Trip trip = getTripById(id);
        trip.setStatus(PublicationStatus.ARCHIVED);

        Trip archivedTrip = tripRepo.save(trip);
        logger.info("Trip archived successfully: {}", archivedTrip.getLocation());

        return archivedTrip;
    }

    // ===== SEARCH AND FILTERING =====

    /**
     * Search trips by multiple criteria.
     * @param location location filter (partial match)
     * @param startDate minimum start date
     * @param endDate maximum end date
     * @param minCertification minimum certification level
     * @param availableOnly only include trips with available spots
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return List of matching trips
     */
    public List<Trip> searchTrips(String location, LocalDate startDate, LocalDate endDate,
        DiveCertification minCertification, boolean availableOnly, BigDecimal minPrice, BigDecimal maxPrice) {

        return tripRepo.searchTrips(location, startDate, endDate, minCertification,
            availableOnly, minPrice, maxPrice);
    }

    /**
     * Get trips with start date between the given dates.
     * @param startDate the start of the date range
     * @param endDate the end of the date range
     * @return List of trips starting between the given dates
     */
    public List<Trip> getTripsByStartDateBetween(LocalDate startDate, LocalDate endDate) {
        return tripRepo.findByStartDateBetween(startDate, endDate);
    }

    /**
     * Get trips by location (case-insensitive partial match).
     * @param location the location to search for
     * @return List of trips with matching location
     */
    public List<Trip> getTripsByLocation(String location) {
        return tripRepo.findByLocationContaining(location);
    }

    /**
     * Get trips that are not fully booked.
     * @return List of trips that are not fully booked
     */
    public List<Trip> getAvailableTrips() {
        return tripRepo.findAvailableTrips();
    }

    /**
     * Get available upcoming trips.
     * @return List of available upcoming trips
     */
    public List<Trip> getAvailableUpcomingTrips() {
        return tripRepo.findAvailableUpcomingTrips();
    }

    /**
     * Get trips accessible with given certification level.
     * @param certification the user's certification level
     * @return List of trips the user can join
     */
    public List<Trip> getTripsForCertificationLevel(DiveCertification certification) {
        return tripRepo.findTripsForCertificationLevel(certification);
    }

    /**
     * Get active trips (currently ongoing).
     * @return List of active trips
     */
    public List<Trip> getActiveTrips() {
        return tripRepo.findActiveTrips();
    }

    /**
     * Get past trips.
     * @return List of past trips
     */
    public List<Trip> getPastTrips() {
        return tripRepo.findPastTrips();
    }

    /**
     * Get trips by price range.
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return List of trips within price range
     */
    public List<Trip> getTripsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return tripRepo.findTripsByPriceRange(minPrice, maxPrice);
    }

    /**
     * Get similar trips based on location and certification.
     * @param trip the reference trip
     * @param limit maximum number of similar trips to return
     * @return List of similar trips
     */
    public List<Trip> getSimilarTrips(Trip trip, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return tripRepo.findSimilarTrips(trip.getId(), trip.getLocation(),
            trip.getMinCertificationRequired(), pageable);
    }

    // ===== BOOKING MANAGEMENT =====

    /**
     * Check if a user with the given certification can book this trip
     * @param tripId the trip ID
     * @param userCertification the user's certification level
     * @return true if the user can book the trip, false otherwise
     */
    public boolean canBookTripWithCertification(Long tripId, DiveCertification userCertification) {
        Trip trip = getTripById(tripId);

        return certificationValidator.validateCertification(
            userCertification,
            trip.getMinCertificationRequired()
        );
    }

    /**
     * Book a trip if user has adequate certification
     * @param tripId the trip ID
     * @param userCertification the user's certification level
     * @return The updated trip
     * @throws IllegalStateException if the trip is fully booked or user lacks required certification
     */
    @Transactional
    public Trip bookTripWithCertification(Long tripId, DiveCertification userCertification) {
        if (!canBookTripWithCertification(tripId, userCertification)) {
            throw new IllegalStateException("User does not have required certification for this trip");
        }

        return bookTrip(tripId);
    }

    /**
     * Book a spot on a trip
     * @param tripId the trip ID
     * @return The updated trip
     * @throws ResourceNotFoundException if the trip is not found
     * @throws IllegalStateException if the trip is fully booked
     */
    @Transactional
    public Trip bookTrip(Long tripId) {
        Trip trip = getTripById(tripId);

        if (trip.isFullyBooked()) {
            throw new IllegalStateException("Trip is fully booked");
        }

        if (trip.isPastTrip()) {
            throw new IllegalStateException("Cannot book a trip that has already ended");
        }

        trip.incrementBookings();
        Trip updatedTrip = tripRepo.save(trip);

        logger.info("Booking added to trip: {} (Current bookings: {}/{})",
            trip.getLocation(), trip.getCurrentBookings(), trip.getCapacity());

        return updatedTrip;
    }

    /**
     * Cancel a booking for a trip
     * @param tripId the trip ID
     * @return The updated trip
     * @throws ResourceNotFoundException if the trip is not found
     */
    @Transactional
    public Trip cancelBooking(Long tripId) {
        Trip trip = getTripById(tripId);

        if (trip.getCurrentBookings() > 0) {
            trip.setCurrentBookings(trip.getCurrentBookings() - 1);
            Trip updatedTrip = tripRepo.save(trip);

            logger.info("Booking cancelled for trip: {} (Current bookings: {}/{})",
                trip.getLocation(), trip.getCurrentBookings(), trip.getCapacity());

            return updatedTrip;
        }

        return trip;
    }

    // ===== ANALYTICS AND STATISTICS =====
    /**
     * Get comprehensive trip statistics.
     * @return Map containing various trip statistics
     */
    public Map<String, Object> getTripStatistics() {
        List<Trip> allTrips = getAllTrips();
        List<Trip> availableTrips = getAvailableTrips();
        List<Trip> upcomingTrips = getUpcomingTrips();

        Integer totalCapacity = tripRepo.getTotalCapacity();
        Integer totalBookings = tripRepo.getTotalBookings();

        totalCapacity = totalCapacity != null ? totalCapacity : 0;
        totalBookings = totalBookings != null ? totalBookings : 0;

        int availableSpots = totalCapacity - totalBookings;
        double utilizationRate = totalCapacity > 0 ? (totalBookings * 100.0) / totalCapacity : 0.0;

        return Map.of(
            "totalTrips", allTrips.size(),
            "availableTrips", availableTrips.size(),
            "upcomingTrips", upcomingTrips.size(),
            "totalCapacity", totalCapacity,
            "totalBookings", totalBookings,
            "availableSpots", availableSpots,
            "utilizationRate", Math.round(utilizationRate * 100.0) / 100.0,
            "averagePrice", tripRepo.getAveragePrice()
        );
    }
    /**
     * Get trip distribution by certification level.
     * @return Map of certification levels to trip counts
     */
    public Map<DiveCertification, Long> getTripsByCertificationLevel() {
        return getAllTrips().stream()
            .collect(Collectors.groupingBy(
                Trip::getMinCertificationRequired,
                Collectors.counting()
            ));
    }
    /**
     * Get trips with low booking rates (less than 50% capacity).
     * @return List of trips with low booking rates
     */
    public List<Trip> getTripsWithLowBookings() {
        return tripRepo.findTripsWithLowBookings();
    }
    /**
     * Get popular destinations.
     * @return List of popular destinations with trip counts
     */
    public List<Object[]> getPopularDestinations() {
        return tripRepo.findPopularDestinations();
    }
    /**
     * Get most booked trips.
     * @param limit maximum number of trips to return
     * @return List of most booked trips
     */
    public List<Trip> getMostBookedTrips(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return tripRepo.findMostBookedTrips(pageable);
    }
    /**
     * Check if a trip is starting soon (within next 7 days).
     * @param trip the trip to check
     * @return true if trip starts within 7 days
     */
    public boolean isTripStartingSoon(Trip trip) {
        LocalDate today = LocalDate.now();
        LocalDate tripStart = trip.getStartDate();

        return !tripStart.isBefore(today) &&
            ChronoUnit.DAYS.between(today, tripStart) <= 7;
    }


    /**
     * Get featured trips for homepage display.
     * FIXED: Uses safe repository method that exists.
     * @param limit maximum number of trips to return
     * @return List of featured trips
     */
    public List<Trip> getFeaturedTrips(int limit) {
        try {
            logger.info("TripService.getFeaturedTrips called with limit: {}", limit);
            List<Trip> featuredTrips = tripRepo.findByFeaturedTrueAndStatusOrderByDisplayOrderAsc(
                PublicationStatus.PUBLISHED);
            List<Trip> limitedTrips = featuredTrips.stream()
                .limit(limit)
                .collect(Collectors.toList());
            logger.info("TripService.getFeaturedTrips returning {} trips", limitedTrips.size()); // For debugging
            for (Trip trip : limitedTrips) {
                logger.info("Featured Trip: ID={}, Location={}, Featured={}, Status={}",
                    trip.getId(), trip.getLocation(), trip.getFeatured(), trip.getStatus());
            }
            return limitedTrips;
        } catch (Exception e) {
            logger.error("Error in TripService.getFeaturedTrips", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get upcoming published trips.
     * FIXED: Uses safe repository method that exists.
     * @return List of upcoming trips
     */
    public List<Trip> getUpcomingTrips() {
        try {
            logger.info("TripService.getUpcomingTrips called");
            List<Trip> upcomingTrips = tripRepo.findUpcomingTrips();
            logger.info("TripService.getUpcomingTrips returning {} trips", upcomingTrips.size());
            return upcomingTrips;
        } catch (Exception e) {
            logger.error("Error in TripService.getUpcomingTrips", e);
            return new ArrayList<>();
        }
    }
    /**
     * Get distinct locations for filter dropdown.
     * @return List of distinct locations
     */
    public List<String> getDistinctLocations() {
        return tripRepo.findDistinctLocations();
    }


    /**
     * Validate trip data before create/update operations.
     * @param trip the trip to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTripData(Trip trip) {
        if (trip.getLocation() == null || trip.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Trip location cannot be empty");
        }
        if (trip.getDescription() == null || trip.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Trip description cannot be empty");
        }
        if (trip.getStartDate() == null) {
            throw new IllegalArgumentException("Trip start date cannot be empty");
        }
        if (trip.getEndDate() == null) {
            throw new IllegalArgumentException("Trip end date cannot be empty");
        }
        if (trip.getEndDate().isBefore(trip.getStartDate())) {
            throw new IllegalArgumentException("Trip end date cannot be before start date");
        }
        if (trip.getCapacity() == null || trip.getCapacity() <= 0) {
            throw new IllegalArgumentException("Trip capacity must be greater than zero");
        }
        if (trip.getMinCertificationRequired() == null) {
            throw new IllegalArgumentException("Minimum certification is required");
        }
        if (trip.getPrice() != null && trip.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Trip price must be greater than zero");
        }
        // Additional business rules
        if (trip.getCapacity() > 50) {
            logger.warn("Trip capacity is very high: {}", trip.getCapacity());
        }
        LocalDate today = LocalDate.now();
        if (trip.getStartDate().isBefore(today.minusDays(1))) {
            logger.warn("Trip start date is in the past: {}", trip.getStartDate());
        }
        // Validate duration
        Integer duration = trip.getDuration();
        if (duration != null && duration > 30) {
            throw new IllegalArgumentException("Trip duration cannot exceed 30 days");
        }
    }
    /**
     * Update trip fields from details object.
     * @param trip the trip to update
     * @param tripDetails the source of new values
     */
    private void updateTripFields(Trip trip, Trip tripDetails) {
        if (tripDetails.getLocation() != null) {
            trip.setLocation(tripDetails.getLocation());
        }
        if (tripDetails.getDescription() != null) {
            trip.setDescription(tripDetails.getDescription());
        }
        if (tripDetails.getShortDescription() != null) {
            trip.setShortDescription(tripDetails.getShortDescription());
        }
        if (tripDetails.getStartDate() != null) {
            trip.setStartDate(tripDetails.getStartDate());
        }
        if (tripDetails.getEndDate() != null) {
            trip.setEndDate(tripDetails.getEndDate());
        }
        if (tripDetails.getImageUrl() != null) {
            trip.setImageUrl(tripDetails.getImageUrl());
        }
        // Handle capacity change - ensure it's not less than current bookings
        if (tripDetails.getCapacity() != null) {
            if (tripDetails.getCapacity() < trip.getCurrentBookings()) {
                throw new IllegalArgumentException("Cannot reduce capacity below current number of bookings");
            }
            trip.setCapacity(tripDetails.getCapacity());
        }
        if (tripDetails.getMinCertificationRequired() != null) {
            trip.setMinCertificationRequired(tripDetails.getMinCertificationRequired());
        }
        if (tripDetails.getPrice() != null) {
            trip.setPrice(tripDetails.getPrice());
        }
        if (tripDetails.getStatus() != null) {
            trip.setStatus(tripDetails.getStatus());
        }
        if (tripDetails.getFeatured() != null) {
            trip.setFeatured(tripDetails.getFeatured());
        }
        if (tripDetails.getDisplayOrder() != null) {
            trip.setDisplayOrder(tripDetails.getDisplayOrder());
        }
        if (tripDetails.getSlug() != null) {
            trip.setSlug(tripDetails.getSlug());
        }
    }
}
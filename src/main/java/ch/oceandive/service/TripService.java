package ch.oceandive.service;

import ch.fhnw.oceandive.dto.TripDTO;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.repository.TripRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Trip savedTrip = tripRepo.save(trip);
        logger.info("Trip created successfully with ID: {}", savedTrip.getId());

        return savedTrip;
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

    // ===== SEARCH AND FILTERING =====

    /**
     * Search trips by multiple criteria.
     * @param location location filter (partial match)
     * @param startDate minimum start date
     * @param endDate maximum end date
     * @param minCertification minimum certification level
     * @param availableOnly only include trips with available spots
     * @return List of matching trips
     */
    public List<Trip> searchTrips(String location, LocalDate startDate, LocalDate endDate,
        DiveCertification minCertification, boolean availableOnly) {

        List<Trip> trips = tripRepo.findAll();

        return trips.stream()
            .filter(trip -> location == null ||
                trip.getLocation().toLowerCase().contains(location.toLowerCase()))
            .filter(trip -> startDate == null ||
                !trip.getStartDate().isBefore(startDate))
            .filter(trip -> endDate == null ||
                !trip.getStartDate().isAfter(endDate))
            .filter(trip -> minCertification == null ||
                certificationValidator.validateCertification(minCertification, trip.getMinCertificationRequired()))
            .filter(trip -> !availableOnly || !trip.isFullyBooked())
            .collect(Collectors.toList());
    }

    /**
     * Get trips with start date after the given date.
     * @param date the reference date
     * @return List of trips starting after the given date
     */
    public List<Trip> getTripsByStartDateAfter(LocalDate date) {
        return tripRepo.findByStartDateAfter(date);
    }

    /**
     * Get trips with start date before the given date.
     * @param date the reference date
     * @return List of trips starting before the given date
     */
    public List<Trip> getTripsByStartDateBefore(LocalDate date) {
        return tripRepo.findByStartDateBefore(date);
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
        return tripRepo.findByLocationContainingIgnoreCase(location);
    }

    /**
     * Get trips that are not fully booked.
     * @return List of trips that are not fully booked
     */
    public List<Trip> getAvailableTrips() {
        return tripRepo.findAvailableTrips();
    }

    /**
     * Get trips by minimum certification level.
     * @param certification the certification level
     * @return List of trips requiring the specified minimum certification
     */
    public List<Trip> getTripsByMinCertification(DiveCertification certification) {
        return tripRepo.findByMinCertificationRequired(certification);
    }

    /**
     * Get upcoming trips (starting after today).
     * @return List of upcoming trips
     */
    public List<Trip> getUpcomingTrips() {
        return tripRepo.findByStartDateAfter(LocalDate.now());
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

        int totalCapacity = allTrips.stream().mapToInt(Trip::getCapacity).sum();
        int totalBookings = allTrips.stream().mapToInt(Trip::getCurrentBookings).sum();
        int availableSpots = totalCapacity - totalBookings;

        double utilizationRate = totalCapacity > 0 ? (totalBookings * 100.0) / totalCapacity : 0.0;

        return Map.of(
            "totalTrips", allTrips.size(),
            "availableTrips", availableTrips.size(),
            "upcomingTrips", upcomingTrips.size(),
            "totalCapacity", totalCapacity,
            "totalBookings", totalBookings,
            "availableSpots", availableSpots,
            "utilizationRate", Math.round(utilizationRate * 100.0) / 100.0
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
     * Get trips expiring soon (starting within the next 30 days).
     * @return List of trips starting soon
     */
    public List<Trip> getTripsStartingSoon() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        return getTripsByStartDateBetween(today, thirtyDaysFromNow);
    }

    /**
     * Get trips with low booking rates (less than 50% capacity).
     * @return List of trips with low booking rates
     */
    public List<Trip> getTripsWithLowBookings() {
        return getAllTrips().stream()
            .filter(trip -> trip.getCapacity() > 0)
            .filter(trip -> (trip.getCurrentBookings() * 100.0 / trip.getCapacity()) < 50.0)
            .collect(Collectors.toList());
    }

    // ===== UTILITY METHODS =====

    /**
     * Convert Trip entity to DTO.
     * @param trip the trip entity
     * @return TripDTO
     */
    public TripDTO convertToDTO(Trip trip) {
        return new TripDTO(
            trip.getId(),
            trip.getLocation(),
            trip.getDescription(),
            trip.getStartDate(),
            trip.getEndDate(),
            trip.getDuration(),
            trip.getCreatedAt(),
            trip.getImageUrl(),
            trip.getCapacity(),
            trip.getCurrentBookings(),
            trip.getMinCertificationRequired()
        );
    }

    /**
     * Convert list of trips to DTOs.
     * @param trips list of trip entities
     * @return List of TripDTOs
     */
    public List<TripDTO> convertToDTOs(List<Trip> trips) {
        return trips.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
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
     * Get trip occupancy percentage.
     * @param trip the trip
     * @return occupancy percentage (0-100)
     */
    public double getTripOccupancyPercentage(Trip trip) {
        if (trip.getCapacity() == 0) {
            return 0.0;
        }
        return (trip.getCurrentBookings() * 100.0) / trip.getCapacity();
    }

    // ===== PRIVATE HELPER METHODS =====

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

        // Additional business rules
        if (trip.getCapacity() > 50) {
            logger.warn("Trip capacity is very high: {}", trip.getCapacity());
        }

        LocalDate today = LocalDate.now();
        if (trip.getStartDate().isBefore(today.minusDays(1))) {
            logger.warn("Trip start date is in the past: {}", trip.getStartDate());
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
    }
}
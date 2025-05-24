package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.repository.TripRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class for managing Trip entities.
 */
@Service
public class TripService {

    private final TripRepo tripRepo;
    private final CertificationValidatorService certificationValidator;

    public TripService(TripRepo tripRepo, CertificationValidatorService certificationValidator) {
        this.tripRepo = tripRepo;
        this.certificationValidator = certificationValidator;
    }

    /**
     * Get all trips.
     * @return List of all trips
     */
    public List<Trip> getAllTrips() {
        return tripRepo.findAll();
    }

    /**
     * Get a trip by ID.
     * @return The trip
     * @throws ResourceNotFoundException if the trip is not found
     */
    public Trip getTripById(Long id) {
        return tripRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
    }

    /**
     * Get trips with start date after the given date.
     * @return List of trips starting after the given date
     */
    public List<Trip> getTripsByStartDateAfter(LocalDate date) {
        return tripRepo.findByStartDateAfter(date);
    }

    /**
     * Get trips with start date before the given date.
     * @return List of trips starting before the given date
     */
    public List<Trip> getTripsByStartDateBefore(LocalDate date) {
        return tripRepo.findByStartDateBefore(date);
    }

    /**
     * Get trips with start date between the given dates.
     * @return List of trips starting between the given dates
     */
    public List<Trip> getTripsByStartDateBetween(LocalDate startDate, LocalDate endDate) {
        return tripRepo.findByStartDateBetween(startDate, endDate);
    }

    /**
     * Get trips by location.
     * @return List of trips with the given location
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
     * Create a new trip.
     * @return The created trip
     */
    @Transactional
    public Trip createTrip(Trip trip) {
        // Validate trip data
        validateTripData(trip);
        
        // Ensure currentBookings starts at 0
        trip.setCurrentBookings(0);
        
        return tripRepo.save(trip);
    }

    /**
     * Update an existing trip.
     * @return The updated trip
     * @throws ResourceNotFoundException if the trip is not found
     */
    @Transactional
    public Trip updateTrip(Long id, Trip tripDetails) {
        Trip trip = getTripById(id);
        
        // Validate trip data
        validateTripData(tripDetails);
        
        // Update fields
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
        
        return tripRepo.save(trip);
    }

    /**
     * Delete a trip by ID
     * @throws ResourceNotFoundException if the trip is not found
     */
    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = getTripById(id);
        tripRepo.delete(trip);
    }
    
    /**
     * Check if a user with the given certification can book this trip
     * @return true if the user can book the trip, false otherwise
     */
    public boolean canBookTripWithCertification(Long tripId, DiveCertification userCertification) {
        Trip trip = getTripById(tripId);
        
        // Use the simple certification validation method
        return certificationValidator.validateCertification(
            userCertification, 
            trip.getMinCertificationRequired()
        );
    }
    
    /**
     * Book a trip if user has adequate certification
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
        return tripRepo.save(trip);
    }
    
    /**
     * Cancel a booking for a trip
     * @return The updated trip
     * @throws ResourceNotFoundException if the trip is not found
     */
    @Transactional
    public Trip cancelBooking(Long tripId) {
        Trip trip = getTripById(tripId);
        
        if (trip.getCurrentBookings() > 0) {
            trip.setCurrentBookings(trip.getCurrentBookings() - 1);
        }
        
        return tripRepo.save(trip);
    }
    
    /**
     * Get trips by minimum certification level.
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
    
    /**
     * Get all trips with pagination.
     * @return Page of trips
     */
    public Page<Trip> getAllTrips(Pageable pageable) {
        return tripRepo.getAllTrips(pageable);
    }
    
    // Validation logic
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
    }
}
package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.repository.TripRepo;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public TripService(TripRepo tripRepo) {
        this.tripRepo = tripRepo;
    }

    /**
     * Get all trips.
     *
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
     * Get trips by name.
     * @return List of trips with the given name
     */
    public List<Trip> getTripsByName(String name) {
        return tripRepo.findByNameContainingIgnoreCase(name);
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
        
        trip.setName(tripDetails.getName());
        trip.setDescription(tripDetails.getDescription());
        trip.setStartDate(tripDetails.getStartDate());
        trip.setEndDate(tripDetails.getEndDate());
        trip.setImageUrl(tripDetails.getImageUrl());
        trip.setCapacity(tripDetails.getCapacity());
        trip.setMinCertificationRequired(tripDetails.getMinCertificationRequired());
        
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
}
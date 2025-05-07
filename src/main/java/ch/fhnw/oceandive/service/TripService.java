package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.dto.admin_side.AdminTripDTO;
import ch.fhnw.oceandive.dto.client_side.ClientTripDTO;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.repository.TripRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling trip operations.
 */
@Service
public class TripService {

    private final TripRepository tripRepository;

    @Autowired
    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    /**
     * Get all active trips (admin view).
     *
     * @return list of all active trips
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> getAllActiveTrips() {
        return tripRepository.findAllByIsActiveTrue().stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active trips for public viewing.
     *
     * @return list of all active trips with limited information
     */
    public List<ClientTripDTO> getAllActiveTripsForPublic() {
        return tripRepository.findAllByIsActiveTrueAndIsDeletedFalse().stream()
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a trip by ID (admin view).
     *
     * @param id the trip ID
     * @return the trip with the specified ID
     * @throws EntityNotFoundException if the trip is not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AdminTripDTO getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + id));
        return convertToTripDTO(trip);
    }

    /**
     * Get a trip by ID for public viewing.
     *
     * @param id the trip ID
     * @return the trip with the specified ID and limited information
     * @throws EntityNotFoundException if the trip is not found or not active
     */
    public ClientTripDTO getTripByIdForPublic(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + id));
        
        if (!trip.isActive() || trip.isDeleted()) {
            throw new EntityNotFoundException("Trip not found with id: " + id);
        }
        
        return convertToPublicTripDTO(trip);
    }

    /**
     * Search for trips by title (admin view).
     *
     * @param title the title to search for
     * @return list of trips matching the title
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> searchTripsByTitle(String title) {
        return tripRepository.findByTripTitleContainingIgnoreCase(title).stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search for trips by title for public viewing.
     *
     * @param title the title to search for
     * @return list of active trips matching the title with limited information
     */
    public List<ClientTripDTO> searchTripsByTitleForPublic(String title) {
        return tripRepository.findByTripTitleContainingIgnoreCase(title).stream()
                .filter(trip -> trip.isActive() && !trip.isDeleted())
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Search for trips by location (admin view).
     *
     * @param location the location to search for
     * @return list of trips matching the location
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> searchTripsByLocation(String location) {
        return tripRepository.findByLocationContainingIgnoreCase(location).stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search for trips by location for public viewing.
     *
     * @param location the location to search for
     * @return list of active trips matching the location with limited information
     */
    public List<ClientTripDTO> searchTripsByLocationForPublic(String location) {
        return tripRepository.findByLocationContainingIgnoreCase(location).stream()
                .filter(trip -> trip.isActive() && !trip.isDeleted())
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trips within a date range (admin view).
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of trips within the date range
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> getTripsByDateRange(LocalDate startDate, LocalDate endDate) {
        return tripRepository.findByStartDateBetween(startDate, endDate).stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trips within a date range for public viewing.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of active trips within the date range with limited information
     */
    public List<ClientTripDTO> getTripsByDateRangeForPublic(LocalDate startDate, LocalDate endDate) {
        return tripRepository.findByStartDateBetween(startDate, endDate).stream()
                .filter(trip -> trip.isActive() && !trip.isDeleted())
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trips by required certification (admin view).
     *
     * @param certification the required certification
     * @return list of trips requiring the specified certification
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> getTripsByRequiredCertification(DiveCertification certification) {
        return tripRepository.findByRequiredCertification(certification).stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trips by required certification for public viewing.
     *
     * @param certification the required certification
     * @return list of active trips requiring the specified certification with limited information
     */
    public List<ClientTripDTO> getTripsByRequiredCertificationForPublic(DiveCertification certification) {
        return tripRepository.findByRequiredCertification(certification).stream()
                .filter(trip -> trip.isActive() && !trip.isDeleted())
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new trip.
     *
     * @param adminTripDTO the trip data
     * @return the created trip
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public AdminTripDTO createTrip(AdminTripDTO adminTripDTO) {
        Trip trip = convertToEntity(adminTripDTO);
        trip.setActive(true);
        trip.setDeleted(false);
        Trip savedTrip = tripRepository.save(trip);
        return convertToTripDTO(savedTrip);
    }

    /**
     * Update an existing trip.
     *
     * @param id the ID of the trip to update
     * @param adminTripDTO the updated trip data
     * @return the updated trip
     * @throws EntityNotFoundException if the trip is not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public AdminTripDTO updateTrip(Long id, AdminTripDTO adminTripDTO) {
        Trip existingTrip = tripRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + id));
        
        updateTripFromDTO(existingTrip, adminTripDTO);
        Trip updatedTrip = tripRepository.save(existingTrip);
        return convertToTripDTO(updatedTrip);
    }

    /**
     * Soft delete a trip.
     *
     * @param id the ID of the trip to delete
     * @throws EntityNotFoundException if the trip is not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + id));
        
        trip.setActive(false);
        trip.setDeleted(true);
        tripRepository.save(trip);
    }

    /**
     * Permanently delete a trip.
     *
     * @param id the ID of the trip to permanently delete
     * @throws EntityNotFoundException if the trip is not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void permanentlyDeleteTrip(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new EntityNotFoundException("Trip not found with id: " + id);
        }
        tripRepository.deleteById(id);
    }

    // Helper methods for entity-DTO conversion

    private AdminTripDTO convertToTripDTO(Trip trip) {
        return new AdminTripDTO(
                trip.getId(),
                trip.getTripTitle(),
                trip.getDescription(),
                trip.getLocation(),
                trip.getPrice(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getDuration(),
                trip.getCapacity(),
                trip.getAvailableSpots(),
                trip.getRequiredCertification(),
                trip.getProvidedCertification(),
                trip.getIncludedItems(),
                trip.isActive(),
                trip.isDeleted(),
                trip.getImageUrl()
        );
    }
    private ClientTripDTO convertToPublicTripDTO(Trip trip) {
        return new ClientTripDTO(
                trip.getId(),
                trip.getTripTitle(),
                trip.getDescription(),
                trip.getLocation(),
                trip.getPrice(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getDuration(),
                trip.getAvailableSpots(),
                trip.getRequiredCertification(),
                trip.getIncludedItems(),
                trip.getImageUrl()
        );
    }

    private Trip convertToEntity(AdminTripDTO adminTripDTO) {
        Trip trip = new Trip();
        if (adminTripDTO.getId() != null) {
            trip.setId(adminTripDTO.getId());
        }
        mapping(adminTripDTO, trip);
        return trip;
    }
    private void mapping(AdminTripDTO adminTripDTO, Trip trip) {
        trip.setTripTitle(adminTripDTO.getTripTitle());
        trip.setDescription(adminTripDTO.getDescription());
        trip.setLocation(adminTripDTO.getLocation());
        trip.setPrice(adminTripDTO.getPrice());
        trip.setStartDate(adminTripDTO.getStartDate());
        trip.setEndDate(adminTripDTO.getEndDate());
        trip.setDuration(adminTripDTO.getDuration());
        trip.setCapacity(adminTripDTO.getCapacity());
        trip.setAvailableSpots(adminTripDTO.getAvailableSpots());
        trip.setRequiredCertification(adminTripDTO.getRequiredCertification());
        trip.setProvidedCertification(adminTripDTO.getProvidedCertification());
        trip.setIncludedItems(new ArrayList<>(adminTripDTO.getIncludedItems()));
        trip.setActive(adminTripDTO.getIsActive());
        trip.setDeleted(adminTripDTO.getIsDeleted());
        trip.setImageUrl(adminTripDTO.getImageUrl());
    }

    private void updateTripFromDTO(Trip trip, AdminTripDTO adminTripDTO) {
        mapping(adminTripDTO, trip);
    }
}

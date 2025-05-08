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
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> getAllActiveTrips() {
        return tripRepository.findAllByIsActiveTrue().stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active trips for public viewing.
     */
    public List<ClientTripDTO> getAllActiveTripsForPublic() {
        return tripRepository.findAllByIsActiveTrueAndIsDeletedFalse().stream()
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a trip by ID (admin view).
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AdminTripDTO getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + id));
        return convertToTripDTO(trip);
    }

    /**
     * Get a trip by ID for public viewing.
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
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> searchTripsByTitle(String title) {
        return tripRepository.findByTripTitleContainingIgnoreCase(title).stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search for trips by title for public viewing.
     */
    public List<ClientTripDTO> searchTripsByTitleForPublic(String title) {
        return tripRepository.findByTripTitleContainingIgnoreCase(title).stream()
                .filter(trip -> trip.isActive() && !trip.isDeleted())
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Search for trips by location (admin view).
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> searchTripsByLocation(String location) {
        return tripRepository.findByLocationContainingIgnoreCase(location).stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search for trips by location for public viewing.
     */
    public List<ClientTripDTO> searchTripsByLocationForPublic(String location) {
        return tripRepository.findByLocationContainingIgnoreCase(location).stream()
                .filter(trip -> trip.isActive() && !trip.isDeleted())
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trips within a date range (admin view).
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> getTripsByDateRange(LocalDate startDate, LocalDate endDate) {
        return tripRepository.findByStartDateBetween(startDate, endDate).stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trips within a date range for public viewing.
     */
    public List<ClientTripDTO> getTripsByDateRangeForPublic(LocalDate startDate, LocalDate endDate) {
        return tripRepository.findByStartDateBetween(startDate, endDate).stream()
                .filter(trip -> trip.isActive() && !trip.isDeleted())
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trips by required certification (admin view).
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AdminTripDTO> getTripsByRequiredCertification(DiveCertification certification) {
        return tripRepository.findByRequiredCertification(certification).stream()
                .map(this::convertToTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trips by required certification for public viewing.
     */
    public List<ClientTripDTO> getTripsByRequiredCertificationForPublic(DiveCertification certification) {
        return tripRepository.findByRequiredCertification(certification).stream()
                .filter(trip -> trip.isActive() && !trip.isDeleted())
                .map(this::convertToPublicTripDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new trip.
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

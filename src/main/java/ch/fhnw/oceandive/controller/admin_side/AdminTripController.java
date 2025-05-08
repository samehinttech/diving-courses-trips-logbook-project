package ch.fhnw.oceandive.controller.admin_side;

import ch.fhnw.oceandive.dto.admin_side.AdminTripDTO;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.service.TripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing Trip resources by admin.
 */
@RestController("adminTripController")
@RequestMapping("/api/admin/trips")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminTripController {

    private final TripService tripService;

    @Autowired
    public AdminTripController(TripService tripService) {
        this.tripService = tripService;
    }

    /**
     * GET /api/admin/trips : Get all active trips.
     */
    @GetMapping
    public ResponseEntity<List<AdminTripDTO>> getAllActiveTrips() {
        List<AdminTripDTO> trips = tripService.getAllActiveTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/admin/trips/{id} : Get the trip with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminTripDTO> getTripById(@PathVariable Long id) {
        AdminTripDTO trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    /**
     * GET /api/admin/trips/search: Search for trips by title.
     */
    @GetMapping("/search")
    public ResponseEntity<List<AdminTripDTO>> searchTripsByTitle(@RequestParam String title) {
        List<AdminTripDTO> trips = tripService.searchTripsByTitle(title);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/admin/trips/search-location : Search for trips by location.
     */
    @GetMapping("/search-location")
    public ResponseEntity<List<AdminTripDTO>> searchTripsByLocation(@RequestParam String location) {
        List<AdminTripDTO> trips = tripService.searchTripsByLocation(location);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/admin/trips/date-range : Get trips within a date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<AdminTripDTO>> getTripsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AdminTripDTO> trips = tripService.getTripsByDateRange(startDate, endDate);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/admin/trips/certification : Get trips by certification.
     */
    @GetMapping("/certification")
    public ResponseEntity<List<AdminTripDTO>> getTripsByCertification(
            @RequestParam DiveCertification certification) {
        List<AdminTripDTO> trips = tripService.getTripsByRequiredCertification(certification);
        return ResponseEntity.ok(trips);
    }

    /**
     * POST /api/admin/trips: Create a new trip.
     */
    @PostMapping
    public ResponseEntity<AdminTripDTO> createTrip(@Valid @RequestBody AdminTripDTO adminTripDTO) {
        AdminTripDTO createdTrip = tripService.createTrip(adminTripDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTrip);
    }

    /**
     * PUT /api/admin/trips/{id}: Update an existing trip.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminTripDTO> updateTrip(
            @PathVariable Long id, @Valid @RequestBody AdminTripDTO adminTripDTO) {
        AdminTripDTO updatedTrip = tripService.updateTrip(id, adminTripDTO);
        return ResponseEntity.ok(updatedTrip);
    }

    /**
     * DELETE /api/admin/trips/{id} : Delete a trip (soft delete).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/admin/trips/{id}/permanent : Permanently delete a trip.
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteTrip(@PathVariable Long id) {
        tripService.permanentlyDeleteTrip(id);
        return ResponseEntity.noContent().build();
    }
}
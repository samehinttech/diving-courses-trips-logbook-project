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
     *
     * @return the ResponseEntity with status 200 (OK) and the list of active trips in body
     */
    @GetMapping
    public ResponseEntity<List<AdminTripDTO>> getAllActiveTrips() {
        List<AdminTripDTO> trips = tripService.getAllActiveTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/admin/trips/{id} : Get the trip with the specified ID.
     * @return the ResponseEntity with status 200 (OK) and the trip in body,
     *         or with status 404 (Not Found) if the trip is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminTripDTO> getTripById(@PathVariable Long id) {
        AdminTripDTO trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    /**
     * GET /api/admin/trips/search: Search for trips by title.
     * @return the ResponseEntity with status 200 (OK) and the list of matching trips in body
     */
    @GetMapping("/search")
    public ResponseEntity<List<AdminTripDTO>> searchTripsByTitle(@RequestParam String title) {
        List<AdminTripDTO> trips = tripService.searchTripsByTitle(title);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/admin/trips/search-location : Search for trips by location.
     * @return the ResponseEntity with status 200 (OK) and the list of matching trips in body
     */
    @GetMapping("/search-location")
    public ResponseEntity<List<AdminTripDTO>> searchTripsByLocation(@RequestParam String location) {
        List<AdminTripDTO> trips = tripService.searchTripsByLocation(location);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/admin/trips/date-range : Get trips within a date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return the ResponseEntity with status 200 (OK) and the list of trips in body
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
     *
     * @param certification the certification to search for
     * @return the ResponseEntity with status 200 (OK) and the list of trips in body
     */
    @GetMapping("/certification")
    public ResponseEntity<List<AdminTripDTO>> getTripsByCertification(
            @RequestParam DiveCertification certification) {
        List<AdminTripDTO> trips = tripService.getTripsByRequiredCertification(certification);
        return ResponseEntity.ok(trips);
    }

    /**
     * POST /api/admin/trips: Create a new trip.
     * @return the ResponseEntity with status 201 (Created) and the new trip in body
     */
    @PostMapping
    public ResponseEntity<AdminTripDTO> createTrip(@Valid @RequestBody AdminTripDTO adminTripDTO) {
        AdminTripDTO createdTrip = tripService.createTrip(adminTripDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTrip);
    }

    /**
     * PUT /api/admin/trips/{id}: Update an existing trip.
     * @return the ResponseEntity with status 200 (OK) and the updated trip in body,
     *         or with status 404 (Not Found) if the trip is not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminTripDTO> updateTrip(
            @PathVariable Long id, @Valid @RequestBody AdminTripDTO adminTripDTO) {
        AdminTripDTO updatedTrip = tripService.updateTrip(id, adminTripDTO);
        return ResponseEntity.ok(updatedTrip);
    }

    /**
     * DELETE /api/admin/trips/{id} : Delete a trip (soft delete).
     * @return the ResponseEntity with status 204 (No Content),
     *         or with status 404 (Not Found) if the trip is not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/admin/trips/{id}/permanent : Permanently delete a trip.
     * @return the ResponseEntity with status 204 (No Content),
     *         or with status 404 (Not Found) if the trip is not found
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteTrip(@PathVariable Long id) {
        tripService.permanentlyDeleteTrip(id);
        return ResponseEntity.noContent().build();
    }
}
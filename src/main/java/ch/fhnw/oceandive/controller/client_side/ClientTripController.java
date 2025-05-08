package ch.fhnw.oceandive.controller.client_side;

import ch.fhnw.oceandive.dto.client_side.ClientTripDTO;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for public Trip view.
 * are accessible to all users, including unauthenticated ones.
 */
@RestController("clientTripController")
@RequestMapping("/api/trips")
public class ClientTripController {

    private final TripService tripService;

    @Autowired
    public ClientTripController(TripService tripService) {
        this.tripService = tripService;
    }

    /**
     * GET /api/trips : Get all active trips for public access.
     */
    @GetMapping
    public ResponseEntity<List<ClientTripDTO>> getAllActiveTrips() {
        List<ClientTripDTO> trips = tripService.getAllActiveTripsForPublic();
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/trips/{id} : Get the trip with the specified ID for public access.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientTripDTO> getTripById(@PathVariable Long id) {
        ClientTripDTO trip = tripService.getTripByIdForPublic(id);
        return ResponseEntity.ok(trip);
    }

    /**
     * GET /api/trips/search: Search for trips by title for public access.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClientTripDTO>> searchTripsByTitle(@RequestParam String title) {
        List<ClientTripDTO> trips = tripService.searchTripsByTitleForPublic(title);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/trips/search-location: Search for trips by location for public access.
     */
    @GetMapping("/search-location")
    public ResponseEntity<List<ClientTripDTO>> searchTripsByLocation(@RequestParam String location) {
        List<ClientTripDTO> trips = tripService.searchTripsByLocationForPublic(location);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/trips/date-range: Get trips within a date range for public access.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<ClientTripDTO>> getTripsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ClientTripDTO> trips = tripService.getTripsByDateRangeForPublic(startDate, endDate);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/trips/certification: Get trips by certification for public access.
     */
    @GetMapping("/certification")
    public ResponseEntity<List<ClientTripDTO>> getTripsByCertification(
            @RequestParam DiveCertification certification) {
        List<ClientTripDTO> trips = tripService.getTripsByRequiredCertificationForPublic(certification);
        return ResponseEntity.ok(trips);
    }
}
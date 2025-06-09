package ch.oceandive.controller.rest;

import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.service.TripService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing trips.
 */
@RestController
@RequestMapping("/api")
public class TripController {

    private final TripService tripService;

    @Autowired
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    /**
     * GET /api/trips: Get all trips.
     * Public endpoint is accessible to all users.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of trips in body
     */
    @GetMapping("/trips")
    public ResponseEntity<List<Trip>> getAllTrips() {
        List<Trip> trips = tripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/trips/{id}: Get the trip with the specified ID.
     * Public endpoint is accessible to all users.
     *
     * @param id the ID of the trip to retrieve
     * @return the ResponseEntity with status 200 (OK) and the trip in body,
     *         or with status 404 (Not Found) if the trip is not found
     */
    @GetMapping("/trips/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable Long id) {
        Trip trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    /**
     * GET /api/trips/upcoming: Get all upcoming trips (starting from today).
     * Public endpoint is accessible to all users.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of upcoming trips in body,
     *         or with status 200 (OK) and "coming soon" message if no upcoming trips are found
     */
    @GetMapping("/trips/upcoming")
    public ResponseEntity<?> getUpcomingTrips() {
        List<Trip> trips = tripService.getTripsByStartDateAfter(LocalDate.now());
        if (trips.isEmpty()) {
            return ResponseEntity.ok("coming soon");
        }
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/trips/available: Get all available trips (not fully booked).
     * Public endpoint is accessible to all users.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of available trips in body
     */
    @GetMapping("/trips/available")
    public ResponseEntity<List<Trip>> getAvailableTrips() {
        List<Trip> trips = tripService.getAvailableTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/trips/location/{location}: Get trips by location(which is used as the trip name).
     * Public endpoint is accessible to all users.
     *
     * @param location the location to search for
     * @return the ResponseEntity with status 200 (OK) and the list of trips in body
     */
    @GetMapping("/trips/location/{location}")
    public ResponseEntity<List<Trip>> getTripsByLocation(@PathVariable String location) {
        List<Trip> trips = tripService.getTripsByLocation(location);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET /api/trips/date-range: Get trips within a date range.
     * Public endpoint is accessible to all users.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return the ResponseEntity with status 200 (OK) and the list of trips in body
     */
    @GetMapping("/trips/date-range")
    public ResponseEntity<List<Trip>> getTripsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Trip> trips = tripService.getTripsByStartDateBetween(startDate, endDate);
        return ResponseEntity.ok(trips);
    }
}

package ch.oceandive.controller.rest;

import ch.oceandive.dto.TripDTO;
import ch.oceandive.model.DiveCertification;
import ch.oceandive.model.Trip;
import ch.oceandive.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/*
  * REST API controller for trip management operations.
  * That Part of the project is not yet fully implemented.
 */

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private static final Logger logger = LoggerFactory.getLogger(TripController.class);
    private final TripService tripService;

    @Autowired
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }
//   // Endpoint to get all trips for public access
    @GetMapping
    public ResponseEntity<?> getAllTrips(
        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Include unpublished trips") @RequestParam(defaultValue = "false") boolean includeUnpublished,
        @Parameter(description = "Enable pagination") @RequestParam(defaultValue = "false") boolean paginated) {
        logger.debug("Getting all trips - page: {}, size: {}, includeUnpublished: {}",
            page, size, includeUnpublished);
        try {
            if (paginated) {
                Pageable pageable = PageRequest.of(page, size);
                Page<Trip> trips = tripService.getAllTrips(pageable);
                return ResponseEntity.ok(trips);
            } else {
                List<Trip> trips = includeUnpublished ?
                    tripService.getAllTripsIncludingUnpublished() :
                    tripService.getAllTrips();
                return ResponseEntity.ok(trips);
            }
        } catch (Exception e) {
            logger.error("Error getting trips", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving trips: " + e.getMessage());
        }
    }

    // Endpoint to get a trip by its ID for Admin level
    @GetMapping("/{id}")
    public ResponseEntity<?> getTripById(@Parameter(description = "Trip ID") @PathVariable Long id) {
        logger.debug("Getting trip by ID: {}", id);

        try {
            Trip trip = tripService.getTripById(id);
            return ResponseEntity.ok(trip);
        } catch (Exception e) {
            logger.error("Error getting trip by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Trip not found with ID: " + id);
        }
    }

    // End point to get a trip by its slug (stored in the database) for Admin Level
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getTripBySlug(@Parameter(description = "Trip slug") @PathVariable String slug) {
        logger.debug("Getting trip by slug: {}", slug);
        try {
            Trip trip = tripService.getTripBySlug(slug);
            return ResponseEntity.ok(trip);
        } catch (Exception e) {
            logger.error("Error getting trip by slug: {}", slug, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Trip not found with slug: " + slug);
        }
    }

    // Endpoint to create a new trip for admin Level
    @PostMapping
    public ResponseEntity<?> createTrip(@Valid @RequestBody TripDTO tripDTO) {
        logger.info("Creating new trip: {}", tripDTO.getLocation());
        try {
            Trip createdTrip = tripService.createTrip(tripDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTrip);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating trip", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating trip", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating trip: " + e.getMessage());
        }
    }

    // Endpoint to update an existing trip in the ADMIN Level defined in the other layers and the SecurityConfig
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrip(
        @Parameter(description = "Trip ID") @PathVariable Long id,
        @Valid @RequestBody TripDTO tripDTO) {
        logger.info("Updating trip with ID: {}", id);
        try {
            Trip updatedTrip = tripService.updateTrip(id, tripDTO);
            return ResponseEntity.ok(updatedTrip);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating trip", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating trip with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Trip not found with ID: " + id);
        }
    }

    // Endpoint to delete an existing trip in the ADMIN Level defined in the other layers and the SecurityConfig
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrip(@Parameter(description = "Trip ID") @PathVariable Long id) {
        logger.info("Deleting trip with ID: {}", id);

        try {
            tripService.deleteTrip(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            logger.error("Cannot delete trip with active bookings", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Cannot delete trip with active bookings");
        } catch (Exception e) {
            logger.error("Error deleting trip with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Trip not found with ID: " + id);
        }
    }

    // Endpoint to archive a trip to the ADMIN Level defined in the other layers and the SecurityConfig
    @PutMapping("/{id}/archive")
    public ResponseEntity<?> archiveTrip(@Parameter(description = "Trip ID") @PathVariable Long id) {
        logger.info("Archiving trip with ID: {}", id);

        try {
            Trip archivedTrip = tripService.archiveTrip(id);
            return ResponseEntity.ok(archivedTrip);
        } catch (Exception e) {
            logger.error("Error archiving trip with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Trip not found with ID: " + id);
        }
    }

    // Endpoint to search trips with various filters for public access
    @GetMapping("/search")
    public ResponseEntity<List<Trip>> searchTrips(
        @Parameter(description = "Location filter") @RequestParam(required = false) String location,
        @Parameter(description = "Start date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "End date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @Parameter(description = "Certification level filter") @RequestParam(required = false) DiveCertification certification,
        @Parameter(description = "Available spots only") @RequestParam(defaultValue = "false") boolean availableOnly,
        @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
        @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice) {

        logger.debug("Searching trips with filters - location: {}, startDate: {}, endDate: {}, " +
                "certification: {}, availableOnly: {}, minPrice: {}, maxPrice: {}",
            location, startDate, endDate, certification, availableOnly, minPrice, maxPrice);

        List<Trip> trips = tripService.searchTrips(location, startDate, endDate,
            certification, availableOnly, minPrice, maxPrice);
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get upcoming trips for public access
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingTrips() {
        logger.debug("Getting upcoming trips");

        List<Trip> trips = tripService.getUpcomingTrips();
        if (trips.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "coming soon"));
        }
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get available trips, with an option for upcoming trips only
    @GetMapping("/available")
    public ResponseEntity<List<Trip>> getAvailableTrips(
        @Parameter(description = "Upcoming trips only") @RequestParam(defaultValue = "false") boolean upcomingOnly) {
        logger.debug("Getting available trips - upcomingOnly: {}", upcomingOnly);
        List<Trip> trips = upcomingOnly ?
            tripService.getAvailableUpcomingTrips() :
            tripService.getAvailableTrips();
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get featured trips, with a limit on the number of results (Featured are (Special offers, Seasonal trips, etc.))
    @GetMapping("/featured")
    public ResponseEntity<List<Trip>> getFeaturedTrips(
        @Parameter(description = "Maximum results") @RequestParam(defaultValue = "6") int limit) {
        logger.debug("Getting featured trips - limit: {}", limit);
        List<Trip> trips = tripService.getFeaturedTrips(limit);
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get trips by location
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Trip>> getTripsByLocation(
        @Parameter(description = "Location name") @PathVariable String location) {
        logger.debug("Getting trips by location: {}", location);
        List<Trip> trips = tripService.getTripsByLocation(location);
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get trips by date range public access
    @GetMapping("/date-range")
    public ResponseEntity<List<Trip>> getTripsByDateRange(
        @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.debug("Getting trips by date range: {} to {}", startDate, endDate);

        List<Trip> trips = tripService.getTripsByStartDateBetween(startDate, endDate);
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get trips for specific certification level public access
    @GetMapping("/certification/{level}")
    public ResponseEntity<List<Trip>> getTripsForCertification(
        @Parameter(description = "Certification level") @PathVariable DiveCertification level) {
        logger.debug("Getting trips for certification level: {}", level);

        List<Trip> trips = tripService.getTripsForCertificationLevel(level);
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get trips by price range public access
    @GetMapping("/price-range")
    public ResponseEntity<List<Trip>> getTripsByPriceRange(
        @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
        @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice) {
        logger.debug("Getting trips by price range: {} to {}", minPrice, maxPrice);
        List<Trip> trips = tripService.getTripsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get similar trips based on a given trip ID in the backend
    @GetMapping("/{id}/similar")
    public ResponseEntity<?> getSimilarTrips(
        @Parameter(description = "Trip ID") @PathVariable Long id,
        @Parameter(description = "Maximum results") @RequestParam(defaultValue = "5") int limit) {
        logger.debug("Getting similar trips for trip ID: {}, limit: {}", id, limit);
        try {
            Trip trip = tripService.getTripById(id);
            List<Trip> similarTrips = tripService.getSimilarTrips(trip, limit);
            return ResponseEntity.ok(similarTrips);
        } catch (Exception e) {
            logger.error("Error getting similar trips for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Trip not found with ID: " + id);
        }
    }

    // Endpoint to book a trip, with the required certification level
    @PostMapping("/{id}/book")
    public ResponseEntity<?> bookTrip(
        @Parameter(description = "Trip ID") @PathVariable Long id,
        @Parameter(description = "User certification") @RequestParam(required = false) DiveCertification certification) {
        logger.info("Booking trip with ID: {}, certification: {}", id, certification);

        try {
            Trip bookedTrip;
            if (certification != null) {
                bookedTrip = tripService.bookTripWithCertification(id, certification);
            } else {
                bookedTrip = tripService.bookTrip(id);
            }
            return ResponseEntity.ok(bookedTrip);
        } catch (IllegalStateException e) {
            logger.error("Cannot book trip", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error booking trip with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Trip not found with ID: " + id);
        }
    }

    // Endpoint to cancel a trip booking
    @PostMapping("/{id}/cancel-booking")
    public ResponseEntity<?> cancelBooking(@Parameter(description = "Trip ID") @PathVariable Long id) {
        logger.info("Cancelling booking for trip with ID: {}", id);

        try {
            Trip updatedTrip = tripService.cancelBooking(id);
            return ResponseEntity.ok(updatedTrip);
        } catch (Exception e) {
            logger.error("Error cancelling booking for trip with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Trip not found with ID: " + id);
        }
    }

    // Endpoint to check if a user can book a trip based on their certification level
    @GetMapping("/{id}/can-book")
    public ResponseEntity<?> canBookTrip(
        @Parameter(description = "Trip ID") @PathVariable Long id,
        @Parameter(description = "User certification") @RequestParam DiveCertification certification) {
        logger.debug("Checking if user can book trip ID: {} with certification: {}", id, certification);

        try {
            boolean canBook = tripService.canBookTripWithCertification(id, certification);
            return ResponseEntity.ok(Map.of("canBook", canBook));
        } catch (Exception e) {
            logger.error("Error checking booking eligibility for trip ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Trip not found with ID: " + id);
        }
    }

    // Endpoint to get trip statistics and for ADMIN Dashboard
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTripStatistics() {
        logger.debug("Getting trip statistics");

        Map<String, Object> statistics = tripService.getTripStatistics();
        return ResponseEntity.ok(statistics);
    }

    // Endpoint to get popular destinations for analytics  for ADMIN Dashboard
    @GetMapping("/analytics/popular-destinations")
    public ResponseEntity<List<Map<String, Object>>> getPopularDestinations() {
        logger.debug("Getting popular destinations");

        List<Object[]> results = tripService.getPopularDestinations();
        List<Map<String, Object>> destinations = results.stream()
            .map(result -> Map.of(
                "location", result[0],
                "tripCount", result[1]
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(destinations);
    }

    // Endpoints to get the most booked trips  for ADMIN Dashboard
    @GetMapping("/analytics/most-booked")
    public ResponseEntity<List<Trip>> getMostBookedTrips(
        @Parameter(description = "Maximum results") @RequestParam(defaultValue = "10") int limit) {
        logger.debug("Getting most booked trips - limit: {}", limit);

        List<Trip> trips = tripService.getMostBookedTrips(limit);
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get trips with low bookings for analytics  for ADMIN Dashboard
    @GetMapping("/analytics/low-bookings")
    public ResponseEntity<List<Trip>> getTripsWithLowBookings() {
        logger.debug("Getting trips with low bookings");

        List<Trip> trips = tripService.getTripsWithLowBookings();
        return ResponseEntity.ok(trips);
    }

    // Endpoint to get distinct locations for trip filtering
    @GetMapping("/locations")
    public ResponseEntity<List<String>> getDistinctLocations() {
        logger.debug("Getting distinct locations");

        List<String> locations = tripService.getDistinctLocations();
        return ResponseEntity.ok(locations);
    }

    // Endpoints to get active and past trips for ADMIN Dashboard
    @GetMapping("/active")
    public ResponseEntity<List<Trip>> getActiveTrips() {
        logger.debug("Getting active trips");

        List<Trip> trips = tripService.getActiveTrips();
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/past")
    public ResponseEntity<List<Trip>> getPastTrips(
        @Parameter(description = "Maximum results") @RequestParam(defaultValue = "20") int limit) {
        logger.debug("Getting past trips - limit: {}", limit);

        List<Trip> trips = tripService.getPastTrips();
        return ResponseEntity.ok(trips.stream().limit(limit).collect(Collectors.toList()));
    }
}
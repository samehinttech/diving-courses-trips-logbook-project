package ch.oceandive.controller.web;

import ch.oceandive.dto.TripDTO;
import ch.oceandive.utils.PublicationStatus;
import ch.oceandive.utils.DiveCertification;
import ch.oceandive.model.Trip;
import ch.oceandive.service.TripService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * MVC Controller for Trip management.
 * Handles web UI interactions for trip-related operations.
 */
@Controller
@RequestMapping("/trips")
public class TripMVCController {

  private static final Logger logger = LoggerFactory.getLogger(TripMVCController.class);
  private final TripService tripService;

  @Autowired
  public TripMVCController(TripService tripService) {
    this.tripService = tripService;
  }

  // ===== PUBLIC TRIP PAGES =====

  /**
   * Display all published trips.
   * GET /trips
   */
  @GetMapping
  public String listTrips(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false) DiveCertification certification,
      @RequestParam(defaultValue = "false") boolean availableOnly,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      Model model) {

    logger.debug("Listing trips - page: {}, size: {}, filters applied", page, size);

    try {
      List<Trip> trips;
      if (hasFilters(location, startDate, endDate, certification, availableOnly, minPrice, maxPrice)) {
        trips = tripService.searchTrips(location, startDate, endDate, certification,
            availableOnly, minPrice, maxPrice);
        model.addAttribute("searchApplied", true);
      } else {
        Pageable pageable = PageRequest.of(page, size);
        Page<Trip> tripPage = tripService.getAllTrips(pageable);
        trips = tripPage.getContent();
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tripPage.getTotalPages());
        model.addAttribute("totalItems", tripPage.getTotalElements());
        model.addAttribute("hasNext", tripPage.hasNext());
        model.addAttribute("hasPrevious", tripPage.hasPrevious());
      }

      // Add trips and filter options to the model
      model.addAttribute("trips", trips);
      model.addAttribute("locations", tripService.getDistinctLocations());
      model.addAttribute("certifications", DiveCertification.values());

      // Preserve filter values
      model.addAttribute("selectedLocation", location);
      model.addAttribute("selectedStartDate", startDate);
      model.addAttribute("selectedEndDate", endDate);
      model.addAttribute("selectedCertification", certification);
      model.addAttribute("selectedAvailableOnly", availableOnly);
      model.addAttribute("selectedMinPrice", minPrice);
      model.addAttribute("selectedMaxPrice", maxPrice);

      return "trips/list";

    } catch (Exception e) {
      logger.error("Error listing trips", e);
      model.addAttribute("error", "Error loading trips. Please try again.");
      return "trips/list";
    }
  }

  /**
   * Display trip details.
   * GET /trips/{id}
   */
  @GetMapping("/{id}")
  public String viewTrip(@PathVariable Long id, Model model) {
    logger.debug("Viewing trip details for ID: {}", id);

    try {
      Trip trip = tripService.getTripById(id);
      List<Trip> similarTrips = tripService.getSimilarTrips(trip, 4);

      model.addAttribute("trip", trip);
      model.addAttribute("similarTrips", similarTrips);
      model.addAttribute("certifications", DiveCertification.values());

      // Add booking information
      model.addAttribute("isFullyBooked", trip.isFullyBooked());
      model.addAttribute("availableSpots", trip.getAvailableSpots());
      model.addAttribute("bookingPercentage", Math.round(trip.getBookingPercentage()));
      model.addAttribute("isUpcoming", trip.isUpcomingTrip());
      model.addAttribute("isPast", trip.isPastTrip());
      model.addAttribute("isActive", trip.isActiveTrip());

      return "trips/detail";

    } catch (Exception e) {
      logger.error("Error viewing trip with ID: {}", id, e);
      model.addAttribute("error", "Trip not found.");
      return "error/404";
    }
  }

  /**
   * Display trip details by slug.
   * GET /trips/slug/{slug}
   */
  @GetMapping("/slug/{slug}")
  public String viewTripBySlug(@PathVariable String slug, Model model) {
    logger.debug("Viewing trip details for slug: {}", slug);

    try {
      Trip trip = tripService.getTripBySlug(slug);
      return "redirect:/trips/" + trip.getId();
    } catch (Exception e) {
      logger.error("Error viewing trip with slug: {}", slug, e);
      model.addAttribute("error", "Trip not found.");
      return "error/404";
    }
  }

  /**
   * Display upcoming trips.
   * GET /trips/upcoming
   */
  @GetMapping("/upcoming")
  public String upcomingTrips(Model model) {
    logger.debug("Displaying upcoming trips");

    try {
      List<Trip> upcomingTrips = tripService.getUpcomingTrips();
      List<Trip> availableTrips = tripService.getAvailableUpcomingTrips();

      model.addAttribute("upcomingTrips", upcomingTrips);
      model.addAttribute("availableTrips", availableTrips);
      model.addAttribute("pageTitle", "Upcoming Diving Trips");

      if (upcomingTrips.isEmpty()) {
        model.addAttribute("noTripsMessage", "No upcoming trips are currently scheduled. Please check back soon!");
      }

      return "trips/upcoming";

    } catch (Exception e) {
      logger.error("Error loading upcoming trips", e);
      model.addAttribute("error", "Error loading upcoming trips.");
      return "trips/upcoming";
    }
  }

  /**
   * Display featured trips.
   * GET /trips/featured
   */
  @GetMapping("/featured")
  public String featuredTrips(Model model) {
    logger.debug("Displaying featured trips");

    try {
      List<Trip> featuredTrips = tripService.getFeaturedTrips(12);

      model.addAttribute("featuredTrips", featuredTrips);
      model.addAttribute("pageTitle", "Featured Diving Adventures");

      return "trips/featured";

    } catch (Exception e) {
      logger.error("Error loading featured trips", e);
      model.addAttribute("error", "Error loading featured trips.");
      return "trips/featured";
    }
  }

  /**
   * Search trips page.
   * GET /trips/search
   */
  @GetMapping("/search")
  public String searchTripsPage(Model model) {
    logger.debug("Displaying trip search page");

    model.addAttribute("locations", tripService.getDistinctLocations());
    model.addAttribute("certifications", DiveCertification.values());
    model.addAttribute("pageTitle", "Search Diving Trips");

    return "trips/search";
  }

  // ===== BOOKING ENDPOINTS ===== WHich is not active for now ============


  /*
    * Note: Booking functionality is currently not active.
    * This section is reserved for future implementation.
    * The same for admin courses and trips management.
   */

  /**
   * Book a trip.
   * POST /trips/{id}/book
   */
  @PostMapping("/{id}/book")
  public String bookTrip(
      @PathVariable Long id,
      @RequestParam(required = false) DiveCertification userCertification,
      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {

    logger.info("Booking trip with ID: {}", id);

    try {
      Trip trip;
      if (userCertification != null) {
        trip = tripService.bookTripWithCertification(id, userCertification);
      } else {
        trip = tripService.bookTrip(id);
      }

      redirectAttributes.addFlashAttribute("successMessage",
          "Successfully booked trip to " + trip.getLocation() + "!");

      return "redirect:/trips/" + id;

    } catch (IllegalStateException e) {
      logger.error("Cannot book trip", e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/trips/" + id;
    } catch (Exception e) {
      logger.error("Error booking trip with ID: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", "Error booking trip. Please try again.");
      return "redirect:/trips/" + id;
    }
  }

  /**
   * Cancel a booking.
   * POST /trips/{id}/cancel-booking
   */
  @PostMapping("/{id}/cancel-booking")
  public String cancelBooking(
      @PathVariable Long id,
      RedirectAttributes redirectAttributes) {

    logger.info("Cancelling booking for trip with ID: {}", id);

    try {
      Trip trip = tripService.cancelBooking(id);
      redirectAttributes.addFlashAttribute("successMessage",
          "Booking cancelled for trip to " + trip.getLocation());

      return "redirect:/trips/" + id;

    } catch (Exception e) {
      logger.error("Error cancelling booking for trip with ID: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling booking. Please try again.");
      return "redirect:/trips/" + id;
    }
  }

  // ===== ADMIN TRIP MANAGEMENT =====

  /**
   * Admin trip list.
   * GET /trips/admin
   */
  @GetMapping("/admin")
  public String adminTripList(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) PublicationStatus status,
      Model model) {

    logger.debug("Admin trip list - page: {}, size: {}, status: {}", page, size, status);

    try {
      Pageable pageable = PageRequest.of(page, size);
      Page<Trip> tripPage = tripService.getAllTrips(pageable);

      // Filter by status if provided
      List<Trip> trips = tripPage.getContent();
      if (status != null) {
        trips = trips.stream()
            .filter(trip -> trip.getStatus() == status)
            .toList();
      }
      model.addAttribute("trips", trips);
      model.addAttribute("currentPage", page);
      model.addAttribute("totalPages", tripPage.getTotalPages());
      model.addAttribute("totalItems", tripPage.getTotalElements());
      model.addAttribute("statuses", PublicationStatus.values());
      model.addAttribute("selectedStatus", status);

      // Add statistics
      Map<String, Object> statistics = tripService.getTripStatistics();
      model.addAttribute("statistics", statistics);

      return "admin/trips/list";

    } catch (Exception e) {
      logger.error("Error loading admin trip list", e);
      model.addAttribute("error", "Error loading trips.");
      return "admin/trips/list";
    }
  }

  /**
   * Show create trip form.
   * GET /trips/admin/create
   */
  @GetMapping("/admin/create")
  public String createTripForm(Model model) {
    logger.debug("Showing create trip form");

    model.addAttribute("tripDTO", new TripDTO(null, "", "", "", null, null, null, "",
        1, 0, DiveCertification.OPEN_WATER, null,
        PublicationStatus.PUBLISHED, false, 0, "", null));
    model.addAttribute("certifications", DiveCertification.values());
    model.addAttribute("statuses", PublicationStatus.values());
    model.addAttribute("pageTitle", "Create New Trip");

    return "admin/trips/create";
  }

  /**
   * Process create trip form.
   * POST /trips/admin/create
   */
  @PostMapping("/admin/create")
  public String createTrip(
      @Valid @ModelAttribute("tripDTO") TripDTO tripDTO,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {

    logger.info("Processing create trip form for: {}", tripDTO.getLocation());

    if (bindingResult.hasErrors()) {
      logger.warn("Validation errors in create trip form");
      model.addAttribute("certifications", DiveCertification.values());
      model.addAttribute("statuses", PublicationStatus.values());
      model.addAttribute("pageTitle", "Create New Trip");
      return "admin/trips/create";
    }

    try {
      Trip createdTrip = tripService.createTrip(tripDTO);
      redirectAttributes.addFlashAttribute("successMessage",
          "Trip '" + createdTrip.getLocation() + "' created successfully!");

      return "redirect:/trips/admin";

    } catch (Exception e) {
      logger.error("Error creating trip", e);
      model.addAttribute("errorMessage", "Error creating trip: " + e.getMessage());
      model.addAttribute("certifications", DiveCertification.values());
      model.addAttribute("statuses", PublicationStatus.values());
      return "admin/trips/create";
    }
  }

  /**
   * Show edit trip form.
   * GET /trips/admin/{id}/edit
   */
  @GetMapping("/admin/{id}/edit")
  public String editTripForm(@PathVariable Long id, Model model) {
    logger.debug("Showing edit trip form for ID: {}", id);

    try {
      Trip trip = tripService.getTripById(id);
      TripDTO tripDTO = new TripDTO(trip);

      model.addAttribute("tripDTO", tripDTO);
      model.addAttribute("trip", trip);
      model.addAttribute("certifications", DiveCertification.values());
      model.addAttribute("statuses", PublicationStatus.values());
      model.addAttribute("pageTitle", "Edit Trip: " + trip.getLocation());

      return "admin/trips/edit";

    } catch (Exception e) {
      logger.error("Error loading trip for editing with ID: {}", id, e);
      model.addAttribute("error", "Trip not found.");
      return "error/404";
    }
  }

  /**
   * Process edit trip form.
   * POST /trips/admin/{id}/edit
   */
  @PostMapping("/admin/{id}/edit")
  public String editTrip(
      @PathVariable Long id,
      @Valid @ModelAttribute("tripDTO") TripDTO tripDTO,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {

    logger.info("Processing edit trip form for ID: {}", id);

    if (bindingResult.hasErrors()) {
      logger.warn("Validation errors in edit trip form");
      try {
        Trip trip = tripService.getTripById(id);
        model.addAttribute("trip", trip);
        model.addAttribute("certifications", DiveCertification.values());
        model.addAttribute("statuses", PublicationStatus.values());
        model.addAttribute("pageTitle", "Edit Trip: " + trip.getLocation());
        return "admin/trips/edit";
      } catch (Exception e) {
        return "error/404";
      }
    }

    try {
      Trip updatedTrip = tripService.updateTrip(id, tripDTO);
      redirectAttributes.addFlashAttribute("successMessage",
          "Trip '" + updatedTrip.getLocation() + "' updated successfully!");

      return "redirect:/trips/admin";

    } catch (Exception e) {
      logger.error("Error updating trip with ID: {}", id, e);
      model.addAttribute("errorMessage", "Error updating trip: " + e.getMessage());
      try {
        Trip trip = tripService.getTripById(id);
        model.addAttribute("trip", trip);
        model.addAttribute("certifications", DiveCertification.values());
        model.addAttribute("statuses", PublicationStatus.values());
        return "admin/trips/edit";
      } catch (Exception ex) {
        return "error/404";
      }
    }
  }

  /**
   * Delete trip.
   * POST /trips/admin/{id}/delete
   */
  @PostMapping("/admin/{id}/delete")
  public String deleteTrip(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    logger.info("Deleting trip with ID: {}", id);

    try {
      Trip trip = tripService.getTripById(id);
      String tripLocation = trip.getLocation();

      tripService.deleteTrip(id);
      redirectAttributes.addFlashAttribute("successMessage",
          "Trip '" + tripLocation + "' deleted successfully!");

    } catch (IllegalStateException e) {
      logger.error("Cannot delete trip with active bookings", e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Cannot delete trip with active bookings. Archive it instead.");
    } catch (Exception e) {
      logger.error("Error deleting trip with ID: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", "Error deleting trip.");
    }

    return "redirect:/trips/admin";
  }

  /**
   * Archive trip.
   * POST /trips/admin/{id}/archive
   */
  @PostMapping("/admin/{id}/archive")
  public String archiveTrip(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    logger.info("Archiving trip with ID: {}", id);

    try {
      Trip trip = tripService.archiveTrip(id);
      redirectAttributes.addFlashAttribute("successMessage",
          "Trip '" + trip.getLocation() + "' archived successfully!");

    } catch (Exception e) {
      logger.error("Error archiving trip with ID: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", "Error archiving trip.");
    }

    return "redirect:/trips/admin";
  }

  // ===== ADMIN ANALYTICS =====

  /**
   * Trip analytics dashboard.
   * GET /trips/admin/analytics
   */
  @GetMapping("/admin/analytics")
  public String tripAnalytics(Model model) {
    logger.debug("Displaying trip analytics");

    try {
      Map<String, Object> statistics = tripService.getTripStatistics();
      List<Trip> lowBookingTrips = tripService.getTripsWithLowBookings();
      List<Trip> mostBookedTrips = tripService.getMostBookedTrips(10);
      List<Object[]> popularDestinations = tripService.getPopularDestinations();
      Map<DiveCertification, Long> certificationDistribution = tripService.getTripsByCertificationLevel();

      model.addAttribute("statistics", statistics);
      model.addAttribute("lowBookingTrips", lowBookingTrips);
      model.addAttribute("mostBookedTrips", mostBookedTrips);
      model.addAttribute("popularDestinations", popularDestinations);
      model.addAttribute("certificationDistribution", certificationDistribution);

      return "admin/trips/analytics";

    } catch (Exception e) {
      logger.error("Error loading trip analytics", e);
      model.addAttribute("error", "Error loading analytics data.");
      return "admin/trips/analytics";
    }
  }

  // ===== UTILITY METHODS =====

  /**
   * Check if any search filters are applied.
   */
  private boolean hasFilters(String location, LocalDate startDate, LocalDate endDate,
      DiveCertification certification, boolean availableOnly,
      BigDecimal minPrice, BigDecimal maxPrice) {
    return (location != null && !location.trim().isEmpty()) ||
        startDate != null ||
        endDate != null ||
        certification != null ||
        availableOnly ||
        minPrice != null ||
        maxPrice != null;
  }

  // ===== EXCEPTION HANDLERS =====

  /**
   * Handle general exceptions for trip pages.
   */
  @ExceptionHandler(Exception.class)
  public String handleException(Exception e, Model model) {
    logger.error("Unhandled exception in TripMvcController", e);
    model.addAttribute("error", "An unexpected error occurred. Please try again.");
    return "error/general";
  }
}
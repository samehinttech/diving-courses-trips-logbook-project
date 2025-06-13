package ch.oceandive.controller.web;

import ch.oceandive.model.Trip;
import ch.oceandive.model.DiveCertification;
import ch.oceandive.service.TripService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * MVC admin controller for managing trips from the admin dashboard.
 * For Future Dev
 */
@Controller
@RequestMapping("/admin/trips")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTripController {

  private static final Logger logger = LoggerFactory.getLogger(AdminTripController.class);
  private static final int DEFAULT_PAGE_SIZE = 10;

  private final TripService tripService;

  public AdminTripController(TripService tripService) {
    this.tripService = tripService;
  }

  /**
   * Display list of all trips with search and pagination.
   */
  @GetMapping
  public String listTrips(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE + "") int size,
      @RequestParam(required = false) String search,
      Model model) {

    try {
      Pageable pageable = PageRequest.of(page, size);
      Page<Trip> tripPage;

      if (search != null && !search.trim().isEmpty()) {
        tripPage = tripService.getAllTrips(pageable);
        model.addAttribute("search", search);
      } else {
        tripPage = tripService.getAllTrips(pageable);
      }

      model.addAttribute("tripPage", tripPage);
      model.addAttribute("trips", tripPage.getContent());
      model.addAttribute("currentPage", page);
      model.addAttribute("totalPages", tripPage.getTotalPages());
      model.addAttribute("totalElements", tripPage.getTotalElements());
      model.addAttribute("pageSize", size);

      // Add summary statistics
      model.addAttribute("totalTrips", tripService.getAllTrips().size());
      model.addAttribute("availableTrips", tripService.getAvailableTrips().size());
      model.addAttribute("upcomingTrips", tripService.getUpcomingTrips().size());

      model.addAttribute("pageTitle", "Trip Management - Admin Dashboard");

      return "admin/trips/list";

    } catch (Exception e) {
      logger.error("Error loading trips list", e);
      model.addAttribute("error", "Error loading trips: " + e.getMessage());
      return "admin/trips/list";
    }
  }

  /**
   * Show form to create a new trip.
   */
  @GetMapping("/create")
  public String showCreateForm(Model model) {
    Trip trip = new Trip();
    // Set default values
    trip.setStartDate(LocalDate.now().plusDays(30)); // Default start date
    trip.setEndDate(LocalDate.now().plusDays(37));   // Default 7-day trip

    model.addAttribute("trip", trip);
    model.addAttribute("certifications", DiveCertification.values());
    model.addAttribute("pageTitle", "Create New Trip - Admin Dashboard");
    model.addAttribute("isEdit", false);

    return "admin/trips/forms";
  }

  /**
   * Process trip creation.
   */
  @PostMapping("/create")
  public String createTrip(
      @Valid @ModelAttribute("trip") Trip trip,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {

    try {
      if (result.hasErrors()) {
        model.addAttribute("certifications", DiveCertification.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Create New Trip - Admin Dashboard");
        return "admin/trips/forms";
      }

      Trip createdTrip = tripService.createTrip(trip);

      logger.info("Admin created new trip: {} (ID: {})",
          createdTrip.getLocation(), createdTrip.getId());

      redirectAttributes.addFlashAttribute("successMessage",
          "Trip to '" + createdTrip.getLocation() + "' created successfully!");

      return "redirect:/admin/trips";

    } catch (Exception e) {
      logger.error("Error creating trip", e);
      model.addAttribute("error", "Error creating trip: " + e.getMessage());
      model.addAttribute("certifications", DiveCertification.values());
      model.addAttribute("isEdit", false);
      model.addAttribute("pageTitle", "Create New Trip - Admin Dashboard");
      return "admin/trips/forms";
    }
  }

  /**
   * Show form to edit an existing trip.
   */
  @GetMapping("/edit/{id}")
  public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    try {
      Trip trip = tripService.getTripById(id);

      model.addAttribute("trip", trip);
      model.addAttribute("certifications", DiveCertification.values());
      model.addAttribute("pageTitle", "Edit Trip: " + trip.getLocation());
      model.addAttribute("isEdit", true);

      return "admin/trips/forms";

    } catch (Exception e) {
      logger.error("Error loading trip for edit: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Trip not found or error loading trip details.");
      return "redirect:/admin/trips";
    }
  }

  /**
   * Process trip update.
   */
  @PostMapping("/edit/{id}")
  public String updateTrip(
      @PathVariable Long id,
      @Valid @ModelAttribute("trip") Trip trip,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {

    try {
      if (result.hasErrors()) {
        model.addAttribute("certifications", DiveCertification.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Edit Trip");
        return "admin/trips/forms";
      }

      Trip updatedTrip = tripService.updateTrip(id, trip);

      logger.info("Admin updated trip: {} (ID: {})",
          updatedTrip.getLocation(), updatedTrip.getId());

      redirectAttributes.addFlashAttribute("successMessage",
          "Trip to '" + updatedTrip.getLocation() + "' updated successfully!");

      return "redirect:/admin/trips";

    } catch (Exception e) {
      logger.error("Error updating trip: {}", id, e);
      model.addAttribute("error", "Error updating trip: " + e.getMessage());
      model.addAttribute("certifications", DiveCertification.values());
      model.addAttribute("isEdit", true);
      model.addAttribute("pageTitle", "Edit Trip");
      return "admin/trips/forms";
    }
  }

  /**
   * View trip details.
   */
  @GetMapping("/view/{id}")
  public String viewTrip(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    try {
      Trip trip = tripService.getTripById(id);

      model.addAttribute("trip", trip);
      model.addAttribute("pageTitle", "Trip Details: " + trip.getLocation());

      return "admin/trips/view";

    } catch (Exception e) {
      logger.error("Error loading trip details: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Trip not found or error loading trip details.");
      return "redirect:/admin/trips";
    }
  }

  /**
   * Delete a trip (with confirmation).
   */
  @PostMapping("/delete/{id}")
  public String deleteTrip(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      Trip trip = tripService.getTripById(id);
      String tripLocation = trip.getLocation();

      // Check if trip has bookings
      if (trip.getCurrentBookings() > 0) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "Cannot delete trip to '" + tripLocation + "' because it has " +
                trip.getCurrentBookings() + " active bookings.");
        return "redirect:/admin/trips";
      }

      tripService.deleteTrip(id);

      logger.info("Admin deleted trip: {} (ID: {})", tripLocation, id);

      redirectAttributes.addFlashAttribute("successMessage",
          "Trip to '" + tripLocation + "' deleted successfully!");

      return "redirect:/admin/trips";

    } catch (Exception e) {
      logger.error("Error deleting trip: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Error deleting trip: " + e.getMessage());
      return "redirect:/admin/trips";
    }
  }

  /**
   * Bulk operations for trips.
   */
  @PostMapping("/bulk-action")
  public String bulkAction(
      @RequestParam("action") String action,
      @RequestParam("tripIds") Long[] tripIds,
      RedirectAttributes redirectAttributes) {

    try {
      int successCount = 0;

      if (action.equals("delete")) {
        for (Long id : tripIds) {
          Trip trip = tripService.getTripById(id);
          if (trip.getCurrentBookings() == 0) {
            tripService.deleteTrip(id);
            successCount++;
          }
        }
        redirectAttributes.addFlashAttribute("successMessage",
            successCount + " trips deleted successfully!");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage",
            "Unknown bulk action: " + action);
      }

      return "redirect:/admin/trips";

    } catch (Exception e) {
      logger.error("Error performing bulk action: {}", action, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Error performing bulk action: " + e.getMessage());
      return "redirect:/admin/trips";
    }
  }

  /**
   * Trip statistics dashboard.
   */
  @GetMapping("/stats")
  public String tripStats(Model model) {
    try {
      model.addAttribute("totalTrips", tripService.getAllTrips().size());
      model.addAttribute("availableTrips", tripService.getAvailableTrips().size());
      model.addAttribute("upcomingTrips", tripService.getUpcomingTrips().size());

      // Calculate total capacity and bookings
      var allTrips = tripService.getAllTrips();
      int totalCapacity = allTrips.stream().mapToInt(Trip::getCapacity).sum();
      int totalBookings = allTrips.stream().mapToInt(Trip::getCurrentBookings).sum();

      model.addAttribute("totalCapacity", totalCapacity);
      model.addAttribute("totalBookings", totalBookings);
      model.addAttribute("availableSpots", totalCapacity - totalBookings);

      model.addAttribute("pageTitle", "Trip Statistics - Admin Dashboard");

      return "admin/trips/stats";

    } catch (Exception e) {
      logger.error("Error loading trip statistics", e);
      model.addAttribute("error", "Error loading statistics: " + e.getMessage());
      return "admin/trips/stats";
    }
  }
}
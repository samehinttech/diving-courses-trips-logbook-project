package ch.oceandive.controller.web;

import ch.oceandive.dto.DiveLogDTO;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.service.DiveLogService;
import ch.oceandive.service.PremiumUserService;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * MVC controller for dive log pages
 */
@Controller
@PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
public class DiveLogMVCController {

  private static final Logger logger = LoggerFactory.getLogger(DiveLogMVCController.class);
  private static final String PAGE_TITLE_DIVE_LOG = "My Dive Log - OceanDive";
  private static final String PAGE_TITLE_ADD_DIVE = "Add New Dive - OceanDive";

  private final DiveLogService diveLogService;
  private final PremiumUserService premiumUserService;

  @Autowired
  public DiveLogMVCController(DiveLogService diveLogService, PremiumUserService premiumUserService) {
    this.diveLogService = diveLogService;
    this.premiumUserService = premiumUserService;
  }

  // Get the current logged-in user
  private PremiumUser getCurrentPremiumUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    try {
      return premiumUserService.getPremiumUserEntityByUsername(username);
    } catch (Exception e) {
      // Check if this is an admin user
      boolean isAdmin = authentication.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

      if (isAdmin) {
        logger.debug("Admin user {} accessing dive logs (no premium account)", username);
        return null; // Return null for admin users without premium accounts
      } else {
        throw e;
      }
    }
  }

  // Method to check if the current user is admin
  private boolean isCurrentUserAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }

  /**
   * GET /dive-log: Show the dive log page for the current user
   */
  @GetMapping("/dive-log")
  public String diveLog(Model model, @RequestParam(required = false) String location) {
    try {
      PremiumUser currentUser = getCurrentPremiumUser();
      // Handle admin users without premium accounts
      if (currentUser == null && isCurrentUserAdmin()) {
        model.addAttribute("pageTitle", PAGE_TITLE_DIVE_LOG);
        model.addAttribute("diveLogs", List.of());
        model.addAttribute("totalDives", 0);
        model.addAttribute("totalHours", 0.0);
        model.addAttribute("uniqueLocations", 0L);
        model.addAttribute("locations", List.of());
        model.addAttribute("filteredCount", 0);
        model.addAttribute("selectedLocation", "");
        model.addAttribute("userDisplayName", "Admin User");
        model.addAttribute("isAdmin", true);
        return "dive-log";
      }

      // Regular premium user flow - use service directly for page rendering
      List<DiveLogDTO> diveLogs;
      String diveLocation = "";
      if (location != null && !location.trim().isEmpty()) {
        diveLocation = location.trim();
        diveLogs = diveLogService.findByUserAndLocation(currentUser, diveLocation);
        model.addAttribute("selectedLocation", diveLocation);
      } else {
        diveLogs = diveLogService.findByUserOrderByDiveDateDesc(currentUser);
        model.addAttribute("selectedLocation", "");
      }

      // Get statistics of the user's dive logs
      Map<String, Object> statistics = diveLogService.getUserStatistics(currentUser);
      // Get locations for filter dropdown
      List<String> locations = diveLogService.getUserLocations(currentUser);
      model.addAttribute("pageTitle", PAGE_TITLE_DIVE_LOG);
      model.addAttribute("diveLogs", diveLogs);
      model.addAttribute("totalDives", statistics.get("totalDives"));
      model.addAttribute("totalHours", statistics.get("totalHours"));
      model.addAttribute("uniqueLocations", statistics.get("uniqueLocations"));
      model.addAttribute("locations", locations);
      model.addAttribute("filteredCount", diveLogs.size());
      assert currentUser != null;
      model.addAttribute("userDisplayName",
          currentUser.getFirstName() != null ? currentUser.getFirstName()
              : currentUser.getUsername());
      model.addAttribute("isAdmin", isCurrentUserAdmin());

      logger.debug("Loaded {} dive logs for user: {} with location filter: '{}'",
          diveLogs.size(), currentUser.getUsername(), diveLocation);
    } catch (Exception e) {
      logger.error("Error loading dive logs with location filter: {}", location, e);
      model.addAttribute("errorMessage", "Failed to load dive logs. Please try again.");
      model.addAttribute("diveLogs", List.of());
      model.addAttribute("totalDives", 0);
      model.addAttribute("totalHours", 0.0);
      model.addAttribute("uniqueLocations", 0L);
      model.addAttribute("filteredCount", 0);
      model.addAttribute("locations", List.of());
      model.addAttribute("selectedLocation", "");
      model.addAttribute("userDisplayName", "User");
      model.addAttribute("isAdmin", isCurrentUserAdmin());
    }
    return "dive-log";
  }

  /**
   * GET /dive-log/add: Show add new dive log page
   */
  @GetMapping("/dive-log/add")
  public String addDiveLogPage(Model model) {
    // Admin users can access the page but won't actually use it
    if (isCurrentUserAdmin()) {
      model.addAttribute("pageTitle", PAGE_TITLE_ADD_DIVE);
      model.addAttribute("diveLog", new DiveLogDTO());
      model.addAttribute("noteCount", 0);
      model.addAttribute("isAdmin", true);
      model.addAttribute("adminMessage",
          "Admin access - dive log functionality available but not typically used by administrators.");
      return "add-dive-log";
    }
    model.addAttribute("pageTitle", PAGE_TITLE_ADD_DIVE);

    // Create a new dive log DTO with default values
    DiveLogDTO diveLogDTO = new DiveLogDTO();
    diveLogDTO.setDiveDate(LocalDate.now());
    LocalTime now = LocalTime.now().withSecond(0).withNano(0);
    diveLogDTO.setStartTime(now);
    diveLogDTO.setEndTime(now.plusHours(1));
    model.addAttribute("diveLog", diveLogDTO);
    model.addAttribute("noteCount", 0);
    model.addAttribute("isAdmin", false);
    return "add-dive-log";
  }

  /**
   * POST /dive-log/add: Process new dive log form
   * Uses service directly for creation
   */
  @PostMapping("/dive-log/add")
  public String addDiveLog(@Valid @ModelAttribute("diveLog") DiveLogDTO diveLogDTO,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {
    // Admin users can access but restricted to view only
    if (isCurrentUserAdmin()) {
      redirectAttributes.addFlashAttribute("infoMessage",
          "Admin access: This functionality is available but typically used by premium users.");
      return "redirect:/dive-log";
    }
    model.addAttribute("pageTitle", PAGE_TITLE_ADD_DIVE);
    // Calculate note count for display
    int noteCount = diveLogDTO.getNotes() != null ? diveLogDTO.getNotes().length() : 0;
    model.addAttribute("noteCount", noteCount);
    if (bindingResult.hasErrors()) {
      logger.warn("Validation errors in dive log form for user");
      return "add-dive-log";
    }
    try {
      PremiumUser currentUser = getCurrentPremiumUser();
      if (currentUser == null) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "You must be logged in to add dive logs.");
        return "redirect:/dive-log";
      }

      // Validate dive log data using service
      String validationError = diveLogService.validate(diveLogDTO, currentUser, false);
      if (validationError != null) {
        model.addAttribute("errorMessage", validationError);
        return "add-dive-log";
      }
      // Create dive log using service directly
      DiveLogDTO createdDiveLog = diveLogService.create(diveLogDTO, currentUser);
      redirectAttributes.addFlashAttribute("successMessage",
          "New dive log #" + createdDiveLog.getDiveNumber() + " added successfully!");
      logger.info("New dive log #{} created by user: {}",
          createdDiveLog.getDiveNumber(), currentUser.getUsername());
      return "redirect:/dive-log";
    } catch (Exception e) {
      logger.error("Error creating dive log", e);
      model.addAttribute("errorMessage",
          "Failed to create dive log. Please check your input and try again.");
      return "add-dive-log";
    }
  }

  /**
   * POST /dive-log/delete/{diveNumber}: Delete a dive log (form submission)
   * This redirects to REST API via JavaScript, but kept for fallback
   */
  @PostMapping("/dive-log/delete/{diveNumber}")
  public String deleteDiveLog(@PathVariable Integer diveNumber, RedirectAttributes redirectAttributes) {
    // Admin users can access but show an info message
    if (isCurrentUserAdmin()) {
      redirectAttributes.addFlashAttribute("infoMessage",
          "Admin access: Delete functionality available but typically used by premium users.");
      return "redirect:/dive-log";
    }

    try {
      PremiumUser currentUser = getCurrentPremiumUser();

      if (currentUser == null) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "You must be logged in to delete dive logs.");
        return "redirect:/dive-log";
      }

      // Use service directly for fallback deletion
      diveLogService.deleteByDiveNumber(diveNumber, currentUser);
      redirectAttributes.addFlashAttribute("successMessage", "Dive log deleted successfully.");
      logger.info("Dive log #{} deleted by user: {}", diveNumber, currentUser.getUsername());

    } catch (Exception e) {
      logger.error("Error deleting dive log: {}", diveNumber, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Failed to delete dive log. Please try again.");
    }

    return "redirect:/dive-log";
  }

  /**
   * POST /dive-log/preview: Preview/calculate dive details before saving
   */
  @PostMapping("/dive-log/preview")
  public String previewDiveLog(@ModelAttribute("diveLog") DiveLogDTO diveLogDTO, Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_ADD_DIVE);
    model.addAttribute("isAdmin", isCurrentUserAdmin());

    // Calculate duration if start and end times are provided
    if (diveLogDTO.getStartTime() != null && diveLogDTO.getEndTime() != null) {
      if (diveLogDTO.getEndTime().isAfter(diveLogDTO.getStartTime())) {
        long durationMinutes = Duration.between(
            diveLogDTO.getStartTime(), diveLogDTO.getEndTime()).toMinutes();
        diveLogDTO.setDuration((int) durationMinutes);

        model.addAttribute("previewMessage",
            "Duration calculated: " + formatDuration((int) durationMinutes) +
                ". Review your details and save when ready.");
      }
    }

    // Calculate note count
    int noteCount = diveLogDTO.getNotes() != null ? diveLogDTO.getNotes().length() : 0;
    model.addAttribute("noteCount", noteCount);

    return "add-dive-log";
  }

  /**
   * GET /dive-log/edit/{diveNumber}: Show edit dive log page
   * Page rendering only - actual data operations handled by REST API
   */
  @GetMapping("/dive-log/edit/{diveNumber}")
  public String editDiveLogPage(@PathVariable Integer diveNumber, Model model) {
    // Admin users can access but restricted to view only
    if (isCurrentUserAdmin()) {
      model.addAttribute("pageTitle", "Edit Dive Log - OceanDive");
      model.addAttribute("diveLog", new DiveLogDTO());
      model.addAttribute("isEdit", true);
      model.addAttribute("isAdmin", true);
      model.addAttribute("adminMessage",
          "Admin access - edit functionality available but typically used by premium users.");
      return "edit-dive-log";
    }

    try {
      PremiumUser currentUser = getCurrentPremiumUser();
      // The modal will reload data via REST API
      DiveLogDTO diveLog = diveLogService.findByDiveNumberAndUser(diveNumber, currentUser);
      model.addAttribute("pageTitle", "Edit Dive #" + diveLog.getDiveNumber() + " - OceanDive");
      model.addAttribute("diveLog", diveLog);
      model.addAttribute("isEdit", true);
      model.addAttribute("isAdmin", false);
      // Calculate note count for display
      int noteCount = diveLog.getNotes() != null ? diveLog.getNotes().length() : 0;
      model.addAttribute("noteCount", noteCount);
      logger.debug("Loaded dive log for editing: DiveNumber={}, Date={}",
          diveLog.getDiveNumber(), diveLog.getDiveDate());
      return "edit-dive-log";
    } catch (Exception e) {
      logger.error("Error loading dive log for editing: {}", diveNumber, e);
      return "redirect:/dive-log?error=load-failed";
    }
  }


  //Format duration for display purposes. That is added after a long time debugging time formatting issues
  private String formatDuration(int minutes) {
    if (minutes < 60) {
      return minutes + " min";
    }
    int hours = minutes / 60;
    int mins = minutes % 60;
    return String.format("%dh %02dm", hours, mins);
  }
}
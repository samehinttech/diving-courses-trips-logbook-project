package ch.oceandive.controller.web;

import ch.oceandive.dto.DiveLogDTO;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.service.DiveLogService;
import ch.oceandive.service.PremiumUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  /**
   * Helper method to get the current logged-in user
   */
  private PremiumUser getCurrentPremiumUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return premiumUserService.getPremiumUserEntityByUsername(username);
  }

  /**
   * GET /dive-log: Show the dive log page for the current user
   */
  @GetMapping("/dive-log")
  public String diveLog(Model model, @RequestParam(required = false) String location) {
    try {
      PremiumUser currentUser = getCurrentPremiumUser();

      // Get dive logs using the service
      List<DiveLogDTO> diveLogs;
      if (location != null && !location.trim().isEmpty()) {
        diveLogs = diveLogService.findByUserAndLocation(currentUser, location.trim());
        model.addAttribute("selectedLocation", location.trim());
      } else {
        diveLogs = diveLogService.findByUserOrderByDiveDateDesc(currentUser);
        model.addAttribute("selectedLocation", "");
      }

      // Get statistics from a service
      Map<String, Object> statistics = diveLogService.getUserStatistics(currentUser);

      // Get locations for filter dropdown
      List<String> locations = diveLogService.getUserLocations(currentUser);

      // Add data to the model
      model.addAttribute("pageTitle", PAGE_TITLE_DIVE_LOG);
      model.addAttribute("diveLogs", diveLogs);
      model.addAttribute("totalDives", statistics.get("totalDives"));
      model.addAttribute("totalHours", statistics.get("totalHours"));
      model.addAttribute("uniqueLocations", statistics.get("uniqueLocations"));
      model.addAttribute("locations", locations);
      model.addAttribute("userDisplayName",
          currentUser.getFirstName() != null ? currentUser.getFirstName() : currentUser.getUsername());

      logger.debug("Loaded {} dive logs for user: {}", diveLogs.size(), currentUser.getUsername());

    } catch (Exception e) {
      logger.error("Error loading dive logs", e);
      model.addAttribute("errorMessage", "Failed to load dive logs. Please try again.");
      model.addAttribute("diveLogs", List.of());
      model.addAttribute("totalDives", 0);
      model.addAttribute("totalHours", 0.0);
      model.addAttribute("uniqueLocations", 0L);
    }

    return "dive-log";
  }

  /**
   * GET /dive-log/add: Show add new dive log page
   */
  @GetMapping("/dive-log/add")
  public String addDiveLogPage(Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_ADD_DIVE);

    // Create a new dive log DTO with default values
    DiveLogDTO diveLogDTO = new DiveLogDTO();
    diveLogDTO.setDiveDate(LocalDate.now());

    LocalTime now = LocalTime.now().withSecond(0).withNano(0);
    diveLogDTO.setStartTime(now);
    diveLogDTO.setEndTime(now.plusHours(1));

    model.addAttribute("diveLog", diveLogDTO);
    model.addAttribute("noteCount", 0);

    return "add-dive-log";
  }

  /**
   * POST /dive-log/add: add a new dive log form
   */
  @PostMapping("/dive-log/add")
  public String addDiveLog(@Valid @ModelAttribute("diveLog") DiveLogDTO diveLogDTO,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_ADD_DIVE);
    // Calculate note count for display
    int noteCount = diveLogDTO.getNotes() != null ? diveLogDTO.getNotes().length() : 0;
    model.addAttribute("noteCount", noteCount);
    if (bindingResult.hasErrors()) {
      return "add-dive-log";
    }

    try {
      PremiumUser currentUser = getCurrentPremiumUser();

      // Validate using service
      String validationError = diveLogService.validate(diveLogDTO, currentUser, false);
      if (validationError != null) {
        model.addAttribute("errorMessage", validationError);
        return "add-dive-log";
      }
      // Create dive log using service
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
   * POST /dive-log/delete/{id}: Delete a dive log (form submission)
   * This is for non-JavaScript fallback
   */
  @PostMapping("/dive-log/delete/{id}")
  public String deleteDiveLog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      PremiumUser currentUser = getCurrentPremiumUser();
      diveLogService.delete(id, currentUser);

      redirectAttributes.addFlashAttribute("successMessage", "Dive log deleted successfully.");
      logger.info("Dive log {} deleted by user: {}", id, currentUser.getUsername());

    } catch (Exception e) {
      logger.error("Error deleting dive log: {}", id, e);
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

    // Calculate duration if start and end times are provided
    if (diveLogDTO.getStartTime() != null && diveLogDTO.getEndTime() != null) {
      if (diveLogDTO.getEndTime().isAfter(diveLogDTO.getStartTime())) {
        long durationMinutes = java.time.Duration.between(
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
   * Format duration for display purposes.
   */
  private String formatDuration(int minutes) {
    if (minutes < 60) {
      return minutes + " min";
    }
    int hours = minutes / 60;
    int mins = minutes % 60;
    return String.format("%dh %02dm", hours, mins);
  }
}
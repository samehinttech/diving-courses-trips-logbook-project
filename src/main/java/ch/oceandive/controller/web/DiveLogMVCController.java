package ch.oceandive.controller.web;

import ch.oceandive.model.DiveLog;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.service.DiveLogService;
import ch.oceandive.service.PremiumUserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

/**
 * Pure MVC controller for dive log pages - server-side rendering only, no JavaScript
 */
@Controller
public class DiveLogMVCController {

  private static final Logger logger = LoggerFactory.getLogger(DiveLogMVCController.class);
  private static final String PAGE_TITLE_DIVE_LOG = "My Dive Log - OceanDive";

  private final DiveLogService diveLogService;
  private final PremiumUserService premiumUserService;

  @Autowired
  public DiveLogMVCController(DiveLogService diveLogService,
      PremiumUserService premiumUserService) {
    this.diveLogService = diveLogService;
    this.premiumUserService = premiumUserService;
  }

  //Helper method to get the current logged-in user
  private PremiumUser getCurrentPremiumUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return premiumUserService.getPremiumUserEntityByUsername(username);
  }

  /**
   * Calculate duration and character count for a dive log
   */
  private void calculateDerivedFields(DiveLog diveLog, Model model) {
    // Calculate duration if start and end times are provided
    if (diveLog.getStartTime() != null && diveLog.getEndTime() != null) {
      if (diveLog.getEndTime().isAfter(diveLog.getStartTime())) {
        long durationMinutes = java.time.Duration.between(diveLog.getStartTime(), diveLog.getEndTime()).toMinutes();
        diveLog.setDuration((int) durationMinutes);
      } else {
        // Clear duration if times are invalid
        diveLog.setDuration(null);
      }
    } else {
      diveLog.setDuration(null);
    }

    // Calculate character count for notes
    int noteCount = diveLog.getNotes() != null ? diveLog.getNotes().length() : 0;
    model.addAttribute("noteCount", noteCount);

    // Set dive date from start time if not set
    if (diveLog.getDiveDate() == null && diveLog.getStartTime() != null) {
      diveLog.setDiveDate(diveLog.getStartTime().toLocalDate());
    }
  }

  /**
   * GET /dive-log: Show the dive log page for the current user.
   */
  @GetMapping("/dive-log")
  public String diveLog(Model model, @RequestParam(required = false) String location) {
    try {
      // Get current user
      PremiumUser premiumUser = getCurrentPremiumUser();
      // Get dive logs for the user
      List<DiveLog> diveLogs;
      if (location != null && !location.trim().isEmpty()) {
        // Filter by location if provided
        diveLogs = diveLogService.findByUserAndLocation(premiumUser, location.trim());
        model.addAttribute("selectedLocation", location.trim());
      } else {
        // Get all dive logs for the user
        diveLogs = diveLogService.findByUserOrderByDiveDateDesc(premiumUser);
        model.addAttribute("selectedLocation", "");
      }

      // Calculate statistics
      long totalDives = diveLogService.countByUser(premiumUser);
      Double totalHours = diveLogService.getTotalHoursByUser(premiumUser);
      Long uniqueLocations = diveLogService.getUniqueLocationsByUser(premiumUser);

      // Add data to the model
      model.addAttribute("pageTitle", PAGE_TITLE_DIVE_LOG);
      model.addAttribute("diveLogs", diveLogs);
      model.addAttribute("totalDives", totalDives);
      model.addAttribute("totalHours", totalHours);
      model.addAttribute("uniqueLocations", uniqueLocations);
      model.addAttribute("userDisplayName",
          premiumUser.getFirstName() != null ? premiumUser.getFirstName()
              : premiumUser.getUsername());

      // Get unique locations for filter dropdown
      List<DiveLog> allUserDiveLogs = diveLogService.findByUser(premiumUser);
      Set<String> locations = allUserDiveLogs.stream()
          .map(DiveLog::getLocation)
          .collect(Collectors.toSet());
      model.addAttribute("locations", locations);

      logger.debug("Loaded {} dive logs for user: {}", diveLogs.size(), premiumUser.getUsername());

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
   * POST /dive-log/filter: Filter dive logs by location
   */
  @PostMapping("/dive-log/filter")
  public String filterDiveLogs(@RequestParam(required = false) String location) {
    if (location != null && !location.trim().isEmpty()) {
      return "redirect:/dive-log?location=" + location.trim();
    } else {
      return "redirect:/dive-log";
    }
  }

  /**
   * POST /dive-log/delete/{id}: Delete a dive log
   */
  @PostMapping("/dive-log/delete/{id}")
  public String deleteDiveLog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      PremiumUser premiumUser = getCurrentPremiumUser();

      // Verify the dive log belongs to the current user before deleting
      Optional<DiveLog> diveLogOpt = diveLogService.findById(id);
      if (diveLogOpt.isPresent() && diveLogOpt.get().getUser().getId().equals(premiumUser.getId())) {
        diveLogService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Dive log deleted successfully.");
        logger.info("Dive log {} deleted by user: {}", id, premiumUser.getUsername());
      } else {
        redirectAttributes.addFlashAttribute("errorMessage", "Dive log not found or access denied.");
        logger.warn("Unauthorized attempt to delete dive log {} by user: {}", id, premiumUser.getUsername());
      }
    } catch (Exception e) {
      logger.error("Error deleting dive log: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Failed to delete dive log. Please try again.");
    }

    return "redirect:/dive-log";
  }

  /**
   * GET /dive-log/add: Show add new dive log page
   */
  @GetMapping("/dive-log/add")
  public String addDiveLogPage(Model model) {
    model.addAttribute("pageTitle", "Add New Dive - OceanDive");

    // Create a new dive log with default values
    DiveLog diveLog = new DiveLog();

    // Set default date to today
    diveLog.setDiveDate(java.time.LocalDate.now());

    // Set default start time to current hour
    java.time.LocalDateTime now = java.time.LocalDateTime.now();
    diveLog.setStartTime(now.withMinute(0).withSecond(0).withNano(0));

    // Set default end time to one hour later
    diveLog.setEndTime(now.withMinute(0).withSecond(0).withNano(0).plusHours(1));

    // Calculate derived fields (duration, character count)
    calculateDerivedFields(diveLog, model);

    model.addAttribute("diveLog", diveLog);

    return "add-dive-log";
  }

  /**
   * POST /dive-log/add: Process new dive log form
   */
  @PostMapping("/dive-log/add")
  public String addDiveLog(@Valid @ModelAttribute DiveLog diveLog,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {
    try {
      PremiumUser premiumUser = getCurrentPremiumUser();

      // Calculate derived fields before validation
      calculateDerivedFields(diveLog, model);

      // Additional custom validation
      validateDiveLog(diveLog, bindingResult, premiumUser);

      // Check for validation errors
      if (bindingResult.hasErrors()) {
        model.addAttribute("pageTitle", "Add New Dive - OceanDive");
        return "add-dive-log";
      }

      // Set the user
      diveLog.setUser(premiumUser);

      // Save dive log
      diveLogService.save(diveLog);

      redirectAttributes.addFlashAttribute("successMessage",
          "New dive log #" + diveLog.getDiveNumber() + " added successfully!");
      logger.info("New dive log #{} created by user: {}", diveLog.getDiveNumber(), premiumUser.getUsername());

      return "redirect:/dive-log";

    } catch (Exception e) {
      logger.error("Error creating dive log", e);
      model.addAttribute("errorMessage",
          "Failed to create dive log. Please check your input and try again.");
      model.addAttribute("pageTitle", "Add New Dive - OceanDive");
      calculateDerivedFields(diveLog, model); // Recalculate for form display
      return "add-dive-log";
    }
  }

  /**
   * Custom validation for dive log business rules
   */
  private void validateDiveLog(DiveLog diveLog, BindingResult bindingResult, PremiumUser user) {
    // Validate time logic
    if (diveLog.getStartTime() != null && diveLog.getEndTime() != null) {
      if (diveLog.getEndTime().isBefore(diveLog.getStartTime()) ||
          diveLog.getEndTime().isEqual(diveLog.getStartTime())) {
        bindingResult.rejectValue("endTime", "error.endTime",
            "End time must be after start time.");
      } else {
        // Check if duration is reasonable (not more than 24 hours)
        long durationMinutes = java.time.Duration.between(diveLog.getStartTime(), diveLog.getEndTime()).toMinutes();
        if (durationMinutes > 1440) { // 24 hours
          bindingResult.rejectValue("endTime", "error.endTime",
              "Dive duration cannot exceed 24 hours. Please check your times.");
        } else if (durationMinutes < 1) {
          bindingResult.rejectValue("endTime", "error.endTime",
              "Dive must be at least 1 minute long.");
        }
      }
    }

    // Validate dive date consistency
    if (diveLog.getDiveDate() != null && diveLog.getStartTime() != null) {
      if (!diveLog.getDiveDate().equals(diveLog.getStartTime().toLocalDate())) {
        bindingResult.rejectValue("diveDate", "error.diveDate",
            "Dive date must match the date of the start time.");
      }
    }

    // Check for future dates
    if (diveLog.getDiveDate() != null && diveLog.getDiveDate().isAfter(java.time.LocalDate.now())) {
      bindingResult.rejectValue("diveDate", "error.diveDate",
          "Dive date cannot be in the future.");
    }

    // Check if dive number already exists for this user
    if (diveLog.getDiveNumber() != null) {
      Optional<DiveLog> existingDive = diveLogService.findByUserAndDiveNumber(user, diveLog.getDiveNumber());
      if (existingDive.isPresent()) {
        bindingResult.rejectValue("diveNumber", "error.diveNumber",
            "Dive number " + diveLog.getDiveNumber() + " already exists. Please use a different number.");
      }
    }

    // Validate temperature ranges (additional checks beyond entity validation)
    if (diveLog.getWaterTemperature() != null && diveLog.getAirTemperature() != null) {
      // Water shouldn't be much warmer than air in most cases
      double tempDiff = diveLog.getWaterTemperature() - diveLog.getAirTemperature();
      if (tempDiff > 20) {
        bindingResult.rejectValue("waterTemperature", "error.waterTemperature",
            "Water temperature seems unusually high compared to air temperature. Please verify.");
      }
    }
  }

  /**
   * POST /dive-log/preview: Preview/calculate dive details before saving
   */
  @PostMapping("/dive-log/preview")
  public String previewDiveLog(@ModelAttribute DiveLog diveLog, Model model) {
    model.addAttribute("pageTitle", "Add New Dive - OceanDive");

    // Calculate derived fields
    calculateDerivedFields(diveLog, model);

    // Add preview message
    if (diveLog.getDuration() != null && diveLog.getDuration() > 0) {
      model.addAttribute("previewMessage",
          "Duration calculated: " + diveLog.getFormattedDuration() + ". Review your details and save when ready.");
    }

    model.addAttribute("diveLog", diveLog);
    return "add-dive-log";
  }

  /**
   * Calculate dive statistics (kept for potential future use)
   */
  private Map<String, Object> calculateDiveStats(List<DiveLog> diveLogs) {
    Map<String, Object> statistics = new HashMap<>();

    int totalDives = diveLogs.size();
    int totalMinutes = diveLogs.stream()
        .mapToInt(dive -> dive.getDuration() != null ? dive.getDuration() : 0).sum();
    double totalHours = Math.round(totalMinutes / 60.0 * 10.0) / 10.0;
    long uniqueLocations = diveLogs.stream().map(DiveLog::getLocation).distinct().count();

    statistics.put("totalDives", totalDives);
    statistics.put("totalHours", totalHours);
    statistics.put("uniqueLocations", uniqueLocations);

    return statistics;
  }

  /**
   * Get empty statistics for error cases (kept for potential future use)
   */
  private Map<String, Object> getEmptyStats() {
    Map<String, Object> statistics = new HashMap<>();
    statistics.put("totalDives", 0);
    statistics.put("totalHours", 0.0);
    statistics.put("uniqueLocations", 0L);
    return statistics;
  }
}
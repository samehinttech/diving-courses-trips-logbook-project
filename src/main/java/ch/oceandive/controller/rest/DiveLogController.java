package ch.oceandive.controller.rest;

import ch.oceandive.dto.DiveLogDTO;
import ch.oceandive.model.DiveLog;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.service.DiveLogService;
import ch.oceandive.service.PremiumUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dive-logs")
public class DiveLogController {

  private final DiveLogService diveLogService;
  private final PremiumUserService premiumUserService;
  private static final Logger logger = LoggerFactory.getLogger(DiveLogController.class);

  public DiveLogController(DiveLogService diveLogService, PremiumUserService premiumUserService) {
    this.diveLogService = diveLogService;
    this.premiumUserService = premiumUserService;
  }

  /**
   * Get the currently authenticated PremiumUser.
   */
  private PremiumUser getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      String username = authentication.getName();
      return premiumUserService.getPremiumUserEntityByUsername(username);
    }
    throw new UsernameNotFoundException("User not authenticated");
  }

  /**
   * Convert DiveLog Entity to DTO
   */
  private DiveLogDTO convertToDTO(DiveLog diveLog) {
    return new DiveLogDTO(
        diveLog.getId(),
        diveLog.getDiveNumber(),
        diveLog.getLocation(),
        diveLog.getStartTime(),
        diveLog.getEndTime(),
        diveLog.getDuration(),
        diveLog.getWaterTemperature(),
        diveLog.getAirTemperature(),
        diveLog.getNotes(),
        diveLog.getDiveDate()
    );
  }

  /**
   * Convert DiveLog DTO to Entity
   */
  private DiveLog convertToEntity(DiveLogDTO dto, PremiumUser currentUser) {
    DiveLog diveLog = new DiveLog();
    diveLog.setId(dto.getId());
    diveLog.setDiveNumber(dto.getDiveNumber());
    diveLog.setLocation(dto.getLocation());
    diveLog.setStartTime(dto.getStartTime());
    diveLog.setEndTime(dto.getEndTime());
    diveLog.setDuration(dto.getDuration());
    diveLog.setWaterTemperature(dto.getWaterTemperature());
    diveLog.setAirTemperature(dto.getAirTemperature());
    diveLog.setNotes(dto.getNotes());
    diveLog.setDiveDate(dto.getDiveDate());
    diveLog.setUser(currentUser);
    calculateDiveProperties(diveLog); // Reuse calculation logic
    return diveLog;
  }

  /**
   * Calculate and set derived dive log properties like duration and dive date.
   */
  private void calculateDiveProperties(DiveLog diveLog) {
    if (diveLog.getDuration() == null && diveLog.getStartTime() != null && diveLog.getEndTime() != null) {
      long durationMinutes = java.time.Duration.between(diveLog.getStartTime(), diveLog.getEndTime()).toMinutes();
      diveLog.setDuration((int) durationMinutes);
    }
    if (diveLog.getDiveDate() == null && diveLog.getStartTime() != null) {
      diveLog.setDiveDate(diveLog.getStartTime().toLocalDate());
    }
  }

  /**
   * Verify if a dive log belongs to the current user.
   */
  private void verifyOwnership(DiveLog diveLog, PremiumUser currentUser) {
    if (!diveLog.getUser().getId().equals(currentUser.getId())) {
      throw new RuntimeException("Access denied");
    }
  }

  /**
   * Handle validation errors in a reusable way.
   */
  private ResponseEntity<?> handleValidationErrors(BindingResult bindingResult) {
    Map<String, String> errors = new HashMap<>();
    bindingResult.getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
    );
    return ResponseEntity.badRequest().body(Map.of("errors", errors));
  }

  /**
   * Get all dive logs for the current user.
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllDiveLogs(@RequestParam(required = false) String location) {
    try {
      PremiumUser currentUser = getCurrentUser();
      List<DiveLog> diveLogs = (location != null && !location.isBlank()) ?
          diveLogService.findByUserAndLocation(currentUser, location.trim()) :
          diveLogService.findByUserOrderByDiveDateDesc(currentUser);

      List<DiveLogDTO> diveLogDTOs = diveLogs.stream().map(this::convertToDTO).collect(Collectors.toList());
      return ResponseEntity.ok(Map.of(
          "diveLogs", diveLogDTOs,
          "totalDives", diveLogService.countByUser(currentUser),
          "totalHours", diveLogService.getTotalHoursByUser(currentUser),
          "uniqueLocations", diveLogService.getUniqueLocationsByUser(currentUser)
      ));
    } catch (Exception e) {
      logger.error("Error retrieving dive logs", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to retrieve dive logs"));
    }
  }

  /**
   * Get a specific dive log by ID.
   */
  @GetMapping("/{id}")
  public ResponseEntity<?> getDiveLog(@PathVariable Long id) {
    try {
      PremiumUser currentUser = getCurrentUser();
      DiveLog diveLog = diveLogService.findById(id).orElseThrow(() -> new RuntimeException("Dive log not found"));
      verifyOwnership(diveLog, currentUser);
      return ResponseEntity.ok(convertToDTO(diveLog));
    } catch (Exception e) {
      logger.error("Error retrieving dive log", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to retrieve dive log"));
    }
  }

  /**
   * Create a new dive log.
   */
  @PostMapping
  public ResponseEntity<?> createDiveLog(@Valid @RequestBody DiveLogDTO diveLogDTO, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return handleValidationErrors(bindingResult);
    }
    try {
      PremiumUser currentUser = getCurrentUser();
      DiveLog diveLog = convertToEntity(diveLogDTO, currentUser); // Reuse entity conversion
      String validationError = validateDiveLog(diveLog, currentUser, false);
      if (validationError != null) {
        return ResponseEntity.badRequest().body(Map.of("error", validationError));
      }
      DiveLog savedDiveLog = diveLogService.save(diveLog);
      return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedDiveLog));
    } catch (Exception e) {
      logger.error("Error creating dive log", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create dive log"));
    }
  }

  /**
   * Update an existing dive log.
   */
  @PutMapping("/{id}")
  public ResponseEntity<?> updateDiveLog(@PathVariable Long id, @Valid @RequestBody DiveLogDTO diveLogDTO, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return handleValidationErrors(bindingResult);
    }
    try {
      PremiumUser currentUser = getCurrentUser();
      DiveLog existingDiveLog = diveLogService.findById(id).orElseThrow(() -> new RuntimeException("Dive log not found"));
      verifyOwnership(existingDiveLog, currentUser);
      DiveLog updatedDiveLog = convertToEntity(diveLogDTO, currentUser); // Reuse entity conversion
      updatedDiveLog.setId(existingDiveLog.getId()); // Preserve the ID
      String validationError = validateDiveLog(updatedDiveLog, currentUser, true);
      if (validationError != null) {
        return ResponseEntity.badRequest().body(Map.of("error", validationError));
      }
      diveLogService.save(updatedDiveLog);
      return ResponseEntity.ok(convertToDTO(updatedDiveLog));
    } catch (Exception e) {
      logger.error("Error updating dive log", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update dive log"));
    }
  }

  /**
   * Delete a dive log.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteDiveLog(@PathVariable Long id) {
    try {
      PremiumUser currentUser = getCurrentUser();
      DiveLog diveLog = diveLogService.findById(id).orElseThrow(() -> new RuntimeException("Dive log not found"));
      verifyOwnership(diveLog, currentUser);
      diveLogService.deleteById(id);
      return ResponseEntity.ok(Map.of("message", "Dive log deleted successfully"));
    } catch (Exception e) {
      logger.error("Error deleting dive log", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to delete dive log"));
    }
  }

  /**
   * Get all unique locations for the current user.
   */
  @GetMapping("/locations")
  public ResponseEntity<?> getUserLocations() {
    try {
      PremiumUser currentUser = getCurrentUser();
      List<String> locations = diveLogService.findByUser(currentUser).stream()
          .map(DiveLog::getLocation)
          .distinct()
          .sorted()
          .collect(Collectors.toList());
      return ResponseEntity.ok(locations);
    } catch (Exception e) {
      logger.error("Error retrieving locations", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to retrieve locations"));
    }
  }

  /**
   * Validate dive log business rules
   */
  private String validateDiveLog(DiveLog diveLog, PremiumUser user, boolean isUpdate) {
    // Validate time logic
    if (diveLog.getStartTime() != null && diveLog.getEndTime() != null) {
      if (diveLog.getEndTime().isBefore(diveLog.getStartTime()) ||
          diveLog.getEndTime().isEqual(diveLog.getStartTime())) {
        return "End time must be after start time";
      }
      long durationMinutes = java.time.Duration.between(
          diveLog.getStartTime(),
          diveLog.getEndTime()
      ).toMinutes();
      if (durationMinutes > 1440) { // 24 hours
        return "Dive duration cannot exceed 24 hours";
      } else if (durationMinutes < 1) {
        return "Dive must be at least 1 minute long";
      }
    }
    // Validate dive date consistency
    if (diveLog.getDiveDate() != null && diveLog.getStartTime() != null) {
      if (!diveLog.getDiveDate().equals(diveLog.getStartTime().toLocalDate())) {
        return "Dive date must match the date of the start time";
      }
    }
    // Check for future dates
    if (diveLog.getDiveDate() != null && diveLog.getDiveDate().isAfter(LocalDate.now())) {
      return "Dive date cannot be in the future";
    }
    // Check if the dive number already exists (for new dives or when changing dive number)
    if (diveLog.getDiveNumber() != null) {
      Optional<DiveLog> existingDive = diveLogService.findByUserAndDiveNumber(user, diveLog.getDiveNumber());
      if (existingDive.isPresent() && (!isUpdate || !existingDive.get().getId().equals(diveLog.getId()))) {
        return "Dive number " + diveLog.getDiveNumber() + " already exists";
      }
    }
    // Validate temperature ranges
    if (diveLog.getWaterTemperature() != null && diveLog.getAirTemperature() != null) {
      double tempDiff = diveLog.getWaterTemperature() - diveLog.getAirTemperature();
      if (tempDiff > 20) {
        return "Water temperature seems unusually high compared to air temperature";
      }
    }
    return null; // No validation errors
  }
}

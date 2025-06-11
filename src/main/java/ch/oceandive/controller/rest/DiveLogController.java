package ch.oceandive.controller.rest;

import ch.oceandive.dto.DiveLogDTO;
import ch.oceandive.exceptionHandler.*;
import ch.oceandive.exceptionHandler.ResourceNotFoundException.UnauthorizedException;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.service.DiveLogService;
import ch.oceandive.service.PremiumUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API controller for dive log operations
 */
@RestController
@RequestMapping("/api/dive-logs")
@Tag(name = "Dive Logs", description = "Operations related to dive logs")
public class DiveLogController {

  private static final Logger logger = LoggerFactory.getLogger(DiveLogController.class);
  private final DiveLogService diveLogService;
  private final PremiumUserService premiumUserService;

  @Autowired
  public DiveLogController(DiveLogService diveLogService, PremiumUserService premiumUserService) {
    this.diveLogService = diveLogService;
    this.premiumUserService = premiumUserService;
  }


  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllDiveLogs(
      @RequestParam(required = false) String location) {
    try {
      PremiumUser currentUser = getCurrentUser();

      List<DiveLogDTO> diveLogs;
      if (location != null && !location.trim().isEmpty()) {
        diveLogs = diveLogService.findByUserAndLocation(currentUser, location.trim());
      } else {
        diveLogs = diveLogService.findByUserOrderByDiveDateDesc(currentUser);
      }

      Map<String, Object> statistics = diveLogService.getUserStatistics(currentUser);

      Map<String, Object> response = new HashMap<>();
      response.put("diveLogs", diveLogs);
      response.putAll(statistics);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Error retrieving dive logs", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to retrieve dive logs"));
    }
  }

  /**
   * GET /api/dive-logs/{id} - Get a specific dive log
   */
  @Operation(summary = "Get all dive logs for the current user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "List of dive logs"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<?> getDiveLog(@PathVariable Long id) {
    try {
      PremiumUser currentUser = getCurrentUser();
      DiveLogDTO diveLog = diveLogService.findByIdAndUser(id, currentUser);
      return ResponseEntity.ok(diveLog);
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (UnauthorizedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Access denied"));
    } catch (Exception e) {
      logger.error("Error retrieving dive log", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to retrieve dive log"));
    }
  }

  /**
   * POST /api/dive-logs - Create a new dive log
   */
  @PostMapping
  public ResponseEntity<?> createDiveLog(@Valid @RequestBody DiveLogDTO diveLogDTO,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return handleValidationErrors(bindingResult);
    }

    try {
      PremiumUser currentUser = getCurrentUser();
      DiveLogDTO createdDiveLog = diveLogService.create(diveLogDTO, currentUser);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdDiveLog);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      logger.error("Error creating dive log", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to create dive log"));
    }
  }

  /**
   * PUT /api/dive-logs/{id} - Update an existing dive log
   */
  @PutMapping("/{id}")
  public ResponseEntity<?> updateDiveLog(@PathVariable Long id,
      @Valid @RequestBody DiveLogDTO diveLogDTO,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return handleValidationErrors(bindingResult);
    }

    try {
      PremiumUser currentUser = getCurrentUser();
      DiveLogDTO updatedDiveLog = diveLogService.update(id, diveLogDTO, currentUser);
      return ResponseEntity.ok(updatedDiveLog);
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (UnauthorizedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Access denied"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      logger.error("Error updating dive log", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to update dive log"));
    }
  }

  /**
   * DELETE /api/dive-logs/{id} - Delete a dive log
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteDiveLog(@PathVariable Long id) {
    try {
      PremiumUser currentUser = getCurrentUser();
      diveLogService.delete(id, currentUser);
      return ResponseEntity.ok(Map.of("message", "Dive log deleted successfully"));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (UnauthorizedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "Access denied"));
    } catch (Exception e) {
      logger.error("Error deleting dive log", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to delete dive log"));
    }
  }

  /**
   * GET /api/dive-logs/locations - Get all unique locations for the current user
   */
  @GetMapping("/locations")
  public ResponseEntity<?> getUserLocations() {
    try {
      PremiumUser currentUser = getCurrentUser();
      List<String> locations = diveLogService.getUserLocations(currentUser);
      return ResponseEntity.ok(locations);
    } catch (Exception e) {
      logger.error("Error retrieving locations", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to retrieve locations"));
    }
  }

  /**
   * Helper method to get the current logged-in user
   */
  private PremiumUser getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return premiumUserService.getPremiumUserEntityByUsername(username);
  }

  /**
   * Helper method to handle validation errors
   */
  private ResponseEntity<?> handleValidationErrors(BindingResult bindingResult) {
    Map<String, String> errors = new HashMap<>();
    bindingResult.getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
    );
    return ResponseEntity.badRequest().body(Map.of("errors", errors));
  }
}
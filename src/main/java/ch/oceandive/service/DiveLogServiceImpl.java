package ch.oceandive.service;

import ch.oceandive.dto.DiveLogDTO;
import ch.oceandive.exceptionHandler.*;
import ch.oceandive.exceptionHandler.ResourceNotFoundException.UnauthorizedException;
import ch.oceandive.model.DiveLog;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.repository.DiveLogRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for DiveLog operations with enhanced error handling
 */
@Service
@Transactional
public class DiveLogServiceImpl implements DiveLogService {

  private static final Logger logger = LoggerFactory.getLogger(DiveLogServiceImpl.class);

  private final DiveLogRepo diveLogRepo;

  @Autowired
  public DiveLogServiceImpl(DiveLogRepo diveLogRepo) {
    this.diveLogRepo = diveLogRepo;
  }

  @Override
  public List<DiveLogDTO> findAllByUser(PremiumUser user) {
    try {
      logger.info("Finding all dive logs for user: {}", user.getUsername());
      List<DiveLog> diveLogs = diveLogRepo.findByUser(user);
      logger.info("Found {} dive logs for user {}", diveLogs.size(), user.getUsername());

      return convertToDTOList(diveLogs);
    } catch (Exception e) {
      logger.error("Error finding all dive logs for user {}: ", user.getUsername(), e);
      // Return an empty list instead of throwing exception
      return new ArrayList<>();
    }
  }

  @Override
  public List<DiveLogDTO> findByUserOrderByDiveDateDesc(PremiumUser user) {
    try {
      logger.info("Finding dive logs ordered by date for user: {}", user.getUsername());
      List<DiveLog> diveLogs = diveLogRepo.findByUserOrderByDiveDateDesc(user);
      logger.info("Found {} dive logs for user {}", diveLogs.size(), user.getUsername());

      return convertToDTOList(diveLogs);
    } catch (Exception e) {
      logger.error("Error finding dive logs by date for user {}: ", user.getUsername(), e);
      // Return empty list instead of throwing exception
      return new ArrayList<>();
    }
  }

  @Override
  public List<DiveLogDTO> findByUserAndLocation(PremiumUser user, String location) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    List<DiveLog> diveLogs;

    // Handle null, empty, or whitespace-only location
    if (location == null || location.trim().isEmpty()) {
      logger.debug("No location filter provided, returning all dive logs for user: {}", user.getUsername());
      diveLogs = diveLogRepo.findByUserOrderByDiveDateDesc(user);
    } else {
      String trimmedLocation = location.trim();
      logger.debug("Filtering dive logs by location: '{}' for user: {}", trimmedLocation, user.getUsername());

      // First try partial match (contains)
      diveLogs = diveLogRepo.findByUserAndLocationContainingIgnoreCase(user, trimmedLocation);

      // Log the results for debugging
      logger.debug("Found {} dive logs with location containing: '{}'", diveLogs.size(), trimmedLocation);
    }

    return diveLogs.stream()
        .map(this::convertToDto)
        .sorted((a, b) -> b.getDiveDate().compareTo(a.getDiveDate())) // Ensure date desc order
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getUserLocations(PremiumUser user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    try {
      // Use the new repository method if available, otherwise fall back to the original
      return diveLogRepo.findDistinctLocationsByUser(user);
    } catch (Exception e) {
      // Fallback to the original implementation
      logger.warn("Using fallback method for getUserLocations due to: {}", e.getMessage());
      return diveLogRepo.findByUser(user).stream()
          .map(DiveLog::getLocation)
          .filter(location -> location != null && !location.trim().isEmpty())
          .distinct()
          .sorted(String.CASE_INSENSITIVE_ORDER)
          .collect(Collectors.toList());
    }
  }
  @Override
  public DiveLogDTO findByDiveNumberAndUser(Integer diveNumber, PremiumUser user) {
    try {
      logger.info("Finding dive log by dive number {} for user: {}", diveNumber, user.getUsername());

      DiveLog diveLog = diveLogRepo.findByUserAndDiveNumber(user, diveNumber)
          .orElseThrow(() -> new ResourceNotFoundException("Dive log not found with dive number: " + diveNumber));

      return convertToDto(diveLog);
    } catch (ResourceNotFoundException e) {
      throw e; // Re-throw these specific exceptions
    } catch (Exception e) {
      logger.error("Error finding dive log by dive number {} for user {}: ", diveNumber, user.getUsername(), e);
      throw new RuntimeException("Failed to load dive log", e);
    }
  }

  @Override
  public DiveLogDTO create(DiveLogDTO diveLogDTO, PremiumUser user) {
    try {
      logger.info("Creating dive log for user: {}", user.getUsername());

      // Validate the dive log
      String validationError = validate(diveLogDTO, user, false);
      if (validationError != null) {
        throw new IllegalArgumentException(validationError);
      }

      // Convert DTO to entity
      DiveLog diveLog = convertToEntity(diveLogDTO);
      diveLog.setUser(user);

      // Calculate derived properties
      calculateDerivedProperties(diveLog);

      // Save and return
      DiveLog savedDiveLog = diveLogRepo.save(diveLog);
      logger.info("Created dive log #{} for user: {}", savedDiveLog.getDiveNumber(), user.getUsername());

      return convertToDto(savedDiveLog);
    } catch (IllegalArgumentException e) {
      throw e; // Re-throw validation errors
    } catch (Exception e) {
      logger.error("Error creating dive log for user {}: ", user.getUsername(), e);
      throw new RuntimeException("Failed to create dive log", e);
    }
  }

  @Override
  public DiveLogDTO update(Long id, DiveLogDTO diveLogDTO, PremiumUser user) {
    try {
      logger.info("Updating dive log {} for user: {}", id, user.getUsername());

      // Find existing dive log
      DiveLog existingDiveLog = diveLogRepo.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("Dive log not found with id: " + id));

      // Verify ownership
      if (!existingDiveLog.getUser().getId().equals(user.getId())) {
        throw new UnauthorizedException("Access denied to dive log with id: " + id);
      }

      // Validate the update
      diveLogDTO.setId(id); // Ensure ID is set for validation
      String validationError = validate(diveLogDTO, user, true);
      if (validationError != null) {
        throw new IllegalArgumentException(validationError);
      }

      // Update fields
      updateEntityFromDTO(existingDiveLog, diveLogDTO);

      // Calculate derived properties
      calculateDerivedProperties(existingDiveLog);

      // Save and return
      DiveLog updatedDiveLog = diveLogRepo.save(existingDiveLog);
      logger.info("Updated dive log #{} for user: {}", updatedDiveLog.getDiveNumber(), user.getUsername());

      return convertToDto(updatedDiveLog);
    } catch (ResourceNotFoundException | UnauthorizedException | IllegalArgumentException e) {
      throw e; // Re-throw these specific exceptions
    } catch (Exception e) {
      logger.error("Error updating dive log {} for user {}: ", id, user.getUsername(), e);
      throw new RuntimeException("Failed to update dive log", e);
    }
  }

  @Override
  public DiveLogDTO updateByDiveNumber(Integer diveNumber, DiveLogDTO diveLogDTO, PremiumUser user) {
    try {
      logger.info("Updating dive log by dive number {} for user: {}", diveNumber, user.getUsername());

      // Find existing dive log
      DiveLog existingDiveLog = diveLogRepo.findByUserAndDiveNumber(user, diveNumber)
          .orElseThrow(() -> new ResourceNotFoundException("Dive log not found with dive number: " + diveNumber));

      // Validate the update
      diveLogDTO.setId(existingDiveLog.getId()); // Ensure ID is set for validation
      String validationError = validate(diveLogDTO, user, true);
      if (validationError != null) {
        throw new IllegalArgumentException(validationError);
      }

      // Update fields
      updateEntityFromDTO(existingDiveLog, diveLogDTO);

      // Calculate derived properties
      calculateDerivedProperties(existingDiveLog);

      // Save and return
      DiveLog updatedDiveLog = diveLogRepo.save(existingDiveLog);
      logger.info("Updated dive log #{} for user: {}", updatedDiveLog.getDiveNumber(), user.getUsername());

      return convertToDto(updatedDiveLog);
    } catch (ResourceNotFoundException | IllegalArgumentException e) {
      throw e; // Re-throw these specific exceptions
    } catch (Exception e) {
      logger.error("Error updating dive log by dive number {} for user {}: ", diveNumber, user.getUsername(), e);
      throw new RuntimeException("Failed to update dive log", e);
    }
  }

  @Override
  public void delete(Long id, PremiumUser user) {
    try {
      logger.info("Deleting dive log {} for user: {}", id, user.getUsername());

      DiveLog diveLog = diveLogRepo.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("Dive log not found with id: " + id));

      if (!diveLog.getUser().getId().equals(user.getId())) {
        throw new UnauthorizedException("Access denied to dive log with id: " + id);
      }

      diveLogRepo.delete(diveLog);
      logger.info("Deleted dive log #{} for user: {}", diveLog.getDiveNumber(), user.getUsername());
    } catch (ResourceNotFoundException | UnauthorizedException e) {
      throw e; // Re-throw these specific exceptions
    } catch (Exception e) {
      logger.error("Error deleting dive log {} for user {}: ", id, user.getUsername(), e);
      throw new RuntimeException("Failed to delete dive log", e);
    }
  }

  @Override
  public void deleteByDiveNumber(Integer diveNumber, PremiumUser user) {
    try {
      logger.info("Deleting dive log by dive number {} for user: {}", diveNumber, user.getUsername());

      DiveLog diveLog = diveLogRepo.findByUserAndDiveNumber(user, diveNumber)
          .orElseThrow(() -> new ResourceNotFoundException("Dive log not found with dive number: " + diveNumber));

      diveLogRepo.delete(diveLog);
      logger.info("Deleted dive log #{} for user: {}", diveLog.getDiveNumber(), user.getUsername());
    } catch (ResourceNotFoundException e) {
      throw e; // Re-throw these specific exceptions
    } catch (Exception e) {
      logger.error("Error deleting dive log by dive number {} for user {}: ", diveNumber, user.getUsername(), e);
      throw new RuntimeException("Failed to delete dive log", e);
    }
  }

  @Override
  public Map<String, Object> getUserStatistics(PremiumUser user) {
    try {
      logger.info("Getting statistics for user: {}", user.getUsername());

      Map<String, Object> stats = new HashMap<>();

      // Get total dives count with error handling
      try {
        long totalDives = diveLogRepo.countByUser(user);
        stats.put("totalDives", totalDives);
        logger.debug("Total dives for user {}: {}", user.getUsername(), totalDives);
      } catch (Exception e) {
        logger.warn("Error getting total dives count for user {}: {}", user.getUsername(), e.getMessage());
        stats.put("totalDives", 0L);
      }

      // Get total duration with error handling
      try {
        Integer totalMinutes = diveLogRepo.sumDurationByUser(user);
        Double totalHours = (totalMinutes != null && totalMinutes > 0) ? totalMinutes / 60.0 : 0.0;
        stats.put("totalHours", Math.round(totalHours * 100.0) / 100.0); // Round to 2 decimal places
        logger.debug("Total hours for user {}: {}", user.getUsername(), totalHours);
      } catch (Exception e) {
        logger.warn("Error getting total duration for user {}: {}", user.getUsername(), e.getMessage());
        stats.put("totalHours", 0.0);
      }

      // Get unique locations count with error handling
      try {
        Long uniqueLocations = diveLogRepo.countDistinctLocationsByUser(user);
        stats.put("uniqueLocations", uniqueLocations != null ? uniqueLocations : 0L);
        logger.debug("Unique locations for user {}: {}", user.getUsername(), uniqueLocations);
      } catch (Exception e) {
        logger.warn("Error getting unique locations count for user {}: {}", user.getUsername(), e.getMessage());
        stats.put("uniqueLocations", 0L);
      }

      return stats;
    } catch (Exception e) {
      logger.error("Error calculating statistics for user {}: ", user.getUsername(), e);
      // Return safe defaults instead of throwing
      Map<String, Object> defaultStats = new HashMap<>();
      defaultStats.put("totalDives", 0L);
      defaultStats.put("totalHours", 0.0);
      defaultStats.put("uniqueLocations", 0L);
      return defaultStats;
    }
  }


  @Override
  public String validate(DiveLogDTO diveLogDTO, PremiumUser user, boolean isUpdate) {
    try {
      // Validate time logic with safe time handling
      if (diveLogDTO.getStartTime() != null && diveLogDTO.getEndTime() != null) {
        LocalTime startTime = normalizeTime(diveLogDTO.getStartTime());
        LocalTime endTime = normalizeTime(diveLogDTO.getEndTime());

        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
          return "End time must be after start time";
        }

        long durationMinutes = Duration.between(startTime, endTime).toMinutes();

        if (durationMinutes > 720) { // 12 hours max
          return "Dive duration cannot exceed 12 hours";
        }
        if (durationMinutes < 1) { // Minimum 1 minute
          return "Dive must be at least 1 minute long";
        }
      }
      // Validate dive date
      if (diveLogDTO.getDiveDate() != null) {
        if (diveLogDTO.getDiveDate().isAfter(LocalDate.now())) {
          return "Dive date cannot be in the future";
        }
        LocalDate fiftyYearsAgo = LocalDate.now().minusYears(50);
        if (diveLogDTO.getDiveDate().isBefore(fiftyYearsAgo)) {
          return "Dive date seems too far in the past";
        }
      }

      // Check dive number uniqueness
      if (diveLogDTO.getDiveNumber() != null) {
        Optional<DiveLog> existingDive = diveLogRepo.findByUserAndDiveNumber(user, diveLogDTO.getDiveNumber());
        if (existingDive.isPresent() && (!isUpdate || !existingDive.get().getId().equals(diveLogDTO.getId()))) {
          return "Dive number " + diveLogDTO.getDiveNumber() + " already exists";
        }
      }

      // Temperature validation
      if (diveLogDTO.getWaterTemperature() != null && diveLogDTO.getAirTemperature() != null) {
        double tempDiff = Math.abs(diveLogDTO.getWaterTemperature() - diveLogDTO.getAirTemperature());
        if (tempDiff > 30) { // More reasonable difference
          return "Temperature difference between water and air seems unusual";
        }
      }

      return null; // No validation errors

    } catch (Exception e) {
      logger.error("Error during validation: ", e);
      return "Validation error: " + e.getMessage();
    }
  }

  // ===== PRIVATE HELPER METHODS =====

  /**
   * Safely convert a list of DiveLog entities to DTOs, skipping any that fail conversion
   */
  private List<DiveLogDTO> convertToDTOList(List<DiveLog> diveLogs) {
    List<DiveLogDTO> result = new ArrayList<>();
    int conversionErrors = 0;

    for (DiveLog diveLog : diveLogs) {
      try {
        DiveLogDTO dto = convertToDto(diveLog);
        result.add(dto);
      } catch (Exception e) {
        conversionErrors++;
        logger.error("Failed to convert dive log #{} (ID: {}) to DTO: {}",
            diveLog.getDiveNumber(), diveLog.getId(), e.getMessage());
        logger.debug("Problematic dive log data: startTime={}, endTime={}, duration={}",
            diveLog.getStartTime(), diveLog.getEndTime(), diveLog.getDuration());
      }
    }

    if (conversionErrors > 0) {
      logger.warn("Skipped {} dive logs due to conversion errors", conversionErrors);
    }

    logger.info("Successfully converted {} out of {} dive logs", result.size(), diveLogs.size());
    return result;
  }

  /**
   * Safely convert DiveLog to DTO with enhanced error handling
   */
  private DiveLogDTO convertToDto(DiveLog diveLog) {
    try {
      return DiveLogDTO.builder()
          .id(diveLog.getId())
          .diveNumber(diveLog.getDiveNumber())
          .location(diveLog.getLocation())
          .startTime(normalizeTime(diveLog.getStartTime()))
          .endTime(normalizeTime(diveLog.getEndTime()))
          .duration(diveLog.getDuration())
          .waterTemperature(diveLog.getWaterTemperature())
          .airTemperature(diveLog.getAirTemperature())
          .notes(diveLog.getNotes())
          .diveDate(diveLog.getDiveDate())
          .build();
    } catch (Exception e) {
      logger.error("Error converting DiveLog (ID: {}) to DTO: {}", diveLog.getId(), e.getMessage(), e);
      throw new RuntimeException("Failed to convert dive log data for dive #" + diveLog.getDiveNumber(), e);
    }
  }

  /**
   * Safely convert DTO to DiveLog entity
   */
  private DiveLog convertToEntity(DiveLogDTO dto) {
    try {
      DiveLog diveLog = new DiveLog();
      diveLog.setId(dto.getId());
      diveLog.setDiveNumber(dto.getDiveNumber());
      diveLog.setLocation(dto.getLocation() != null ? dto.getLocation().trim() : null);
      diveLog.setStartTime(normalizeTime(dto.getStartTime()));
      diveLog.setEndTime(normalizeTime(dto.getEndTime()));
      diveLog.setDuration(dto.getDuration());
      diveLog.setWaterTemperature(dto.getWaterTemperature());
      diveLog.setAirTemperature(dto.getAirTemperature());
      diveLog.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : null);
      diveLog.setDiveDate(dto.getDiveDate());
      return diveLog;
    } catch (Exception e) {
      logger.error("Error converting DTO to DiveLog entity: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to convert dive log data", e);
    }
  }

  /**
   * Normalize time to remove microseconds and handle null values
   */
  private LocalTime normalizeTime(LocalTime time) {
    if (time == null) {
      return null;
    }
    try {
      // Truncate to seconds to remove microseconds/nanoseconds
      return time.truncatedTo(ChronoUnit.SECONDS);
    } catch (Exception e) {
      logger.warn("Error normalizing time {}: {}. Using fallback method.", time, e.getMessage());
      // Fallback: create new LocalTime with just hours, minutes, seconds
      try {
        return LocalTime.of(time.getHour(), time.getMinute(), time.getSecond());
      } catch (Exception fallbackError) {
        logger.error("Fallback time normalization also failed for time {}: {}", time, fallbackError.getMessage());
        // Last resort: return a safe default time
        return LocalTime.of(12, 0, 0);
      }
    }
  }

  private void updateEntityFromDTO(DiveLog entity, DiveLogDTO dto) {
    try {
      entity.setDiveNumber(dto.getDiveNumber());
      entity.setLocation(dto.getLocation());
      entity.setStartTime(normalizeTime(dto.getStartTime()));
      entity.setEndTime(normalizeTime(dto.getEndTime()));
      entity.setDuration(dto.getDuration());
      entity.setWaterTemperature(dto.getWaterTemperature());
      entity.setAirTemperature(dto.getAirTemperature());
      entity.setNotes(dto.getNotes());
      entity.setDiveDate(dto.getDiveDate());
    } catch (Exception e) {
      logger.error("Error updating entity from DTO: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to update dive log data", e);
    }
  }

  private void calculateDerivedProperties(DiveLog diveLog) {
    try {
      // Only calculate duration if not already set and times are available
      if (diveLog.getDuration() == null &&
          diveLog.getStartTime() != null &&
          diveLog.getEndTime() != null) {

        LocalTime startTime = normalizeTime(diveLog.getStartTime());
        LocalTime endTime = normalizeTime(diveLog.getEndTime());

        long durationMinutes = Duration.between(startTime, endTime).toMinutes();

        // Handle edge cases
        if (durationMinutes < 0) {
          logger.warn("Negative duration calculated for dive #{}: {} minutes. " +
                  "Start: {}, End: {}. Setting to 0.",
              diveLog.getDiveNumber(), durationMinutes, startTime, endTime);
          durationMinutes = 0;
        } else if (durationMinutes > 720) { // 12 hours
          logger.warn("Very long duration calculated for dive #{}: {} minutes. " +
                  "Start: {}, End: {}.",
              diveLog.getDiveNumber(), durationMinutes, startTime, endTime);
        }

        diveLog.setDuration((int) durationMinutes);
        logger.debug("Calculated duration for dive #{}: {} minutes",
            diveLog.getDiveNumber(), durationMinutes);
      }
    } catch (Exception e) {
      logger.error("Error calculating derived properties for dive #{}: {}",
          diveLog.getDiveNumber(), e.getMessage(), e);
      // Set a safe default duration instead of throwing
      if (diveLog.getDuration() == null) {
        diveLog.setDuration(0);
        logger.info("Set default duration of 0 for dive #{}", diveLog.getDiveNumber());
      }
    }
  }
}
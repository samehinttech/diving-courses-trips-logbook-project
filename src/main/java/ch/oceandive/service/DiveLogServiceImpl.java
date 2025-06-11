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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for DiveLog operations
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
    return diveLogRepo.findByUser(user).stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<DiveLogDTO> findByUserOrderByDiveDateDesc(PremiumUser user) {
    return diveLogRepo.findByUserOrderByDiveDateDesc(user).stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<DiveLogDTO> findByUserAndLocation(PremiumUser user, String location) {
    return diveLogRepo.findByUserAndLocationContainingIgnoreCase(user, location).stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public DiveLogDTO findByIdAndUser(Long id, PremiumUser user) {
    DiveLog diveLog = diveLogRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Dive log not found with id: " + id));

    if (!diveLog.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedException("Access denied to dive log with id: " + id);
    }

    return toDTO(diveLog);
  }

  @Override
  public DiveLogDTO create(DiveLogDTO diveLogDTO, PremiumUser user) {
    // Validate the dive log
    String validationError = validate(diveLogDTO, user, false);
    if (validationError != null) {
      throw new IllegalArgumentException(validationError);
    }

    // Convert DTO to entity
    DiveLog diveLog = toEntity(diveLogDTO);
    diveLog.setUser(user);

    // Calculate derived properties
    calculateDerivedProperties(diveLog);

    // Save and return
    DiveLog savedDiveLog = diveLogRepo.save(diveLog);
    logger.info("Created dive log #{} for user: {}", savedDiveLog.getDiveNumber(), user.getUsername());

    return toDTO(savedDiveLog);
  }

  @Override
  public DiveLogDTO update(Long id, DiveLogDTO diveLogDTO, PremiumUser user) {
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

    return toDTO(updatedDiveLog);
  }

  @Override
  public void delete(Long id, PremiumUser user) {
    DiveLog diveLog = diveLogRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Dive log not found with id: " + id));

    if (!diveLog.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedException("Access denied to dive log with id: " + id);
    }

    diveLogRepo.delete(diveLog);
    logger.info("Deleted dive log #{} for user: {}", diveLog.getDiveNumber(), user.getUsername());
  }

  @Override
  public Map<String, Object> getUserStatistics(PremiumUser user) {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalDives", diveLogRepo.countByUser(user));

    Integer totalMinutes = diveLogRepo.sumDurationByUser(user);
    Double totalHours = totalMinutes != null ? totalMinutes / 60.0 : 0.0;
    stats.put("totalHours", totalHours);

    stats.put("uniqueLocations", diveLogRepo.countDistinctLocationsByUser(user));

    return stats;
  }

  @Override
  public List<String> getUserLocations(PremiumUser user) {
    return diveLogRepo.findByUser(user).stream()
        .map(DiveLog::getLocation)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  @Override
  public String validate(DiveLogDTO diveLogDTO, PremiumUser user, boolean isUpdate) {
    // Validate time logic
    if (diveLogDTO.getStartTime() != null && diveLogDTO.getEndTime() != null) {
      if (diveLogDTO.getEndTime().isBefore(diveLogDTO.getStartTime()) ||
          diveLogDTO.getEndTime().equals(diveLogDTO.getStartTime())) {
        return "End time must be after start time";
      }

      long durationMinutes = Duration.between(diveLogDTO.getStartTime(), diveLogDTO.getEndTime())
          .toMinutes();
      if (durationMinutes > 1440) {
        return "Dive duration cannot exceed 24 hours";
      } else if (durationMinutes < 1) {
        return "Dive must be at least 1 minute long";
      }
    }
    // Validate dive date consistency
    if (diveLogDTO.getDiveDate() != null && diveLogDTO.getDiveDate().isAfter(LocalDate.now())) {
      return "Dive date cannot be in the future";
    }

    // Check if dive number already exists
    if (diveLogDTO.getDiveNumber() != null) {
      Optional<DiveLog> existingDive = diveLogRepo.findByUserAndDiveNumber(user, diveLogDTO.getDiveNumber());
      if (existingDive.isPresent() && (!isUpdate || !existingDive.get().getId().equals(diveLogDTO.getId()))) {
        return "Dive number " + diveLogDTO.getDiveNumber() + " already exists";
      }
    }

    // Validate temperature ranges
    if (diveLogDTO.getWaterTemperature() != null && diveLogDTO.getAirTemperature() != null) {
      double tempDiff = diveLogDTO.getWaterTemperature() - diveLogDTO.getAirTemperature();
      if (tempDiff > 20) {
        return "Water temperature seems unusually high compared to air temperature";
      }
    }

    return null;
  }

  // Private helper methods

  private DiveLogDTO toDTO(DiveLog diveLog) {
    return DiveLogDTO.builder()
        .id(diveLog.getId())
        .diveNumber(diveLog.getDiveNumber())
        .location(diveLog.getLocation())
        .startTime(diveLog.getStartTime())
        .endTime(diveLog.getEndTime())
        .duration(diveLog.getDuration())
        .waterTemperature(diveLog.getWaterTemperature())
        .airTemperature(diveLog.getAirTemperature())
        .notes(diveLog.getNotes())
        .diveDate(diveLog.getDiveDate())
        .build();
  }

  private DiveLog toEntity(DiveLogDTO dto) {
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
    return diveLog;
  }

  private void updateEntityFromDTO(DiveLog entity, DiveLogDTO dto) {
    entity.setDiveNumber(dto.getDiveNumber());
    entity.setLocation(dto.getLocation());
    entity.setStartTime(dto.getStartTime());
    entity.setEndTime(dto.getEndTime());
    entity.setDuration(dto.getDuration());
    entity.setWaterTemperature(dto.getWaterTemperature());
    entity.setAirTemperature(dto.getAirTemperature());
    entity.setNotes(dto.getNotes());
    entity.setDiveDate(dto.getDiveDate());
  }

  private void calculateDerivedProperties(DiveLog diveLog) {
    if (diveLog.getDuration() == null && diveLog.getStartTime() != null && diveLog.getEndTime() != null) {
      long durationMinutes = Duration.between(diveLog.getStartTime(), diveLog.getEndTime()).toMinutes();
      diveLog.setDuration((int) durationMinutes);
    }
  }
}
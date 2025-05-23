package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.exceptionHandler.BusinessRuleViolationException;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.DiveLog;
import ch.fhnw.oceandive.model.PremiumUser;
import ch.fhnw.oceandive.repository.DiveLogRepo;
import ch.fhnw.oceandive.repository.PremiumUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing DiveLog entities.
 */
@Service
public class DiveLogService {

    private final DiveLogRepo diveLogRepo;
    private final PremiumUserRepo premiumUserRepo;

    @Autowired
    public DiveLogService(DiveLogRepo diveLogRepo, PremiumUserRepo premiumUserRepo) {
        this.diveLogRepo = diveLogRepo;
        this.premiumUserRepo = premiumUserRepo;
    }

    // CRUD operations for DiveLog
    // Get all dive logs with pagination
    public List<DiveLog> getAllDiveLogs() {
        return diveLogRepo.findAll();
    }
    
    // Get all dive logs with pagination
    public Page<DiveLog> getAllDiveLogs(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return diveLogRepo.findAll(pageable);
    }

   // Get a dive log by ID.
    public DiveLog getDiveLogById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Dive log ID cannot be null");
        }
        return diveLogRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dive log not found with id: " + id));
    }

   /// Get dive logs by user.
    public List<DiveLog> getDiveLogsByPremiumUser(PremiumUser premiumUser) {
        if (premiumUser == null) {
            throw new IllegalArgumentException("Premium user cannot be null");
        }
        return diveLogRepo.findByPremiumUser(premiumUser);
    }

    // Get dive logs by user ID.
    public List<DiveLog> getDiveLogsByPremiumUserId(Long premiumUserId) {
        if (premiumUserId == null) {
            throw new IllegalArgumentException("Premium user ID cannot be null");
        }
        PremiumUser premiumUser = premiumUserRepo.findById(premiumUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Premium user not found with id: " + premiumUserId));
        return diveLogRepo.findByPremiumUser(premiumUser);
    }

    // Get dive logs by location.
    public List<DiveLog> getDiveLogsByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        return diveLogRepo.findByLocationContainingIgnoreCase(location);
    }

   // Get dive logs with start time after the given date time.
    public List<DiveLog> getDiveLogsByStartTimeAfter(LocalDateTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        return diveLogRepo.findByStartTimeAfter(startTime);
    }

    // Get dive logs with start time before the given date time.
    public List<DiveLog> getDiveLogsByStartTimeBefore(LocalDateTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        return diveLogRepo.findByStartTimeBefore(startTime);
    }

    // Get dive logs with start time between the given date times.
    public List<DiveLog> getDiveLogsByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        return diveLogRepo.findByStartTimeBetween(startTime, endTime);
    }

    // Get dive logs by user and order by dive number descending.
    public List<DiveLog> getDiveLogsByPremiumUserOrderByDiveNumberDesc(PremiumUser premiumUser) {
        if (premiumUser == null) {
            throw new IllegalArgumentException("Premium user cannot be null");
        }
        return diveLogRepo.findByPremiumUserOrderByDiveNumberDesc(premiumUser);
    }

 
    // Create a new dive log for that user ID.
    @Transactional
    public DiveLog createDiveLog(DiveLog diveLog, Long premiumUserId) {
        if (diveLog == null) {
            throw new IllegalArgumentException("Dive log cannot be null");
        }
        if (premiumUserId == null) {
            throw new IllegalArgumentException("Premium user ID cannot be null");
        }
        
        PremiumUser premiumUser = premiumUserRepo.findById(premiumUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Premium user not found with id: " + premiumUserId));

        // Validate dive log data
        validateDiveLog(diveLog);

        // Set diveDate from startTime if not already set
        if (diveLog.getDiveDate() == null && diveLog.getStartTime() != null) {
            diveLog.setDiveDate(diveLog.getStartTime().toLocalDate());
        }

        diveLog.setPremiumUser(premiumUser);
        return diveLogRepo.save(diveLog);
    }

   // Update an existing dive log.
    @Transactional
    public DiveLog updateDiveLog(Long id, DiveLog diveLogDetails, Long currentUserId) {
        DiveLog diveLog = getDiveLogById(id);

        // Check if the current user is the owner of the dive log
        if (!diveLog.getPremiumUser().getId().equals(currentUserId)) {
            throw new BusinessRuleViolationException("You are not authorized to update this dive log");
        }

        // Validate dive log data
        validateDiveLog(diveLogDetails);

        diveLog.setDiveNumber(diveLogDetails.getDiveNumber());
        diveLog.setLocation(diveLogDetails.getLocation());
        diveLog.setStartTime(diveLogDetails.getStartTime());
        diveLog.setEndTime(diveLogDetails.getEndTime());
        diveLog.setWaterTemperature(diveLogDetails.getWaterTemperature());
        diveLog.setAirTemperature(diveLogDetails.getAirTemperature());
        diveLog.setNotes(diveLogDetails.getNotes());

        // Update diveDate from startTime if not provided
        if (diveLogDetails.getDiveDate() != null) {
            diveLog.setDiveDate(diveLogDetails.getDiveDate());
        } else if (diveLogDetails.getStartTime() != null) {
            diveLog.setDiveDate(diveLogDetails.getStartTime().toLocalDate());
        }

        return diveLogRepo.save(diveLog);
    }

   // Delete a dive log by ID for the current logged-in user.
    @Transactional
    public void deleteDiveLog(Long id, Long currentUserId) {
        DiveLog diveLog = getDiveLogById(id);

        // Check if the current user is the owner of the dive log
        if (!diveLog.getPremiumUser().getId().equals(currentUserId)) {
            throw new BusinessRuleViolationException("You are not authorized to delete this dive log");
        }

        diveLogRepo.delete(diveLog);
    }

    // Method to validate the dive log data
    private void validateDiveLog(DiveLog diveLog) {
        if (diveLog.getStartTime() == null) {
            throw new BusinessRuleViolationException("Start time is required");
        }

        if (diveLog.getEndTime() == null) {
            throw new BusinessRuleViolationException("End time is required");
        }

        if (diveLog.getEndTime().isBefore(diveLog.getStartTime())) {
            throw new BusinessRuleViolationException("End time cannot be before start time");
        }

        // Ensure diveDate is set or can be derived from startTime
        if (diveLog.getDiveDate() == null && diveLog.getStartTime() == null) {
            throw new BusinessRuleViolationException("Either dive date or start time must be provided");
        }
    }
}

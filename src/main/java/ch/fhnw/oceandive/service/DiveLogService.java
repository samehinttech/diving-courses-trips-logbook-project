package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.exceptionHandler.BusinessRuleViolationException;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.DiveLog;
import ch.fhnw.oceandive.model.PremiumUser;
import ch.fhnw.oceandive.repository.DiveLogRepo;
import ch.fhnw.oceandive.repository.PremiumUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Get all dive logs.
     *
     * @return List of all dive logs
     */
    public List<DiveLog> getAllDiveLogs() {
        return diveLogRepo.findAll();
    }

    /**
     * Get a dive log by ID.
     * @throws ResourceNotFoundException if the dive log is not found
     */
    public DiveLog getDiveLogById(Long id) {
        return diveLogRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dive log not found with id: " + id));
    }

    /**
     * Get dive logs by premium user.
     * @return List of dive logs for the given premium user
     */
    public List<DiveLog> getDiveLogsByPremiumUser(PremiumUser premiumUser) {
        return diveLogRepo.findByPremiumUser(premiumUser);
    }

    /**
     * Get dive logs by premium user ID.
     * @throws ResourceNotFoundException if the premium user is not found
     */
    public List<DiveLog> getDiveLogsByPremiumUserId(Long premiumUserId) {
        PremiumUser premiumUser = premiumUserRepo.findById(premiumUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Premium user not found with id: " + premiumUserId));
        return diveLogRepo.findByPremiumUser(premiumUser);
    }

    /**
     * Get dive logs by location.
     * @return List of dive logs with the given location
     */
    public List<DiveLog> getDiveLogsByLocation(String location) {
        return diveLogRepo.findByLocationContainingIgnoreCase(location);
    }

    /**
     * Get dive logs with start time after the given date time.
     * @return List of dive logs starting after the given date time
     */
    public List<DiveLog> getDiveLogsByStartTimeAfter(LocalDateTime startTime) {
        return diveLogRepo.findByStartTimeAfter(startTime);
    }

    /**
     * Get dive logs with start time before the given date time.
     * @return List of dive logs starting before the given date time
     */
    public List<DiveLog> getDiveLogsByStartTimeBefore(LocalDateTime startTime) {
        return diveLogRepo.findByStartTimeBefore(startTime);
    }

    /**
     * Get dive logs with start time between the given date times.
     * @return List of dive logs starting between the given date times
     */
    public List<DiveLog> getDiveLogsByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return diveLogRepo.findByStartTimeBetween(startTime, endTime);
    }

    /**
     * Get dive logs by premium user ordered by dive number.
     * @return List of dive logs for the given premium user ordered by dive number
     */
    public List<DiveLog> getDiveLogsByPremiumUserOrderByDiveNumberDesc(PremiumUser premiumUser) {
        return diveLogRepo.findByPremiumUserOrderByDiveNumberDesc(premiumUser);
    }

    /**
     * Create a new dive log.
     * @throws ResourceNotFoundException if the premium user is not found
     */
    @Transactional
    public DiveLog createDiveLog(DiveLog diveLog, Long premiumUserId) {
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

    /**
     * Update an existing dive log.
     * @throws ResourceNotFoundException if the dive log is not found
     * @throws BusinessRuleViolationException if the current user is not the owner of the dive log
     */
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

    /**
     * Delete a dive log by ID.
     * @throws ResourceNotFoundException if the dive log is not found
     * @throws BusinessRuleViolationException if the current user is not the owner of the dive log
     */
    @Transactional
    public void deleteDiveLog(Long id, Long currentUserId) {
        DiveLog diveLog = getDiveLogById(id);

        // Check if the current user is the owner of the dive log
        if (!diveLog.getPremiumUser().getId().equals(currentUserId)) {
            throw new BusinessRuleViolationException("You are not authorized to delete this dive log");
        }

        diveLogRepo.delete(diveLog);
    }

    /**
     * Validate dive log data.
     * @param diveLog The dive log to validate
     * @throws BusinessRuleViolationException if the dive log data is invalid
     */
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

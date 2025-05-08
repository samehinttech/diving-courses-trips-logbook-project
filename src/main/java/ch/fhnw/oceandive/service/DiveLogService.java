package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.dto.DiveLogDTO;
import ch.fhnw.oceandive.model.DiveLog;
import ch.fhnw.oceandive.model.UserEntity;
import ch.fhnw.oceandive.repository.DiveLogRepository;
import ch.fhnw.oceandive.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing dive logs in the application.
 */
@Service
public class DiveLogService {

    private final DiveLogRepository diveLogRepository;
    private final UserRepository userRepository;

    @Autowired
    public DiveLogService(DiveLogRepository diveLogRepository, UserRepository userRepository) {
        this.diveLogRepository = diveLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get a dive log by ID.
     * @throws EntityNotFoundException if no dive log with the given ID exists
     */
    @Transactional(readOnly = true)
    public DiveLogDTO getDiveLogById(Long id) {
        DiveLog diveLog = diveLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dive log not found with id: " + id));
        return DiveLogDTO.fromEntity(diveLog);
    }

    /**
     * Get all dive logs for a user.
     */
    @Transactional(readOnly = true)
    public List<DiveLogDTO> getAllDiveLogsByUser(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return diveLogRepository.findAllByUserOrderByDiveNumberDesc(user).stream()
                .map(DiveLogDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get dive logs for a user within a date range.
     */
    @Transactional(readOnly = true)
    public List<DiveLogDTO> getDiveLogsByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return diveLogRepository.findAllByUserAndDiveDateBetween(user, startDate, endDate).stream()
                .map(DiveLogDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a new dive log.
     */
    @Transactional
    public DiveLogDTO createDiveLog(DiveLogDTO diveLogDTO, String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Check if dive number already exists for this user
        if (diveLogRepository.existsByDiveNumberAndUser(diveLogDTO.getDiveNumber(), user)) {
            throw new IllegalArgumentException("Dive number " + diveLogDTO.getDiveNumber() + " already exists for this user");
        }

        DiveLog diveLog = DiveLogDTO.toEntity(diveLogDTO, user);
        DiveLog savedDiveLog = diveLogRepository.save(diveLog);
        return DiveLogDTO.fromEntity(savedDiveLog);
    }

    /**
     * Update an existing dive log.
     * @throws EntityNotFoundException if no dive log with the given ID exists
     */
    @Transactional
    public DiveLogDTO updateDiveLog(Long id, DiveLogDTO diveLogDTO, String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        DiveLog existingDiveLog = diveLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dive log not found with id: " + id));

        // Check if the dive log belongs to the user
        if (!existingDiveLog.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Dive log does not belong to the user");
        }

        // Check if the new dive number already exists for this user (if changed)
        if (!existingDiveLog.getDiveNumber().equals(diveLogDTO.getDiveNumber()) && 
                diveLogRepository.existsByDiveNumberAndUser(diveLogDTO.getDiveNumber(), user)) {
            throw new IllegalArgumentException("Dive number " + diveLogDTO.getDiveNumber() + " already exists for this user");
        }

        DiveLog updatedDiveLog = DiveLogDTO.toEntity(diveLogDTO, user);
        updatedDiveLog.setId(id); // Ensure ID is preserved
        DiveLog savedDiveLog = diveLogRepository.save(updatedDiveLog);
        return DiveLogDTO.fromEntity(savedDiveLog);
    }

    /**
     * Delete a dive log.
     * @throws EntityNotFoundException if no dive log with the given ID exists
     */
    @Transactional
    public void deleteDiveLog(Long id, String userId) {
        DiveLog diveLog = diveLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dive log not found with id: " + id));

        // Check if the dive log belongs to the user
        if (!diveLog.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Dive log does not belong to the user");
        }

        diveLogRepository.deleteById(id);
    }

    /**
     * Delete a dive log by dive number.
     * @throws EntityNotFoundException if no dive log with the given dive number exists for the user
     */
    @Transactional
    public void deleteDiveLogByDiveNumber(Integer diveNumber, String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        DiveLog diveLog = diveLogRepository.findByDiveNumberAndUser(diveNumber, user)
                .orElseThrow(() -> new EntityNotFoundException("Dive log not found with dive number: " + diveNumber));

        diveLogRepository.delete(diveLog);
    }

    /**
     * Delete all dive logs for a user.
     * Only for admin use when the account has been expired for more than 30 days.
     * @throws IllegalArgumentException if the account has not been expired for more than 30 days
     */
    @Transactional
    public void deleteAllDiveLogsByUser(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Check if the account has been expired for more than 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime userIssuedOn = user.getIssuedOn();

        // If the account was created less than 30 days ago, it's not considered expired
        if (userIssuedOn.isAfter(thirtyDaysAgo)) {
            throw new IllegalArgumentException("Cannot delete dive logs for an account that has not been expired for more than 30 days");
        }

        List<DiveLog> diveLogs = diveLogRepository.findAllByUser(user);
        diveLogRepository.deleteAll(diveLogs);
    }

    /**
     * Get the next dive number for a user.
     */
    @Transactional(readOnly = true)
    public Integer getNextDiveNumber(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return diveLogRepository.findFirstByUserOrderByDiveNumberDesc(user)
                .map(diveLog -> diveLog.getDiveNumber() + 1)
                .orElse(1); // Start with 1 if no dive logs exist
    }
}

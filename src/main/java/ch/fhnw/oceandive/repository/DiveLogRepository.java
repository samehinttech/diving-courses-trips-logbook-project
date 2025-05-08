package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.DiveLog;
import ch.fhnw.oceandive.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing dive logs in the application.
 */
@Repository
@Component
public interface DiveLogRepository extends JpaRepository<DiveLog, Long> {
    
    /**
     * Find a dive log by its dive number and user.
     */
    Optional<DiveLog> findByDiveNumberAndUser(Integer diveNumber, UserEntity user);
    
    /**
     * Find all dive logs for a specific user.
     */
    List<DiveLog> findAllByUser(UserEntity user);
    
    /**
     * Find all dive logs for a specific user, ordered by dive number.
     */
    List<DiveLog> findAllByUserOrderByDiveNumberDesc(UserEntity user);
    
    /**
     * Find all dive logs for a specific user within a date range.
     */
    List<DiveLog> findAllByUserAndDiveDateBetween(UserEntity user, LocalDate startDate, LocalDate endDate);
    
    /**
     * Check if a dive log with the given dive number exists for the user.
     */
    boolean existsByDiveNumberAndUser(Integer diveNumber, UserEntity user);
    
    /**
     * Find the highest dive number for a user.
     */
    Optional<DiveLog> findFirstByUserOrderByDiveNumberDesc(UserEntity user);
}
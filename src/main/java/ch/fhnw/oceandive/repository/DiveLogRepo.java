package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.DiveLog;
import ch.fhnw.oceandive.model.PremiumUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for DiveLog entities.
 */
@Repository
public interface DiveLogRepo extends JpaRepository<DiveLog, Long> {
    
    /**
     * Find dive logs by premium user.
     * 
     * @param premiumUser The premium user
     * @return List of dive logs for the given premium user
     */
    List<DiveLog> findByPremiumUser(PremiumUser premiumUser);
    
    /**
     * Find dive logs by premium user ID.
     * 
     * @param premiumUserId The premium user ID
     * @return List of dive logs for the given premium user ID
     */
    List<DiveLog> findByPremiumUserId(Long premiumUserId);
    
    /**
     * Find dive logs by location containing the given string (case insensitive).
     * 
     * @param location The location to search for
     * @return List of dive logs with locations containing the given string
     */
    List<DiveLog> findByLocationContainingIgnoreCase(String location);
    
    /**
     * Find dive logs with start time after the given date time.
     * 
     * @param startTime The date time to compare with
     * @return List of dive logs starting after the given date time
     */
    List<DiveLog> findByStartTimeAfter(LocalDateTime startTime);
    
    /**
     * Find dive logs with start time before the given date time.
     * 
     * @param startTime The date time to compare with
     * @return List of dive logs starting before the given date time
     */
    List<DiveLog> findByStartTimeBefore(LocalDateTime startTime);
    
    /**
     * Find dive logs with start time between the given date times.
     * 
     * @param startTime The start date time
     * @param endTime The end date time
     * @return List of dive logs starting between the given date times
     */
    List<DiveLog> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find dive logs by premium user ordered by dive number.
     * 
     * @param premiumUser The premium user
     * @return List of dive logs for the given premium user ordered by dive number
     */
    List<DiveLog> findByPremiumUserOrderByDiveNumberDesc(PremiumUser premiumUser);
}
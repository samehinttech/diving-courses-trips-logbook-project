package ch.oceandive.repository;

import ch.oceandive.model.DiveLog;
import ch.oceandive.model.PremiumUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for DiveLog entities.
 */
@Repository
public interface DiveLogRepo extends JpaRepository<DiveLog, Long> {

    // Find all dive logs for a specific user
    List<DiveLog> findByUser(PremiumUser user);

    // Find all dive logs for a specific user, ordered by dive date descending
    List<DiveLog> findByUserOrderByDiveDateDesc(PremiumUser user);

    // Enhanced location filtering methods
    List<DiveLog> findByUserAndLocationContainingIgnoreCase(PremiumUser user, String location);

    // Find a specific dive log by user and dive number
    Optional<DiveLog> findByUserAndDiveNumber(PremiumUser user, Integer diveNumber);

    // Count the total number of dive logs for a specific user
    long countByUser(PremiumUser user);

    // Custom query to find the maximum dive number for a specific user
    @Query("SELECT SUM(d.duration) FROM DiveLog d WHERE d.user = :user")
    Integer sumDurationByUser(@Param("user") PremiumUser user);

    // Count distinct locations for a specific user
    @Query("SELECT COUNT(DISTINCT d.location) FROM DiveLog d WHERE d.user = :user")
    Long countDistinctLocationsByUser(@Param("user") PremiumUser user);

    // Get distinct locations for dropdown
    @Query("SELECT DISTINCT d.location FROM DiveLog d WHERE d.user = :user ORDER BY d.location")
    List<String> findDistinctLocationsByUser(@Param("user") PremiumUser user);
}
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

    List<DiveLog> findByUser(PremiumUser user);

    List<DiveLog> findByUserOrderByDiveDateDesc(PremiumUser user);

    List<DiveLog> findByUserAndLocationContainingIgnoreCase(PremiumUser user, String location);

    Optional<DiveLog> findByUserAndDiveNumber(PremiumUser user, Integer diveNumber);

    long countByUser(PremiumUser user);

    @Query("SELECT SUM(d.duration) FROM DiveLog d WHERE d.user = :user")
    Integer sumDurationByUser(@Param("user") PremiumUser user);

    @Query("SELECT COUNT(DISTINCT d.location) FROM DiveLog d WHERE d.user = :user")
    Long countDistinctLocationsByUser(@Param("user") PremiumUser user);

}

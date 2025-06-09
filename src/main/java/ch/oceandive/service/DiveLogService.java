package ch.oceandive.service;

import ch.fhnw.oceandive.model.DiveLog;
import ch.fhnw.oceandive.model.PremiumUser;
import ch.fhnw.oceandive.repository.DiveLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for DiveLog operations
 */
public interface DiveLogService {

  List<DiveLog> findAll();

  List<DiveLog> findByUser(PremiumUser user);

  List<DiveLog> findByUserOrderByDiveDateDesc(PremiumUser user);

  List<DiveLog> findByUserAndLocation(PremiumUser user, String location);

  Optional<DiveLog> findById(Long id);

  Optional<DiveLog> findByUserAndDiveNumber(PremiumUser user, Integer diveNumber);

  DiveLog save(DiveLog diveLog);

  void delete(DiveLog diveLog);

  void deleteById(Long id);

  long countByUser(PremiumUser user);

  Double getTotalHoursByUser(PremiumUser user);

  Long getUniqueLocationsByUser(PremiumUser user);
}


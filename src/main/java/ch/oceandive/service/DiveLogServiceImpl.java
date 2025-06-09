package ch.oceandive.service;

import ch.fhnw.oceandive.model.DiveLog;
import ch.fhnw.oceandive.model.PremiumUser;
import ch.fhnw.oceandive.repository.DiveLogRepo;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Service implementation for DiveLog operations
 */
@Service
public class DiveLogServiceImpl implements DiveLogService {

  private final DiveLogRepo diveLogRepo;

  public DiveLogServiceImpl(DiveLogRepo diveLogRepo) {
    this.diveLogRepo = diveLogRepo;
  }

  @Override
  public List<DiveLog> findAll() {
    return diveLogRepo.findAll();
  }

  @Override
  public List<DiveLog> findByUser(PremiumUser user) {
    return diveLogRepo.findByUser(user);
  }

  @Override
  public List<DiveLog> findByUserOrderByDiveDateDesc(PremiumUser user) {
    return diveLogRepo.findByUserOrderByDiveDateDesc(user);
  }

  @Override
  public List<DiveLog> findByUserAndLocation(PremiumUser user, String location) {
    return diveLogRepo.findByUserAndLocationContainingIgnoreCase(user, location);
  }

  @Override
  public Optional<DiveLog> findById(Long id) {
    return diveLogRepo.findById(id);
  }

  @Override
  public Optional<DiveLog> findByUserAndDiveNumber(PremiumUser user, Integer diveNumber) {
    return diveLogRepo.findByUserAndDiveNumber(user, diveNumber);
  }

  @Override
  public DiveLog save(DiveLog diveLog) {
    return diveLogRepo.save(diveLog);
  }

  @Override
  public void delete(DiveLog diveLog) {
    diveLogRepo.delete(diveLog);
  }

  @Override
  public void deleteById(Long id) {
    diveLogRepo.deleteById(id);
  }

  @Override
  public long countByUser(PremiumUser user) {
    return diveLogRepo.countByUser(user);
  }

  @Override
  public Double getTotalHoursByUser(PremiumUser user) {
    Integer totalMinutes = diveLogRepo.sumDurationByUser(user);
    return totalMinutes != null ? totalMinutes / 60.0 : 0.0;
  }

  @Override
  public Long getUniqueLocationsByUser(PremiumUser user) {
    return diveLogRepo.countDistinctLocationsByUser(user);
  }
}

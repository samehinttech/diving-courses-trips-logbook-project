package ch.oceandive.service;

import ch.oceandive.dto.DiveLogDTO;
import ch.oceandive.model.PremiumUser;

import java.util.List;
import java.util.Map;

/**
 * Service interface for DiveLog operations
 */
public interface DiveLogService {

  // Get all dive logs for a user
  List<DiveLogDTO> findAllByUser(PremiumUser user);

  //Get all dive logs for a user ordered by dive date descending
  List<DiveLogDTO> findByUserOrderByDiveDateDesc(PremiumUser user);

  // Get dive logs for a user filtered by location
  List<DiveLogDTO> findByUserAndLocation(PremiumUser user, String location);

  // Get a dive log by dive number for a specific user
  DiveLogDTO findByDiveNumberAndUser(Integer diveNumber, PremiumUser user);

  // Create a new dive log
  DiveLogDTO create(DiveLogDTO diveLogDTO, PremiumUser user);

  //Update an existing dive log
  DiveLogDTO update(Long id, DiveLogDTO diveLogDTO, PremiumUser user);

  //Update an existing dive log by dive number
  DiveLogDTO updateByDiveNumber(Integer diveNumber, DiveLogDTO diveLogDTO, PremiumUser user);

  // Delete a dive log
  void delete(Long id, PremiumUser user);

  // Delete a dive log by dive number
  void deleteByDiveNumber(Integer diveNumber, PremiumUser user);

  //Get dive log statistics for a user
  Map<String, Object> getUserStatistics(PremiumUser user);

  // Get all unique locations for a user
  List<String> getUserLocations(PremiumUser user);

  // Validate a dive log DTO
  String validate(DiveLogDTO diveLogDTO, PremiumUser user, boolean isUpdate);
}
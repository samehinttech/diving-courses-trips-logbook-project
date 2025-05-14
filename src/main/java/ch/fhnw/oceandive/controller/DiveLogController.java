package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.exceptionHandler.BusinessRuleViolationException;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.DiveLog;
import ch.fhnw.oceandive.model.PremiumUser;
import ch.fhnw.oceandive.service.DiveLogService;
import ch.fhnw.oceandive.service.PremiumUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing dive logs.
 * Only authenticated premium users can access these endpoints.
 */
@RestController
@RequestMapping("/api/dive-logs")
public class DiveLogController {

    private final DiveLogService diveLogService;
    private final PremiumUserService premiumUserService;

    @Autowired
    public DiveLogController(DiveLogService diveLogService, PremiumUserService premiumUserService) {
        this.diveLogService = diveLogService;
        this.premiumUserService = premiumUserService;
    }

    /**
     * GET /api/dive-logs: Get all dive logs for the current user.
     * Requires authentication.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of dive logs in body
     */
    @GetMapping
    public ResponseEntity<List<DiveLog>> getAllDiveLogsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        List<DiveLog> diveLogs = diveLogService.getDiveLogsByPremiumUser(premiumUser);
        
        return ResponseEntity.ok(diveLogs);
    }

    /**
     * GET /api/dive-logs/{id}: Get the dive log with the specified ID.
     * Requires authentication. Users can only access their own dive logs.
     * @return the ResponseEntity with status 200 (OK) and the dive log in the body,
     *         or with status 404 (Not Found) if the dive log is not found
     * @throws BusinessRuleViolationException if the user is not authorized to access the dive log
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiveLog> getDiveLogById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        DiveLog diveLog = diveLogService.getDiveLogById(id);
        
        // Check if the dive log belongs to the current user
        if (!diveLog.getPremiumUser().getId().equals(premiumUser.getId())) {
            throw new BusinessRuleViolationException("You are not authorized to access this dive log");
        }
        
        return ResponseEntity.ok(diveLog);
    }

    /**
     * GET /api/dive-logs/location/{location}: Get dive logs by location for the current user.
     * Requires authentication.
     *
     * @param location the location to search for
     * @return the ResponseEntity with status 200 (OK) and the list of dive logs in the body
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<List<DiveLog>> getDiveLogsByLocation(@PathVariable String location) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        List<DiveLog> diveLogs = diveLogService.getDiveLogsByLocation(location);
        
        // Filter dive logs to only include those belonging to the current user
        diveLogs = diveLogs.stream()
                .filter(diveLog -> diveLog.getPremiumUser().getId().equals(premiumUser.getId()))
                .toList();
        
        return ResponseEntity.ok(diveLogs);
    }

    /**
     * GET /api/dive-logs/date-range: Get dive logs within a date range for the current user.
     * Requires authentication.
     *
     * @param startTime the start time
     * @param endTime the end time
     * @return the ResponseEntity with status 200 (OK) and the list of dive logs in body
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<DiveLog>> getDiveLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        List<DiveLog> diveLogs = diveLogService.getDiveLogsByStartTimeBetween(startTime, endTime);
        
        // Filter dive logs to only include those belonging to the current user
        diveLogs = diveLogs.stream()
                .filter(diveLog -> diveLog.getPremiumUser().getId().equals(premiumUser.getId()))
                .toList();
        
        return ResponseEntity.ok(diveLogs);
    }

    /**
     * POST /api/dive-logs: Create a new dive log.
     * Requires authentication.
     *
     * @param diveLog the dive log to create
     * @return the ResponseEntity with status 201 (Created) and the new dive log in body
     */
    @PostMapping
    public ResponseEntity<DiveLog> createDiveLog(@RequestBody DiveLog diveLog) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        DiveLog createdDiveLog = diveLogService.createDiveLog(diveLog, premiumUser.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDiveLog);
    }

    /**
     * PUT /api/dive-logs/{id}: Update an existing dive log.
     * Requires authentication. Users can only update their own dive logs.
     * @return the ResponseEntity with status 200 (OK) and the updated dive log in body
     * @throws ResourceNotFoundException if the dive log is not found
     * @throws BusinessRuleViolationException if the user is not authorized to update the dive log
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiveLog> updateDiveLog(@PathVariable Long id, @RequestBody DiveLog diveLog) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        DiveLog updatedDiveLog = diveLogService.updateDiveLog(id, diveLog, premiumUser.getId());
        
        return ResponseEntity.ok(updatedDiveLog);
    }

    /**
     * DELETE /api/dive-logs/{id}: Delete a dive log.
     * @return the ResponseEntity with status 204 (NO_CONTENT)
     * @throws ResourceNotFoundException if the dive log is not found
     * @throws BusinessRuleViolationException if the user is not authorized to delete the dive log
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiveLog(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        PremiumUser premiumUser = premiumUserService.getPremiumUserEntityByUsername(username);
        diveLogService.deleteDiveLog(id, premiumUser.getId());
        
        return ResponseEntity.noContent().build();
    }
}
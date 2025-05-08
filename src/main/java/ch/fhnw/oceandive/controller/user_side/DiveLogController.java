package ch.fhnw.oceandive.controller.user_side;

import ch.fhnw.oceandive.dto.DiveLogDTO;
import ch.fhnw.oceandive.service.DiveLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing dive logs for authenticated users.
 */
@RestController
@RequestMapping("/api/divelogs")
@PreAuthorize("isAuthenticated()")
public class DiveLogController {

    private final DiveLogService diveLogService;

    @Autowired
    public DiveLogController(DiveLogService diveLogService) {
        this.diveLogService = diveLogService;
    }

    /**
     * GET /api/divelogs : Get all dive logs for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<DiveLogDTO>> getAllDiveLogs(Authentication authentication) {
        String userId = authentication.getName();
        List<DiveLogDTO> diveLogs = diveLogService.getAllDiveLogsByUser(userId);
        return ResponseEntity.ok(diveLogs);
    }

    /**
     * GET /api/divelogs/{id} : Get the dive log with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDiveLogById(@PathVariable Long id, Authentication authentication) {
        try {
            DiveLogDTO diveLog = diveLogService.getDiveLogById(id);

            // Check if the dive log belongs to the authenticated user
            if (!diveLog.getUserId().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            return ResponseEntity.ok(diveLog);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/divelogs/daterange : Get dive logs within a date range.
     */
    @GetMapping("/daterange")
    public ResponseEntity<List<DiveLogDTO>> getDiveLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String userId = authentication.getName();
        List<DiveLogDTO> diveLogs = diveLogService.getDiveLogsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(diveLogs);
    }

    /**
     * POST /api/divelogs : Create a new dive log.
     */
    @PostMapping
    public ResponseEntity<?> createDiveLog(@Valid @RequestBody DiveLogDTO diveLogDTO, Authentication authentication) {
        try {
            String userId = authentication.getName();
            DiveLogDTO createdDiveLog = diveLogService.createDiveLog(diveLogDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDiveLog);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/divelogs/{id} : Update an existing dive log.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiveLog(@PathVariable Long id, @Valid @RequestBody DiveLogDTO diveLogDTO, 
                                          Authentication authentication) {
        try {
            String userId = authentication.getName();
            DiveLogDTO updatedDiveLog = diveLogService.updateDiveLog(id, diveLogDTO, userId);
            return ResponseEntity.ok(updatedDiveLog);
        } catch (Exception e) {
            if (e instanceof jakarta.persistence.EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/divelogs/{id} : Delete a dive log by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiveLog(@PathVariable Long id, Authentication authentication) {
        try {
            String userId = authentication.getName();
            diveLogService.deleteDiveLog(id, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e instanceof jakarta.persistence.EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/divelogs/number/{diveNumber} : Delete a dive log by dive number.
     */
    @DeleteMapping("/number/{diveNumber}")
    public ResponseEntity<?> deleteDiveLogByNumber(@PathVariable Integer diveNumber, Authentication authentication) {
        try {
            String userId = authentication.getName();
            diveLogService.deleteDiveLogByDiveNumber(diveNumber, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e instanceof jakarta.persistence.EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/divelogs/next-number : Get the next dive number for the authenticated user.
     */
    @GetMapping("/next-number")
    public ResponseEntity<Map<String, Integer>> getNextDiveNumber(Authentication authentication) {
        String userId = authentication.getName();
        Integer nextNumber = diveLogService.getNextDiveNumber(userId);
        return ResponseEntity.ok(Map.of("nextDiveNumber", nextNumber));
    }
}

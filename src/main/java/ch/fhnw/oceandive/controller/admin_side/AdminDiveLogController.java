package ch.fhnw.oceandive.controller.admin_side;

import ch.fhnw.oceandive.dto.DiveLogDTO;
import ch.fhnw.oceandive.service.DiveLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing dive logs by administrators.
 */
@RestController
@RequestMapping("/api/admin/divelogs")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminDiveLogController {

    private final DiveLogService diveLogService;

    @Autowired
    public AdminDiveLogController(DiveLogService diveLogService) {
        this.diveLogService = diveLogService;
    }

    /**
     * GET /api/admin/divelogs/{id} : Get the dive log with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDiveLogById(@PathVariable Long id) {
        try {
            DiveLogDTO diveLog = diveLogService.getDiveLogById(id);
            return ResponseEntity.ok(diveLog);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/admin/divelogs/user/{userId} : Get all dive logs for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DiveLogDTO>> getAllDiveLogsByUser(@PathVariable String userId) {
        List<DiveLogDTO> diveLogs = diveLogService.getAllDiveLogsByUser(userId);
        return ResponseEntity.ok(diveLogs);
    }

    /**
     * GET /api/admin/divelogs/user/{userId}/daterange : Get dive logs for a user within a date range.
     */
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<DiveLogDTO>> getDiveLogsByDateRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DiveLogDTO> diveLogs = diveLogService.getDiveLogsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(diveLogs);
    }

    /**
     * POST /api/admin/divelogs/user/{userId} : Create a new dive log for a user.
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createDiveLog(@PathVariable String userId, @Valid @RequestBody DiveLogDTO diveLogDTO) {
        try {
            DiveLogDTO createdDiveLog = diveLogService.createDiveLog(diveLogDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDiveLog);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/admin/divelogs/{id}/user/{userId} : Update an existing dive logs
     */
    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<?> updateDiveLog(@PathVariable Long id, @PathVariable String userId, 
                                          @Valid @RequestBody DiveLogDTO diveLogDTO) {
        try {
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
     * DELETE /api/admin/divelogs/{id}/user/{userId} : Delete a dive log.
     */
    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<?> deleteDiveLog(@PathVariable Long id, @PathVariable String userId) {
        try {
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
     * DELETE /api/admin/divelogs/user/{userId}/number/{diveNumber} : Delete a dive log by dive number.
     */
    @DeleteMapping("/user/{userId}/number/{diveNumber}")
    public ResponseEntity<?> deleteDiveLogByNumber(@PathVariable String userId, @PathVariable Integer diveNumber) {
        try {
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
     * DELETE /api/admin/divelogs/user/{userId}/all : Delete all dive logs for a user.
     */
    @DeleteMapping("/user/{userId}/all")
    public ResponseEntity<?> deleteAllDiveLogsByUser(@PathVariable String userId) {
        try {
            diveLogService.deleteAllDiveLogsByUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e instanceof jakarta.persistence.EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

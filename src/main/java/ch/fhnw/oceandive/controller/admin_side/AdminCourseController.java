package ch.fhnw.oceandive.controller.admin_side;

import ch.fhnw.oceandive.dto.admin_side.AdminCourseDTO;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.service.CourseService;
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
 * REST controller for managing Course resource.
 * These endpoints are only accessible to users with the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminCourseController {

    private final CourseService courseService;

    @Autowired
    public AdminCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * GET /api/admin/courses : Get all active courses.
     *
     * @return  list of active courses in body
     */
    @GetMapping
    public ResponseEntity<List<AdminCourseDTO>> getAllActiveCourses() {
        List<AdminCourseDTO> courses = courseService.getAllActiveCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/admin/courses/{id} : Get the course with the specified ID.
     * @return status 200 (OK) and the course in body,
     *         or with status 404 (Not Found) if the course is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminCourseDTO> getCourseById(@PathVariable Long id) {
        AdminCourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    /**
     * GET /api/admin/courses/search: Search for courses by title
     */
    @GetMapping("/search")
    public ResponseEntity<List<AdminCourseDTO>> searchCoursesByTitle(@RequestParam String title) {
        List<AdminCourseDTO> courses = courseService.searchCoursesByTitle(title);
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/admin/courses/date-range: Get courses within a date range.
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<AdminCourseDTO>> getCoursesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AdminCourseDTO> courses = courseService.getCoursesByDateRange(startDate, endDate);
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/admin/courses/certification : Get courses by certification.
     */
    @GetMapping("/certification")
    public ResponseEntity<List<AdminCourseDTO>> getCoursesByCertification(
            @RequestParam DiveCertification certification) {
        List<AdminCourseDTO> courses = courseService.getCoursesByCertification(certification);
        return ResponseEntity.ok(courses);
    }

    /**
     * POST /api/admin/courses : Create a new course.
     * @return status 201 (Created) and the new course in body
     */
    @PostMapping
    public ResponseEntity<AdminCourseDTO> createCourse(@Valid @RequestBody AdminCourseDTO adminCourseDTO) {
        AdminCourseDTO createdCourse = courseService.createCourse(adminCourseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    /**
     * PUT /api/admin/courses/{id} : Update an existing course.
     * @return  status 200 (OK) and the updated course in body,
     *         or with status 404 (Not Found) if the course is not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminCourseDTO> updateCourse(
            @PathVariable Long id, @Valid @RequestBody AdminCourseDTO adminCourseDTO) {
        AdminCourseDTO updatedCourse = courseService.updateCourse(id, adminCourseDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    /**
     * DELETE /api/admin/courses/{id} : Delete a course (soft delete).
     * @return the ResponseEntity with status 204 (No Content),
     *         or with status 404 (Not Found) if the course is not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/admin/courses/{id}/permanent : Permanently delete a course.
     * @return  status 204 (No Content),or with status 404 (Not Found) if the course is not found
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteCourse(@PathVariable Long id) {
        courseService.permanentlyDeleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}

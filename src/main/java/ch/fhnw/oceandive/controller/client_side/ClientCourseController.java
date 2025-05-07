package ch.fhnw.oceandive.controller.client_side;

import ch.fhnw.oceandive.dto.client_side.ClientCourseDTO;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for public Course views that are accessible to all users,
 * including unauthenticated ones (GUEST).
 */
@RestController
@RequestMapping("/api/courses")
public class ClientCourseController {

    private final CourseService courseService;

    @Autowired
    public ClientCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * GET /api/courses : Get all active courses for public access.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of active courses in body
     */
    @GetMapping
    public ResponseEntity<List<ClientCourseDTO>> getAllActiveCourses() {
        List<ClientCourseDTO> courses = courseService.getAllActiveCoursesForPublic();
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/{id} : Get the course with the specified ID for public access.
     * @return the ResponseEntity with status 200 (OK) and the course in the body,
     *         or with status 404 (Not Found) if the course is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientCourseDTO> getCourseById(@PathVariable Long id) {
        ClientCourseDTO course = courseService.getCourseByIdForPublic(id);
        return ResponseEntity.ok(course);
    }

    /**
     * GET /api/courses/search: Search for courses by title for public access.
     * @return the ResponseEntity with status 200 (OK) and the list of matching courses in the body
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClientCourseDTO>> searchCoursesByTitle(@RequestParam String title) {
        List<ClientCourseDTO> courses = courseService.searchCoursesByTitleForPublic(title);
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/date-range: Get courses within a date range for public access.
     * @return the ResponseEntity with status 200 (OK) and the list of courses in the body
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<ClientCourseDTO>> getCoursesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ClientCourseDTO> courses = courseService.getCoursesByDateRangeForPublic(startDate, endDate);
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/certification: Get courses by certification for public access.
     * @return the ResponseEntity with status 200 (OK) and the list of courses body
     */
    @GetMapping("/certification")
    public ResponseEntity<List<ClientCourseDTO>> getCoursesByCertification(
            @RequestParam DiveCertification certification) {
        List<ClientCourseDTO> courses = courseService.getCoursesByCertificationForPublic(certification);
        return ResponseEntity.ok(courses);
    }
}
package ch.fhnw.oceandive.controller.client_side;

import ch.fhnw.oceandive.dto.client_side.PublicCourseDTO;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for public Course resources.
 * These endpoints are accessible to all users, including unauthenticated ones.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * GET /api/courses : Get all active courses for public access.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of active courses in body
     */
    @GetMapping
    public ResponseEntity<List<PublicCourseDTO>> getAllActiveCourses() {
        List<PublicCourseDTO> courses = courseService.getAllActiveCoursesForPublic();
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/{id} : Get the course with the specified ID for public access.
     *
     * @param id the ID of the course to retrieve
     * @return the ResponseEntity with status 200 (OK) and the course in body,
     *         or with status 404 (Not Found) if the course is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PublicCourseDTO> getCourseById(@PathVariable Long id) {
        PublicCourseDTO course = courseService.getCourseByIdForPublic(id);
        return ResponseEntity.ok(course);
    }

    /**
     * GET /api/courses/search: Search for courses by title for public access.
     *
     * @param title the title to search for
     * @return the ResponseEntity with status 200 (OK) and the list of matching courses in body
     */
    @GetMapping("/search")
    public ResponseEntity<List<PublicCourseDTO>> searchCoursesByTitle(@RequestParam String title) {
        List<PublicCourseDTO> courses = courseService.searchCoursesByTitleForPublic(title);
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/date-range: Get courses within a date range for public access.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return the ResponseEntity with status 200 (OK) and the list of courses in the body
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<PublicCourseDTO>> getCoursesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PublicCourseDTO> courses = courseService.getCoursesByDateRangeForPublic(startDate, endDate);
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/certification: Get courses by certification for public access.
     *
     * @param certification the certification to search for
     * @return the ResponseEntity with status 200 (OK) and the list of courses in body
     */
    @GetMapping("/certification")
    public ResponseEntity<List<PublicCourseDTO>> getCoursesByCertification(
            @RequestParam DiveCertification certification) {
        List<PublicCourseDTO> courses = courseService.getCoursesByCertificationForPublic(certification);
        return ResponseEntity.ok(courses);
    }
}
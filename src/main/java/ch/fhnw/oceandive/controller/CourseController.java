package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.service.CourseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing courses.
 */
@RestController
@RequestMapping("/api")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * GET /api/courses: Get all courses.
     * Public endpoint is accessible to all users.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of courses in body
     */
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/{id}: Get the course with the specified ID.
     * Public endpoint is accessible to all users.
     *
     * @param id the ID of the course to retrieve
     * @return the ResponseEntity with status 200 (OK) and the course in body,
     *         or with status 404 (Not Found) if the course is not found
     */
    @GetMapping("/courses/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    /**
     * GET /api/courses/upcoming: Get all upcoming courses (starting from today).
     * Public endpoint is accessible to all users.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of upcoming courses in body,
     *         or with status 200 (OK) and "coming soon" message if no upcoming courses are found
     */
    @GetMapping("/courses/upcoming")
    public ResponseEntity<?> getUpcomingCourses() {
        List<Course> courses = courseService.getCoursesByStartDateAfter(LocalDate.now());
        if (courses.isEmpty()) {
            return ResponseEntity.ok("coming soon");
        }
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/available: Get all available courses (not fully booked).
     * Public endpoint accessible to all users.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of available courses in body
     */
    @GetMapping("/courses/available")
    public ResponseEntity<List<Course>> getAvailableCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/name/{name} : Get courses by name.
     * Public endpoint accessible to all users.
     *
     * @param name the name to search for
     * @return the ResponseEntity with status 200 (OK) and the list of courses in body
     */
    @GetMapping("/courses/name/{name}")
    public ResponseEntity<List<Course>> getCoursesByName(@PathVariable String name) {
        List<Course> courses = courseService.getCoursesByName(name);
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/date-range: Get courses within a date range.
     * Public endpoint is accessible to all users.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return the ResponseEntity with status 200 (OK) and the list of courses in body
     */
    @GetMapping("/courses/date-range")
    public ResponseEntity<List<Course>> getCoursesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Course> courses = courseService.getCoursesByStartDateBetween(startDate, endDate);
        return ResponseEntity.ok(courses);
    }
}

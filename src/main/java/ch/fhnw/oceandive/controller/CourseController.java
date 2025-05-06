package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.dto.CourseDTO;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing Course resources.
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
     * GET /api/courses : Get all active courses.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of active courses in body
     */
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllActiveCourses() {
        List<CourseDTO> courses = courseService.getAllActiveCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/{id} : Get the course with the specified ID.
     *
     * @param id the ID of the course to retrieve
     * @return the ResponseEntity with status 200 (OK) and the course in body,
     *         or with status 404 (Not Found) if the course is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    /**
     * GET /api/courses/search : Search for courses by title.
     *
     * @param title the title to search for
     * @return the ResponseEntity with status 200 (OK) and the list of matching courses in body
     */
    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCoursesByTitle(@RequestParam String title) {
        List<CourseDTO> courses = courseService.searchCoursesByTitle(title);
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/date-range : Get courses within a date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return the ResponseEntity with status 200 (OK) and the list of courses in body
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<CourseDTO>> getCoursesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CourseDTO> courses = courseService.getCoursesByDateRange(startDate, endDate);
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/certification : Get courses by certification.
     *
     * @param certification the certification to search for
     * @return the ResponseEntity with status 200 (OK) and the list of courses in body
     */
    @GetMapping("/certification")
    public ResponseEntity<List<CourseDTO>> getCoursesByCertification(
            @RequestParam DiveCertification certification) {
        List<CourseDTO> courses = courseService.getCoursesByCertification(certification);
        return ResponseEntity.ok(courses);
    }

    /**
     * POST /api/courses : Create a new course.
     *
     * @param courseDTO the course to create
     * @return the ResponseEntity with status 201 (Created) and the new course in body
     */
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    /**
     * PUT /api/courses/{id} : Update an existing course.
     * @return the ResponseEntity with status 200 (OK) and the updated course in body,
     *         or with status 404 (Not Found) if the course is not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
        CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    /**
     * DELETE /api/courses/{id} : Delete a course (soft delete).
     * @return the ResponseEntity with status 204 (No Content),
     or with status 404 (Not Found) if the course is not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/courses/{id}/permanent : Permanently delete a course.
     * @return the ResponseEntity with status 204 (No Content),
     *         or with status 404 (Not Found) if the course is not found
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteCourse(@PathVariable Long id) {
        courseService.permanentlyDeleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
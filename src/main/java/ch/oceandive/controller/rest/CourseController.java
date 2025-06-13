package ch.oceandive.controller.rest;

import ch.oceandive.model.Course;
import ch.oceandive.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
  * REST API controller for course management operations.
 */

@RestController
@RequestMapping("/api") // Base URL for all course-related endpoints
@Tag(name = "Course", description = "Course management operations")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // Endpoint to get all courses (public access)
    @GetMapping("/courses")
    @Operation(summary = "Get all courses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    })
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }
    // Endpoint to get a course by its ID (public access) the course ID is for the backend to identify the course
    @GetMapping("/courses/{id}")
    @Operation(summary = "Get course by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<Course> getCourseById(
        @Parameter(description = "Course ID") @PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    // Endpoint to get upcoming courses (public access)
    @GetMapping("/courses/upcoming")
    @Operation(summary = "Get upcoming courses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upcoming courses retrieved successfully")
    })
    public ResponseEntity<?> getUpcomingCourses() {
        List<Course> courses = courseService.getCoursesByStartDateAfter(LocalDate.now());
        if (courses.isEmpty()) {
            return ResponseEntity.ok("coming soon");
        }
        return ResponseEntity.ok(courses);
    }

    // Endpoint to get available courses (public access)
    @GetMapping("/courses/available")
    @Operation(summary = "Get available courses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available courses retrieved successfully")
    })
    public ResponseEntity<List<Course>> getAvailableCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    // Endpoint to get courses by name (public access)
    @GetMapping("/courses/name/{name}")
    @Operation(summary = "Get courses by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    })
    public ResponseEntity<List<Course>> getCoursesByName(
        @Parameter(description = "Course name") @PathVariable String name) {
        List<Course> courses = courseService.getCoursesByName(name);
        return ResponseEntity.ok(courses);
    }
    // Endpoint to get courses by date range (public access)
    @GetMapping("/courses/date-range")
    @Operation(summary = "Get courses by date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format")
    })
    public ResponseEntity<List<Course>> getCoursesByDateRange(
        @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Course> courses = courseService.getCoursesByStartDateBetween(startDate, endDate);
        return ResponseEntity.ok(courses);
    }
}
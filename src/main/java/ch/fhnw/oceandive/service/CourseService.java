package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.repository.CourseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class for managing Course entities.
 */
@Service
public class CourseService {

    private final CourseRepo courseRepo;

    @Autowired
    public CourseService(CourseRepo courseRepo) {
        this.courseRepo = courseRepo;
    }

    /**
     * Get all courses.
     *
     * @return List of all courses
     */
    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    /**
     * Get a course by ID.
     * 
     * @param id The ID of the course to retrieve
     * @return The found course entity
     * @throws ResourceNotFoundException if the course is not found
     */
    public Course getCourseById(Long id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    /**
     * Get courses with start date after the given date.
     * 
     * @param date The date to compare against
     * @return List of courses starting after the given date
     */
    public List<Course> getCoursesByStartDateAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return courseRepo.findByStartDateAfter(date);
    }

    /**
     * Get courses with start date before the given date.
     * 
     * @param date The date to compare against
     * @return List of courses starting before the given date
     */
    public List<Course> getCoursesByStartDateBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return courseRepo.findByStartDateBefore(date);
    }

    /**
     * Get courses with start date between the given dates.
     * 
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return List of courses starting between the given dates
     * @throws IllegalArgumentException if startDate or endDate is null, or if
     *                                  startDate is after endDate
     */
    public List<Course> getCoursesByStartDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        return courseRepo.findByStartDateBetween(startDate, endDate);
    }

    /**
     * Get courses by location.
     * 
     * @param location The location to search for (case insensitive, partial match)
     * @return List of courses at the given location
     */
    public List<Course> getCoursesByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        return courseRepo.findByLocationContainingIgnoreCase(location);
    }

    /**
     * Get courses that are not fully booked.
     *
     * @return List of courses that are not fully booked
     */
    public List<Course> getAvailableCourses() {
        return courseRepo.findByCurrentBookingsLessThanCapacity();
    }

    /**
     * Create a new course.
     * 
     * @param course The course entity to create
     * @return The created course with generated ID
     */
    @Transactional
    public Course createCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }
        return courseRepo.save(course);
    }

    /**
     * Update an existing course.
     * 
     * @param id            The ID of the course to update
     * @param courseDetails The updated course details
     * @return The updated course entity
     * @throws ResourceNotFoundException if the course is not found
     */
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        if (courseDetails == null) {
            throw new IllegalArgumentException("Course details cannot be null");
        }

        Course course = getCourseById(id);

        course.setLocation(courseDetails.getLocation());
        course.setDescription(courseDetails.getDescription());
        course.setStartDate(courseDetails.getStartDate());
        course.setEndDate(courseDetails.getEndDate());
        course.setImageUrl(courseDetails.getImageUrl());
        course.setCapacity(courseDetails.getCapacity());
        course.setMinCertificationRequired(courseDetails.getMinCertificationRequired());

        return courseRepo.save(course);
    }

    /**
     * Delete a course by ID.
     * 
     * @param id The ID of the course to delete
     * @throws ResourceNotFoundException if the course is not found
     */
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courseRepo.delete(course);
    }
}
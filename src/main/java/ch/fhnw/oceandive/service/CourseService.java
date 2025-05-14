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
     * @throws ResourceNotFoundException if the course is not found
     */
    public Course getCourseById(Long id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    /**
     * Get courses with start date after the given date.
     * @return List of courses starting after the given date
     */
    public List<Course> getCoursesByStartDateAfter(LocalDate date) {
        return courseRepo.findByStartDateAfter(date);
    }

    /**
     * Get courses with start date before the given date.
     * @return List of courses starting before the given date
     */
    public List<Course> getCoursesByStartDateBefore(LocalDate date) {
        return courseRepo.findByStartDateBefore(date);
    }

    /**
     * Get courses with start date between the given dates.
     * @return List of courses starting between the given dates
     */
    public List<Course> getCoursesByStartDateBetween(LocalDate startDate, LocalDate endDate) {
        return courseRepo.findByStartDateBetween(startDate, endDate);
    }

    /**
     * Get courses by location.
     * @return List of courses at the given location
     */
    public List<Course> getCoursesByLocation(String location) {
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
     * @return The created course
     */
    @Transactional
    public Course createCourse(Course course) {
        return courseRepo.save(course);
    }

    /**
     * Update an existing course.
     * @throws ResourceNotFoundException if the course is not found
     */
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
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
     * @throws ResourceNotFoundException if the course is not found
     */
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courseRepo.delete(course);
    }
}
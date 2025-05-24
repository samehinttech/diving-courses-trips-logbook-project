package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.repository.CourseRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CertificationValidatorService certificationValidator;

    public CourseService(CourseRepo courseRepo, CertificationValidatorService certificationValidator) {
        this.courseRepo = courseRepo;
        this.certificationValidator = certificationValidator;
    }

    /**
     * Get all courses.
     * @return List of all courses
     */
    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    /**
     * Get all courses with pagination.
     * @return A page of courses
     */
    public Page<Course> findAll(Pageable pageable) {
        return courseRepo.findAll(pageable);
    }

    /**
     * Get a course by ID.
     * @return The found course entity
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
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return courseRepo.findByStartDateAfter(date);
    }

    /**
     * Get courses with start date before the given date.
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
     * Get courses by name (case insensitive, partial match).
     * @param name the name to search for
     * @return List of courses matching the name
     */
    public List<Course> getCoursesByName(String name) {
        return courseRepo.findByNameContainingIgnoreCase(name);
    }

    /**
     * Check if a user with the given certification can enroll in this course
     * @return true if the user can enroll in the course, false otherwise
     */
    public boolean canEnrollInCourse(Long courseId, DiveCertification userCertification) {
        Course course = getCourseById(courseId);

        // For users with no certification
        if (userCertification == null) {
            userCertification = DiveCertification.NON_DIVER;
        }

        // Use the simple certification validation method
        return certificationValidator.validateCertification(
                userCertification,
                course.getMinCertificationRequired());
    }

    /**
     * Book a course if user has adequate certification
     * @return The updated course
     * @throws IllegalStateException if the course is fully booked or user lacks required certification
     */
    @Transactional
    public Course enrollInCourseWithCertification(Long courseId, DiveCertification userCertification) {
        if (!canEnrollInCourse(courseId, userCertification)) {
            throw new IllegalStateException("User does not have required certification for this course");
        }

        return enrollInCourse(courseId);
    }

    /**
     * Enroll in a course
     * @return The updated course
     * @throws IllegalStateException if the course is fully booked
     */
    @Transactional
    public Course enrollInCourse(Long courseId) {
        Course course = getCourseById(courseId);

        if (course.getCurrentBookings() >= course.getCapacity()) {
            throw new IllegalStateException("Course is fully booked");
        }

        course.setCurrentBookings(course.getCurrentBookings() + 1);
        return courseRepo.save(course);
    }

    /**
     * Create a new course.
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
     * @return The updated course entity
     * @throws ResourceNotFoundException if the course is not found
     */
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        if (courseDetails == null) {
            throw new IllegalArgumentException("Course details cannot be null");
        }
        Course course = getCourseById(id);
        if (courseDetails.getName() != null) {
            course.setName(courseDetails.getName());
        }
        if (courseDetails.getDescription() != null) {
            course.setDescription(courseDetails.getDescription());
        }
        if (courseDetails.getStartDate() != null) {
            course.setStartDate(courseDetails.getStartDate());
        }
        if (courseDetails.getEndDate() != null) {
            course.setEndDate(courseDetails.getEndDate());
        }
        if (courseDetails.getImageUrl() != null) {
            course.setImageUrl(courseDetails.getImageUrl());
        }
        if (courseDetails.getCapacity() != null) {
            if (courseDetails.getCapacity() < course.getCurrentBookings()) {
                throw new IllegalArgumentException("Cannot reduce capacity below current number of bookings");
            }
            course.setCapacity(courseDetails.getCapacity());
        }
        if (courseDetails.getMinCertificationRequired() != null) {
            course.setMinCertificationRequired(courseDetails.getMinCertificationRequired());
        }
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
    
    /**
     * Cancel enrollment in a course.
     * @return The updated course
     */
    @Transactional
    public Course cancelEnrollment(Long courseId) {
        Course course = getCourseById(courseId);
        
        if (course.getCurrentBookings() > 0) {
            course.setCurrentBookings(course.getCurrentBookings() - 1);
        }
        
        return courseRepo.save(course);
    }
}
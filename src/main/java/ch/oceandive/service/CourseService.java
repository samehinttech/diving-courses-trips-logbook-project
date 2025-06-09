package ch.oceandive.service;

import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.CourseStatus;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.repository.CourseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepo courseRepo;
    private final CertificationValidationService certificationValidator;

    public CourseService(CourseRepo courseRepo, CertificationValidationService certificationValidator) {
        this.courseRepo = courseRepo;
        this.certificationValidator = certificationValidator;
    }


     // Get all courses.

    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    //Get all courses with pagination.

    public Page<Course> findAll(Pageable pageable) {
        return courseRepo.findAll(pageable);
    }


    // Get a course by ID.

    public Course getCourseById(Long id) {
        return courseRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    // Get courses with start date after the given date.

    public List<Course> getCoursesByStartDateAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return courseRepo.findByStartDateAfter(date);
    }

    // Get courses with start date before the given date.

    public List<Course> getCoursesByStartDateBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return courseRepo.findByStartDateBefore(date);
    }


     // Get courses with start date between the given dates.

    public List<Course> getCoursesByStartDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        return courseRepo.findByStartDateBetween(startDate, endDate);
    }


    //  Get courses by name (case insensitive, partial match).

    public List<Course> getCoursesByName(String name) {
        return courseRepo.findByNameContainingIgnoreCase(name);
    }


    // Check if a user with the given certification can enroll in this course.

    public boolean canEnrollInCourse(Long courseId, DiveCertification userCertification) {
        Course course = getCourseById(courseId);

        if (userCertification == null) {
            userCertification = DiveCertification.NON_DIVER;
        }

        return certificationValidator.validateCertification(
            userCertification,
            course.getMinCertificationRequired());
    }


     // Book a course if user has adequate certification.

    @Transactional
    public Course enrollInCourseWithCertification(Long courseId, DiveCertification userCertification) {
        if (!canEnrollInCourse(courseId, userCertification)) {
            throw new IllegalStateException("User does not have required certification for this course");
        }
        return enrollInCourse(courseId);
    }
     // Enroll in a course.
    @Transactional
    public Course enrollInCourse(Long courseId) {
        Course course = getCourseById(courseId);

        if (course.getCurrentBookings() >= course.getCapacity()) {
            throw new IllegalStateException("Course is fully booked");
        }

        course.setCurrentBookings(course.getCurrentBookings() + 1);
        return courseRepo.save(course);
    }


    // Create a new course.

    @Transactional
    public Course createCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }
        return courseRepo.save(course);
    }


     // Update an existing course.

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


    // Delete a course by ID.
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courseRepo.delete(course);
    }


     // Cancel enrollment in a course.
    @Transactional
    public Course cancelEnrollment(Long courseId) {
        Course course = getCourseById(courseId);

        if (course.getCurrentBookings() > 0) {
            course.setCurrentBookings(course.getCurrentBookings() - 1);
        }

        return courseRepo.save(course);
    }

     // Get featured courses for homepage display.
    public List<Course> getFeaturedCourses(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return courseRepo.findByStatusAndFeaturedOrderByDisplayOrderAsc(
            CourseStatus.PUBLISHED, true, pageable).getContent();
    }

     //Get all published courses for public course listing.
    public Page<Course> getPublishedCourses(Pageable pageable) {
        return courseRepo.findByStatusOrderByCreatedAtDesc(CourseStatus.PUBLISHED, pageable);
    }


    // Get course by slug for SEO-friendly URLs.
    public Optional<Course> getCourseBySlug(String slug) {
        return courseRepo.findBySlugAndStatus(slug, CourseStatus.PUBLISHED);
    }
   //  Get available courses (published, not full, future dates).

    public List<Course> getAvailableCourses() {
        return courseRepo.findAvailableCourses(CourseStatus.PUBLISHED, LocalDate.now());
    }


    // Get available courses with pagination.
    public Page<Course> getAvailableCourses(Pageable pageable) {
        return courseRepo.findAvailableCourses(CourseStatus.PUBLISHED, LocalDate.now(), pageable);
    }
     // Get upcoming courses (regardless of booking status).
    public List<Course> getUpcomingCourses() {
        return courseRepo.findUpcomingCourses(CourseStatus.PUBLISHED, LocalDate.now());
    }
     // Search published courses.
    public Page<Course> searchPublishedCourses(String searchTerm, Pageable pageable) {
        return courseRepo.searchCourses(searchTerm, CourseStatus.PUBLISHED, pageable);
    }


     // Get all courses for admin (regardless of status).

    public Page<Course> getAllCoursesForAdmin(Pageable pageable) {
        return courseRepo.findAllForAdmin(null, pageable);
    }


     // Search all courses for admin.

    public Page<Course> searchCoursesForAdmin(String searchTerm, Pageable pageable) {
        return courseRepo.findAllForAdmin(searchTerm, pageable);
    }


    // Create course with enhanced validation.

    @Transactional
    public Course createCourseEnhanced(Course course) {
        validateCourseData(course);

        // Generate slug if not provided
        if (course.getSlug() == null || course.getSlug().isEmpty()) {
            course.setSlug(course.generateSlug(course.getName()));
        }

        // Ensure slug is unique
        course.setSlug(ensureUniqueSlug(course.getSlug(), null));

        // Set default short description if not provided
        if (course.getShortDescription() == null || course.getShortDescription().isEmpty()) {
            String desc = course.getDescription();
            course.setShortDescription(desc != null && desc.length() > 150
                ? desc.substring(0, 150) + "..."
                : desc);
        }

        logger.info("Creating new course: {} with slug: {}", course.getName(), course.getSlug());
        return courseRepo.save(course);
    }

    // Update course with enhanced validation.

    @Transactional
    public Course updateCourseEnhanced(Long id, Course courseDetails) {
        Course existingCourse = getCourseById(id);

        // Update fields that are provided
        if (courseDetails.getName() != null) {
            existingCourse.setName(courseDetails.getName());
            // Update slug if name changed
            if (courseDetails.getSlug() == null) {
                existingCourse.setSlug(ensureUniqueSlug(
                    existingCourse.generateSlug(courseDetails.getName()), id));
            }
        }

        if (courseDetails.getDescription() != null) {
            existingCourse.setDescription(courseDetails.getDescription());
        }

        if (courseDetails.getShortDescription() != null) {
            existingCourse.setShortDescription(courseDetails.getShortDescription());
        }

        if (courseDetails.getPrice() != null) {
            existingCourse.setPrice(courseDetails.getPrice());
        }

        if (courseDetails.getStartDate() != null) {
            existingCourse.setStartDate(courseDetails.getStartDate());
        }

        if (courseDetails.getEndDate() != null) {
            existingCourse.setEndDate(courseDetails.getEndDate());
        }

        if (courseDetails.getCapacity() != null) {
            validateCapacityChange(existingCourse, courseDetails.getCapacity());
            existingCourse.setCapacity(courseDetails.getCapacity());
        }

        if (courseDetails.getImageUrl() != null) {
            existingCourse.setImageUrl(courseDetails.getImageUrl());
        }

        if (courseDetails.getMinCertificationRequired() != null) {
            existingCourse.setMinCertificationRequired(courseDetails.getMinCertificationRequired());
        }

        if (courseDetails.getStatus() != null) {
            existingCourse.setStatus(courseDetails.getStatus());
        }

        if (courseDetails.getFeatured() != null) {
            existingCourse.setFeatured(courseDetails.getFeatured());
        }

        if (courseDetails.getDisplayOrder() != null) {
            existingCourse.setDisplayOrder(courseDetails.getDisplayOrder());
        }

        logger.info("Updating course: {} (ID: {})", existingCourse.getName(), id);
        return courseRepo.save(existingCourse);
    }


    // Toggle featured status of a course.
    @Transactional
    public Course toggleFeaturedStatus(Long courseId) {
        Course course = getCourseById(courseId);
        course.setFeatured(!course.getFeatured());
        logger.info("Toggled featured status for course: {} (ID: {}) to: {}",
            course.getName(), courseId, course.getFeatured());
        return courseRepo.save(course);
    }


   //  Change course status.

    @Transactional
    public Course changeStatus(Long courseId, CourseStatus status) {
        Course course = getCourseById(courseId);
        CourseStatus oldStatus = course.getStatus();
        course.setStatus(status);
        logger.info("Changed course status: {} (ID: {}) from {} to {}",
            course.getName(), courseId, oldStatus, status);
        return courseRepo.save(course);
    }
    // Helper methods for validation and slug management.
    private void validateCourseData(Course course) {
        if (course.getName() == null || course.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required");
        }

        if (course.getDescription() == null || course.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Course description is required");
        }

        if (course.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }

        if (course.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required");
        }

        if (course.getStartDate().isAfter(course.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (course.getCapacity() == null || course.getCapacity() < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1");
        }

        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
    }

    private void validateCapacityChange(Course existingCourse, Integer newCapacity) {
        if (newCapacity < existingCourse.getCurrentBookings()) {
            throw new IllegalArgumentException(
                String.format("Cannot reduce capacity to %d below current bookings (%d)",
                    newCapacity, existingCourse.getCurrentBookings()));
        }
    }

    private String ensureUniqueSlug(String baseSlug, Long excludeId) {
        if (baseSlug == null) return null;

        String slug = baseSlug;
        int counter = 1;

        boolean exists = excludeId != null
            ? courseRepo.existsBySlugAndIdNot(slug, excludeId)
            : courseRepo.existsBySlug(slug);

        while (exists) {
            slug = baseSlug + "-" + counter;
            exists = excludeId != null
                ? courseRepo.existsBySlugAndIdNot(slug, excludeId)
                : courseRepo.existsBySlug(slug);
            counter++;
        }

        return slug;
    }

    // Methods for  admin dashboard.

    public long getPublishedCourseCount() {
        return courseRepo.countByStatus(CourseStatus.PUBLISHED);
    }

    public long getAvailableSpotsCount() {
        Long spots = courseRepo.countAvailableSpots(CourseStatus.PUBLISHED, LocalDate.now());
        return spots != null ? spots : 0L;
    }
}
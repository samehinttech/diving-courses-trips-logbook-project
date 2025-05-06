package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.dto.CourseDTO;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling Course-related business logic.
 */
@Service
public class CourseService {

    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Retrieves all active and non-deleted courses.
     *
     * @return a list of CourseDTO objects representing active and non-deleted courses
     */
    public List<CourseDTO> getAllActiveCourses() {
        return courseRepository.findAllByIsActiveTrueAndIsDeletedFalse().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a course by its ID.
     * @throws ResourceNotFoundException if the course is not found
     */
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return convertToDTO(course);
    }

    /**
     * Searches for courses by title.
     * @return a list of CourseDTO objects matching the search criteria
     */
    public List<CourseDTO> searchCoursesByTitle(String title) {
        return courseRepository.findByCourseTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves courses within a date range.
     * @return a list of CourseDTO objects within the date range
     */
    public List<CourseDTO> getCoursesByDateRange(LocalDate startDate, LocalDate endDate) {
        return courseRepository.findByStartDateBetween(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves courses by awarded certification.
     * @return a list of CourseDTO objects with the specified certification
     */
    public List<CourseDTO> getCoursesByCertification(DiveCertification certification) {
        return courseRepository.findCourseByAwardedCertification(certification).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new course.
     * @return the created CourseDTO object
     */
    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = convertToEntity(courseDTO);
        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }

    /**
     * Updates an existing course.
     * @throws ResourceNotFoundException if the course is not found
     */
    @Transactional
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        updateCourseFromDTO(existingCourse, courseDTO);
        Course updatedCourse = courseRepository.save(existingCourse);
        return convertToDTO(updatedCourse);
    }

    /**
     * Deletes a course by setting its isDeleted flag to true.
     * @throws ResourceNotFoundException if the course is not found
     */
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        course.setDeleted(true);
        courseRepository.save(course);
    }

    /**
     * Permanently deletes a course from the database.
     * @throws ResourceNotFoundException if the course is not found
     */
    @Transactional
    public void permanentlyDeleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        courseRepository.delete(course);
    }

    /**
     * Converts a Course entity to a CourseDTO.
     * @return the converted CourseDTO
     */
    private CourseDTO convertToDTO(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getCourseTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getStartDate(),
                course.getEndDate(),
                course.getDuration(),
                course.getMaxParticipants(),
                course.getSpotsAvailable(),
                course.getRequiredCertification(),
                course.getProvidedCertification(),
                course.getAwardedCertification(),
                course.getIncludedItems(),
                course.isActive(),
                course.isDeleted()
        );
    }

    /**
     * Converts a CourseDTO to a Course entity.
     * This ensure the separation of concerns and not revaluing sensitive data.
     * @return the converted Course entity
     */
    private Course convertToEntity(CourseDTO courseDTO) {
        Course course = new Course();
        updateCourseFromDTO(course, courseDTO);
        return course;
    }

    /**
     * Updates a Course entity with information from a CourseDTO
     * @param courseDTO the CourseDTO with updated information
     */
    private void updateCourseFromDTO(Course course, CourseDTO courseDTO) {
        course.setCourseTitle(courseDTO.getCourseTitle());
        course.setDescription(courseDTO.getDescription());
        course.setPrice(courseDTO.getPrice());
        course.setStartDate(courseDTO.getStartDate());
        course.setEndDate(courseDTO.getEndDate());
        course.setDuration(courseDTO.getDuration());
        course.setMaxParticipants(courseDTO.getMaxParticipants());
        course.setSpotsAvailable(courseDTO.getSpotsAvailable());
        course.setRequiredCertification(courseDTO.getRequiredCertification());
        course.setProvidedCertification(courseDTO.getProvidedCertification());
        course.setAwardedCertification(courseDTO.getAwardedCertification());
        course.setIncludedItems(courseDTO.getIncludedItems());
        course.setActive(courseDTO.getIsActive());
        course.setDeleted(courseDTO.getIsDeleted());
    }
}
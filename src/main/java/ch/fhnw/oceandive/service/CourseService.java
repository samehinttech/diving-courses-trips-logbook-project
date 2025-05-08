package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.dto.admin_side.AdminCourseDTO;
import ch.fhnw.oceandive.dto.client_side.ClientCourseDTO;
import ch.fhnw.oceandive.exceptionHandler.ResourceNotFoundException;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public List<AdminCourseDTO> getAllActiveCourses() {
    return courseRepository.findAllByIsActiveTrueAndIsDeletedFalse().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }
  
  /**
   * Retrieves all active and non-deleted courses for public/client use.
   */
  public List<ClientCourseDTO> getAllActiveCoursesForPublic() {
    return courseRepository.findAllByIsActiveTrueAndIsDeletedFalse().stream()
        .map(this::convertToPublicDTO)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves a course by its ID.
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public AdminCourseDTO getCourseById(Long id) {
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    return convertToDTO(course);
  }
  
  /**
   * Retrieves a course by its ID for public/client use.
   */
  public ClientCourseDTO getCourseByIdForPublic(Long id) {
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    
    // Only return active, non-deleted courses to the public
    if (!course.isActive() || course.isDeleted()) {
        throw new ResourceNotFoundException("Course not found with id: " + id);
    }
    
    return convertToPublicDTO(course);
  }

  /**
   * Searches for courses by title.
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public List<AdminCourseDTO> searchCoursesByTitle(String title) {
    return courseRepository.findByCourseTitleContainingIgnoreCase(title).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }
  
  /**
   * Searches for active courses by title for public/client use.
   */
  public List<ClientCourseDTO> searchCoursesByTitleForPublic(String title) {
    return courseRepository.findByCourseTitleContainingIgnoreCase(title).stream()
        .filter(course -> course.isActive() && !course.isDeleted()) // Only show active, non-deleted courses
        .map(this::convertToPublicDTO)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves courses within a date range.
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public List<AdminCourseDTO> getCoursesByDateRange(LocalDate startDate, LocalDate endDate) {
    return courseRepository.findByStartDateBetween(startDate, endDate).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }
  
  /**
   * Retrieves courses within a date range for public/client use.
   */
  public List<ClientCourseDTO> getCoursesByDateRangeForPublic(LocalDate startDate, LocalDate endDate) {
    return courseRepository.findByStartDateBetween(startDate, endDate).stream()
        .filter(course -> course.isActive() && !course.isDeleted()) // Only show active, non-deleted courses
        .map(this::convertToPublicDTO)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves courses by awarded certification.
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public List<AdminCourseDTO> getCoursesByCertification(DiveCertification certification) {
    return courseRepository.findCourseByAwardedCertification(certification).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }
  
  /**
   * Retrieves courses by awarded certification for public/client use.
   */
  public List<ClientCourseDTO> getCoursesByCertificationForPublic(DiveCertification certification) {
    return courseRepository.findCourseByAwardedCertification(certification).stream()
        .filter(course -> course.isActive() && !course.isDeleted()) // Only show active, non-deleted courses
        .map(this::convertToPublicDTO)
        .collect(Collectors.toList());
  }

  /**
   * Creates a new course.
   *
   * @return the created AdminCourseDTO object
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Transactional
  public AdminCourseDTO createCourse(AdminCourseDTO adminCourseDTO) {
    Course course = convertToEntity(adminCourseDTO);
    Course savedCourse = courseRepository.save(course);
    return convertToDTO(savedCourse);
  }

  /**
   * Updates an existing course.
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Transactional
  public AdminCourseDTO updateCourse(Long id, AdminCourseDTO adminCourseDTO) {
    Course existingCourse = courseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

    updateCourseFromDTO(existingCourse, adminCourseDTO);
    Course updatedCourse = courseRepository.save(existingCourse);
    return convertToDTO(updatedCourse);
  }

  /**
   * Deletes a course by setting its isDeleted flag to true.
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Transactional
  public void deleteCourse(Long id) {
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    course.setDeleted(true);
    courseRepository.save(course);
  }

  /**
   * Permanently deletes a course from the database.
   */
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Transactional
  public void permanentlyDeleteCourse(Long id) {
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    courseRepository.delete(course);
  }

  /**
   * Converts a Course entity to a AdminCourseDTO.
   */
  private AdminCourseDTO convertToDTO(Course course) {
    return new AdminCourseDTO(
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
        course.isDeleted(),
        course.getImageUrl()
    );
  }
  
  /**
   * Converts a Course entity to a ClientCourseDTO containing only client-visible information.
   */
  private ClientCourseDTO convertToPublicDTO(Course course) {
    return new ClientCourseDTO(
        course.getId(),
        course.getCourseTitle(),
        course.getDescription(),
        course.getPrice(),
        course.getStartDate(),
        course.getEndDate(),
        course.getDuration(),
        course.getSpotsAvailable(),
        course.getRequiredCertification(),
        course.getAwardedCertification(),
        course.getIncludedItems(),
        course.getImageUrl()
    );
  }

  /**
   * Converts a AdminCourseDTO to a Course entity. This ensures the separation of concerns and not
   */
  private Course convertToEntity(AdminCourseDTO adminCourseDTO) {
    Course course = new Course();
    updateCourseFromDTO(course, adminCourseDTO);
    return course;
  }

  /**
   * Updates a Course entity with information from a AdminCourseDTO
   */
  private void updateCourseFromDTO(Course course, AdminCourseDTO adminCourseDTO) {
    course.setCourseTitle(adminCourseDTO.getCourseTitle());
    course.setDescription(adminCourseDTO.getDescription());
    course.setPrice(adminCourseDTO.getPrice());
    course.setStartDate(adminCourseDTO.getStartDate());
    course.setEndDate(adminCourseDTO.getEndDate());
    course.setDuration(adminCourseDTO.getDuration());
    course.setMaxParticipants(adminCourseDTO.getMaxParticipants());
    course.setSpotsAvailable(adminCourseDTO.getSpotsAvailable());
    course.setRequiredCertification(adminCourseDTO.getRequiredCertification());
    course.setProvidedCertification(adminCourseDTO.getProvidedCertification());
    course.setAwardedCertification(adminCourseDTO.getAwardedCertification());
    course.setIncludedItems(adminCourseDTO.getIncludedItems());
    course.setActive(adminCourseDTO.getIsActive());
    course.setDeleted(adminCourseDTO.getIsDeleted());
    course.setImageUrl(adminCourseDTO.getImageUrl());
  }
}
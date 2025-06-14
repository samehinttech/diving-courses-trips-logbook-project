package ch.oceandive.repository;

import ch.oceandive.model.Course;
import ch.oceandive.utils.PublicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {

  //Find courses with a start date after the given date.
  List<Course> findByStartDateAfter(LocalDate date);

  // Find courses with a start date before the given date.
  List<Course> findByStartDateBefore(LocalDate date);

  // Find courses with a start date between the given dates.
  List<Course> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

  //Find courses by name (case-insensitive, partial match).
  List<Course> findByNameContainingIgnoreCase(String name);

  // For the Page-returning method
  @RestResource(path = "findByStatusAndFeaturedPage", rel = "findByStatusAndFeaturedPage")
  Page<Course> findByStatusAndFeaturedOrderByDisplayOrderAsc(PublicationStatus status, Boolean featured,
      Pageable pageable);

  // Find course by slug (for SEO-friendly URLs).
  Optional<Course> findBySlugAndStatus(String slug, PublicationStatus status);

  // In CourseRepo.java
  Page<Course> findByStatusOrderByCreatedAtDesc(PublicationStatus status, Pageable pageable);

  // Find available courses (list)
  @Query("SELECT c FROM Course c WHERE c.status = :status " +
      "AND c.currentBookings < c.capacity " +
      "AND c.startDate > :currentDate " +
      "ORDER BY c.startDate ASC")
  @RestResource(path = "findAvailableCoursesList", rel = "findAvailableCoursesList")
  List<Course> findAvailableCoursesList(@Param("status") PublicationStatus status,
      @Param("currentDate") LocalDate currentDate);

  // Find available courses with pagination
  @Query("SELECT c FROM Course c WHERE c.status = :status " +
      "AND c.currentBookings < c.capacity " +
      "AND c.startDate > :currentDate " +
      "ORDER BY c.startDate ASC")
  @RestResource(path = "findAvailableCoursesPage", rel = "findAvailableCoursesPage")
  Page<Course> findAvailableCoursesPage(@Param("status") PublicationStatus status,
      @Param("currentDate") LocalDate currentDate,
      Pageable pageable);

  // Find upcoming courses (published, future start date) regardless of booking status.
  @Query("SELECT c FROM Course c WHERE c.status = :status " +
      "AND c.startDate > :currentDate " +
      "ORDER BY c.startDate ASC")
  List<Course> findUpcomingCourses(@Param("status") PublicationStatus status,
      @Param("currentDate") LocalDate currentDate);

  // Count total published courses.
  long countByStatus(PublicationStatus status);

  // Count available course spots.
  @Query("SELECT SUM(c.capacity - c.currentBookings) FROM Course c " +
      "WHERE c.status = :status AND c.currentBookings < c.capacity " +
      "AND c.startDate > :currentDate")
  Long countAvailableSpots(@Param("status") PublicationStatus status,
      @Param("currentDate") LocalDate currentDate);

  // Search courses by name, description, or short description and order by creation day (case-insensitive, partial match).
  @Query("SELECT c FROM Course c WHERE " +
      "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
      "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
      "OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
      "AND c.status = :status " +
      "ORDER BY c.createdAt DESC")
  Page<Course> searchCourses(@Param("searchTerm") String searchTerm,
      @Param("status") PublicationStatus status,
      Pageable pageable);

  // Find all courses for admin (regardless of status) with search capability.
  @Query("SELECT c FROM Course c WHERE " +
      "(:searchTerm IS NULL OR " +
      "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
      "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
      "ORDER BY c.createdAt DESC")
  Page<Course> findAllForAdmin(@Param("searchTerm") String searchTerm, Pageable pageable);

  // Check if a slug exists (for validation).
  boolean existsBySlug(String slug);

  //Check if slug exists for a different course (for updates).
  boolean existsBySlugAndIdNot(String slug, Long id);


}
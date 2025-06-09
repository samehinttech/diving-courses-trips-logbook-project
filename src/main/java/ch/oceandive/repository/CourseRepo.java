package ch.oceandive.repository;

import ch.oceandive.model.Course;
import ch.oceandive.model.CourseStatus;
import ch.oceandive.model.DiveCertification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

  //Find courses that are not fully booked.
  @Query("SELECT c FROM Course c WHERE c.currentBookings < c.capacity")
  List<Course> findByCurrentBookingsLessThanCapacity();

  // Find featured courses that are published, ordered by display order.
  List<Course> findByStatusAndFeaturedOrderByDisplayOrderAsc(CourseStatus status, Boolean featured);

  // Find featured courses with pagination.
  Page<Course> findByStatusAndFeaturedOrderByDisplayOrderAsc(CourseStatus status, Boolean featured,
      Pageable pageable);

  //Find all published courses ordered by start date.
  List<Course> findByStatusOrderByStartDateAsc(CourseStatus status);

  // Find all courses by status with pagination.
  Page<Course> findByStatusOrderByCreatedAtDesc(CourseStatus status, Pageable pageable);

  // Find course by slug (for SEO-friendly URLs).
  Optional<Course> findBySlugAndStatus(String slug, CourseStatus status);

  // Find available courses (published, not full, future start date).
  @Query("SELECT c FROM Course c WHERE c.status = :status " +
      "AND c.currentBookings < c.capacity " +
      "AND c.startDate > :currentDate " +
      "ORDER BY c.startDate ASC")
  List<Course> findAvailableCourses(@Param("status") CourseStatus status,
      @Param("currentDate") LocalDate currentDate);

  // Find available courses with pagination.
  @Query("SELECT c FROM Course c WHERE c.status = :status " +
      "AND c.currentBookings < c.capacity " +
      "AND c.startDate > :currentDate " +
      "ORDER BY c.startDate ASC")
  Page<Course> findAvailableCourses(@Param("status") CourseStatus status,
      @Param("currentDate") LocalDate currentDate,
      Pageable pageable);

  // Find upcoming courses (published, future start date) regardless of booking status.
  @Query("SELECT c FROM Course c WHERE c.status = :status " +
      "AND c.startDate > :currentDate " +
      "ORDER BY c.startDate ASC")
  List<Course> findUpcomingCourses(@Param("status") CourseStatus status,
      @Param("currentDate") LocalDate currentDate);
  // Count total published courses.

  long countByStatus(CourseStatus status);

  // Count available course spots.
  @Query("SELECT SUM(c.capacity - c.currentBookings) FROM Course c " +
      "WHERE c.status = :status AND c.currentBookings < c.capacity " +
      "AND c.startDate > :currentDate")
  Long countAvailableSpots(@Param("status") CourseStatus status,
      @Param("currentDate") LocalDate currentDate);
  // Find courses by certification level.

  List<Course> findByMinCertificationRequiredAndStatusOrderByStartDateAsc(
      DiveCertification certification, CourseStatus status);

  // Search courses by name or description (for admin search functionality).

  @Query("SELECT c FROM Course c WHERE " +
      "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
      "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
      "OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
      "AND c.status = :status " +
      "ORDER BY c.createdAt DESC")
  Page<Course> searchCourses(@Param("searchTerm") String searchTerm,
      @Param("status") CourseStatus status,
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

  // Find similar courses by name (for recommendations).

  @Query("SELECT c FROM Course c WHERE " +
      "c.id != :courseId " +
      "AND c.status = :status " +
      "AND (c.minCertificationRequired = :certification " +
      "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :nameKeyword, '%'))) " +
      "ORDER BY c.startDate ASC")
  List<Course> findSimilarCourses(@Param("courseId") Long courseId,
      @Param("status") CourseStatus status,
      @Param("certification") DiveCertification certification,
      @Param("nameKeyword") String nameKeyword,
      Pageable pageable);

  // Get course statistics for admin dashboard.

  @Query("SELECT " +
      "COUNT(c) as totalCourses, " +
      "COUNT(CASE WHEN c.status = 'PUBLISHED' THEN 1 END) as publishedCourses, " +
      "COUNT(CASE WHEN c.status = 'DRAFT' THEN 1 END) as draftCourses, " +
      "COUNT(CASE WHEN c.featured = true THEN 1 END) as featuredCourses, " +
      "SUM(c.currentBookings) as totalBookings, " +
      "SUM(c.capacity) as totalCapacity " +
      "FROM Course c")
  Object[] getCourseStatistics();

  // Get monthly course creation statistics.

  @Query("SELECT " +
      "YEAR(c.createdAt) as year, " +
      "MONTH(c.createdAt) as month, " +
      "COUNT(c) as courseCount " +
      "FROM Course c " +
      "WHERE c.createdAt >= :startDate " +
      "GROUP BY YEAR(c.createdAt), MONTH(c.createdAt) " +
      "ORDER BY YEAR(c.createdAt) DESC, MONTH(c.createdAt) DESC")
  List<Object[]> getMonthlyCreationStats(@Param("startDate") LocalDate startDate);
}
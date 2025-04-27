package ch.oceandive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.oceandive.model.Course;
import ch.oceandive.model.DiveCertification;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

  /**
   * Find all active courses.
   *
   * @return a list of active courses
   */
  List<Course> findCoursesByActive(boolean active);

  List<Course> findByCourseNameContainingIgnoreCase(String courseName);

  /**
   * Find courses that provide a specific certification.
   *
   * @param certification the certification provided by the the given course
   * @return a list of courses providing that certification
   */
  List<Course> findCoursesByProvidedCertification(DiveCertification certification);

  /**
   * Find courses that require a specific certification or lower.
   *
   * @param certification the maximum certification required
   * @return a list of courses requiring the certification or lower
   */
  List<Course> findCourserByRequiredCertification(DiveCertification certification);


  /**
   * Find active courses by course name containing the given text (case-insensitive).
   * @param courseName the course name text to search for
   * @return a list of matching active courses
   */

  List<Course> findByActiveTrueAndCourseNameContainingIgnoreCase(String courseName);


}

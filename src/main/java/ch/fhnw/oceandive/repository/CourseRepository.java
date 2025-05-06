package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.DiveCertification;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

  List<Course> findByCourseTitleContainingIgnoreCase(String courseTitle);

  List<Course> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

  List<Course> findCourseByAwardedCertification(DiveCertification awardedCertification);

  List<Course> findAllByIsActiveTrue();

  List<Course> findAllByIsDeletedTrue();

  List<Course> findAllByIsActiveTrueAndIsDeletedFalse();

  List<Course> findCourseByIsActiveTrue();


}

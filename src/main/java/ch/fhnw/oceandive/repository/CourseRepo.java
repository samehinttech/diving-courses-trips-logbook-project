package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Course entities.
 */
@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {

    /**
     * Find courses with start date after the given date.
     * 
     * @param date The date to compare with
     * @return List of courses starting after the given date
     */
    List<Course> findByStartDateAfter(LocalDate date);

    /**
     * Find courses with start date before the given date.
     * 
     * @param date The date to compare with
     * @return List of courses starting before the given date
     */
    List<Course> findByStartDateBefore(LocalDate date);

    /**
     * Find courses with start date between the given dates.
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of courses starting between the given dates
     */
    List<Course> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find courses by location.
     * 
     * @param location The location to search for
     * @return List of courses at the given location
     */
    List<Course> findByLocationContainingIgnoreCase(String location);

    /**
     * Find courses that are not fully booked.
     * 
     * @return List of courses that are not fully booked
     */
    @Query("SELECT c FROM Course c WHERE c.currentBookings < c.capacity")
    List<Course> findByCurrentBookingsLessThanCapacity();
}

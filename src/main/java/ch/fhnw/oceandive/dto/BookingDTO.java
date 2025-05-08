package ch.fhnw.oceandive.dto;

import ch.fhnw.oceandive.model.Booking;
import ch.fhnw.oceandive.model.Booking.BookingStatus;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.model.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO for {@link ch.fhnw.oceandive.model.Booking}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDTO implements Serializable {

    private final Long id;
    private final String userId;
    private final Long tripId;
    private final Long courseId;
    private final Integer numberOfBookings;
    private final LocalDateTime bookingDate;
    private final BookingStatus status;

    // Optional fields for display purposes
    private final String userName;
    private final String tripName;
    private final String courseName;

    public BookingDTO(Long id, String userId, Long tripId, Long courseId, Integer numberOfBookings,
                     LocalDateTime bookingDate, BookingStatus status, String userName, 
                     String tripName, String courseName) {
        this.id = id;
        this.userId = userId;
        this.tripId = tripId;
        this.courseId = courseId;
        this.numberOfBookings = numberOfBookings;
        this.bookingDate = bookingDate;
        this.status = status;
        this.userName = userName;
        this.tripName = tripName;
        this.courseName = courseName;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Long getTripId() {
        return tripId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Integer getNumberOfBookings() {
        return numberOfBookings;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getUserName() {
        return userName;
    }

    public String getTripName() {
        return tripName;
    }

    public String getCourseName() {
        return courseName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        BookingDTO entity = (BookingDTO) obj;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.userId, entity.userId) &&
                Objects.equals(this.tripId, entity.tripId) &&
                Objects.equals(this.courseId, entity.courseId) &&
                Objects.equals(this.numberOfBookings, entity.numberOfBookings) &&
                Objects.equals(this.bookingDate, entity.bookingDate) &&
                Objects.equals(this.status, entity.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, tripId, courseId, numberOfBookings, bookingDate, status);
    }

    // Mapper methods to convert Booking to BookingDTO and vice versa
    public static BookingDTO fromEntity(Booking booking) {
        return new BookingDTO(
                booking.getId(),
                booking.getUser() != null ? booking.getUser().getId().toString() : null,
                booking.getTrip() != null ? booking.getTrip().getId() : null,
                booking.getCourse() != null ? booking.getCourse().getId() : null,
                booking.getNumberOfBookings(),
                booking.getBookingDate(),
                booking.getStatus(),
                booking.getUser() != null ? booking.getUser().getUsername() : null,
                booking.getTrip() != null ? booking.getTrip().getTripTitle() : null,
                booking.getCourse() != null ? booking.getCourse().getCourseTitle() : null
        );
    }

    public static Booking toEntity(BookingDTO bookingDTO, UserEntity user, Trip trip, Course course) {
        Booking booking = new Booking(
                user,
                trip,
                course,
                bookingDTO.getNumberOfBookings(),
                bookingDTO.getBookingDate(),
                bookingDTO.getStatus()
        );
        if (bookingDTO.getId() != null) {
            booking.setId(bookingDTO.getId());
        }
        return booking;
    }
}

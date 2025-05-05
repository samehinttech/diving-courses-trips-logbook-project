package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity(name = "Bookings")
public class Booking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trip_id", nullable = false)
  private Trip trip;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "number_of_bookings", nullable = false)
  private Integer numberOfBookings;

  @NotNull
  @CreationTimestamp
  private LocalDateTime bookingDate;

  @NotNull
  @Enumerated(EnumType.STRING)
  private BookingStatus status;

  public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    REJECTED
  }

  public Booking() {

  }

  public Booking(UserEntity user, Trip trip, Course course, Integer numberOfBookings,
      LocalDateTime bookingDate, BookingStatus status) {
    this.user = user;
    this.trip = trip;
    this.course = course;
    this.numberOfBookings = numberOfBookings;
    this.bookingDate = bookingDate;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public Trip getTrip() {
    return trip;
  }

  public void setTrip(Trip trip) {
    this.trip = trip;
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  public Integer getNumberOfBookings() {
    return numberOfBookings;
  }

  public void setNumberOfBookings(Integer numberOfBookings) {
    this.numberOfBookings = numberOfBookings;
  }

  public LocalDateTime getBookingDate() {
    return bookingDate;
  }

  public void setBookingDate(LocalDateTime bookingDate) {
    this.bookingDate = bookingDate;
  }

  public BookingStatus getStatus() {
    return status;
  }

  public void setStatus(BookingStatus status) {
    this.status = status;
  }
}

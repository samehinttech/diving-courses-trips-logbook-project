package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Entity representing a diving trip offered by the system. Trips can be booked by users and
 * customers.
 */
@Entity
@Table(name = "trips")
public class Trip {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, length = 1000)
  private String description;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  // Duration is now calculated from startDate and endDate

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  private String imageUrl;

  @Column(nullable = false)
  private Integer capacity;

  @Column(nullable = false)
  private Integer currentBookings;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DiveCertification minCertificationRequired;

  // Default constructor
  public Trip() {
    this.currentBookings = 0;
  }

  // Constructor
  public Trip(String name, String description, LocalDate startDate, LocalDate endDate,
      String imageUrl, Integer capacity,
      DiveCertification minCertificationRequired) {
    this.name = name;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.imageUrl = imageUrl;
    this.capacity = capacity;
    this.currentBookings = 0;
    this.minCertificationRequired = minCertificationRequired;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public Integer getDuration() {
    return (endDate != null && startDate != null) ? (int) (endDate.toEpochDay()
        - startDate.toEpochDay()) : null;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  public Integer getCurrentBookings() {
    return currentBookings;
  }

  public void setCurrentBookings(Integer currentBookings) {
    this.currentBookings = currentBookings;
  }

  public DiveCertification getMinCertificationRequired() {
    return minCertificationRequired;
  }

  public void setMinCertificationRequired(DiveCertification minCertificationRequired) {
    this.minCertificationRequired = minCertificationRequired;
  }

  // Helper method to check if trip is fully booked
  public boolean isFullyBooked() {
    return currentBookings >= capacity;
  }

  // Helper method to increment bookings
  public void incrementBookings() {
    if (currentBookings < capacity) {
      currentBookings++;
    }
  }
}

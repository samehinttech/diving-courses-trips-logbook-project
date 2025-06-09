package ch.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "trips", indexes = {
    @Index(name = "idx_trip_start_date", columnList = "startDate"),
    @Index(name = "idx_trip_location", columnList = "location"),
    @Index(name = "idx_trip_certification", columnList = "minCertificationRequired"),
    @Index(name = "idx_trip_availability", columnList = "currentBookings, capacity")
})
public class Trip {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  @NotBlank(message = "Trip location is required")
  @Size(min = 2, max = 200, message = "Location must be between 2 and 200 characters")
  private String location;

  @Column(nullable = false, length = 2000)
  @NotBlank(message = "Trip description is required")
  @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
  private String description;

  @Column(nullable = false)
  @NotNull(message = "Start date is required")
  @Future(message = "Start date must be in the future")
  private LocalDate startDate;

  @Column(nullable = false)
  @NotNull(message = "End date is required")
  @Future(message = "End date must be in the future")
  private LocalDate endDate;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(length = 500)
  @Size(max = 500, message = "Image URL must not exceed 500 characters")
  @Pattern(regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp))$|^$",
      message = "Image URL must be a valid URL pointing to an image file")
  private String imageUrl;

  @Column(nullable = false)
  @NotNull(message = "Capacity is required")
  @Min(value = 1, message = "Capacity must be at least 1")
  @Max(value = 50, message = "Capacity cannot exceed 50 participants")
  private Integer capacity;

  @Column(nullable = false)
  @Min(value = 0, message = "Current bookings cannot be negative")
  private Integer currentBookings;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @NotNull(message = "Minimum certification is required")
  private DiveCertification minCertificationRequired;

  // Default constructor
  public Trip() {
    this.currentBookings = 0;
  }

  // Constructor with required fields
  public Trip(@NotBlank String location,
      @NotBlank String description,
      @NotNull LocalDate startDate,
      @NotNull LocalDate endDate,
      @NotNull Integer capacity,
      @NotNull DiveCertification minCertificationRequired) {
    this.location = location;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.capacity = capacity;
    this.currentBookings = 0;
    this.minCertificationRequired = minCertificationRequired;
  }

  // Full constructor
  public Trip(@NotBlank String location,
      @NotBlank String description,
      @NotNull LocalDate startDate,
      @NotNull LocalDate endDate,
      String imageUrl,
      @NotNull Integer capacity,
      @NotNull DiveCertification minCertificationRequired) {
    this.location = location;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.imageUrl = imageUrl;
    this.capacity = capacity;
    this.currentBookings = 0;
    this.minCertificationRequired = minCertificationRequired;
  }

  // ===== GETTERS AND SETTERS =====

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location != null ? location.trim() : null;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description != null ? description.trim() : null;
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

  /**
   * Calculate trip duration in days.
   * @return duration in days, or null if dates are not set
   */
  public Integer getDuration() {
    if (endDate != null && startDate != null) {
      return (int) (endDate.toEpochDay() - startDate.toEpochDay() + 1);
    }
    return null;
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
    this.imageUrl = imageUrl != null ? imageUrl.trim() : null;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  public Integer getCurrentBookings() {
    return currentBookings != null ? currentBookings : 0;
  }

  public void setCurrentBookings(Integer currentBookings) {
    this.currentBookings = currentBookings != null ? Math.max(0, currentBookings) : 0;
  }

  public DiveCertification getMinCertificationRequired() {
    return minCertificationRequired;
  }

  public void setMinCertificationRequired(DiveCertification minCertificationRequired) {
    this.minCertificationRequired = minCertificationRequired;
  }

  // ===== BUSINESS LOGIC METHODS =====

  /**
   * Check if trip is fully booked.
   * @return true if current bookings >= capacity
   */
  public boolean isFullyBooked() {
    return getCurrentBookings() >= (capacity != null ? capacity : 0);
  }

  /**
   * Get available spots.
   * @return number of available spots
   */
  public int getAvailableSpots() {
    return Math.max(0, (capacity != null ? capacity : 0) - getCurrentBookings());
  }

  /**
   * Get booking percentage.
   * @return percentage of capacity booked (0-100)
   */
  public double getBookingPercentage() {
    if (capacity == null || capacity == 0) {
      return 0.0;
    }
    return (getCurrentBookings() * 100.0) / capacity;
  }

  /**
   * Increment bookings by 1 if capacity allows.
   * @return true if booking was successful, false if trip is full
   */
  public boolean incrementBookings() {
    if (isFullyBooked()) {
      return false;
    }
    setCurrentBookings(getCurrentBookings() + 1);
    return true;
  }

  /**
   * Decrement bookings by 1 if there are current bookings.
   * @return true if cancellation was successful, false if no bookings exist
   */
  public boolean decrementBookings() {
    if (getCurrentBookings() <= 0) {
      return false;
    }
    setCurrentBookings(getCurrentBookings() - 1);
    return true;
  }

  /**
   * Check if trip is in the past.
   * @return true if trip has ended
   */
  public boolean isPastTrip() {
    return endDate != null && endDate.isBefore(LocalDate.now());
  }

  /**
   * Check if trip is currently active (ongoing).
   * @return true if trip is currently happening
   */
  public boolean isActiveTrip() {
    LocalDate today = LocalDate.now();
    return startDate != null && endDate != null &&
        !startDate.isAfter(today) && !endDate.isBefore(today);
  }

  /**
   * Check if trip is upcoming.
   * @return true if trip hasn't started yet
   */
  public boolean isUpcomingTrip() {
    return startDate != null && startDate.isAfter(LocalDate.now());
  }

  /**
   * Check if trip starts soon (within next 7 days).
   * @return true if trip starts within 7 days
   */
  public boolean isStartingSoon() {
    if (startDate == null) {
      return false;
    }
    LocalDate today = LocalDate.now();
    LocalDate oneWeekFromNow = today.plusDays(7);
    return !startDate.isBefore(today) && !startDate.isAfter(oneWeekFromNow);
  }

  /**
   * Get trip status as string.
   * @return status description
   */
  public String getStatus() {
    if (isPastTrip()) {
      return "Completed";
    } else if (isActiveTrip()) {
      return "Active";
    } else if (isStartingSoon()) {
      return "Starting Soon";
    } else if (isUpcomingTrip()) {
      return "Upcoming";
    } else {
      return "Unknown";
    }
  }

  /**
   * Check if trip can be modified (not started yet and no bookings).
   * @return true if trip can be safely modified
   */
  public boolean canBeModified() {
    return isUpcomingTrip() && getCurrentBookings() == 0;
  }

  /**
   * Check if trip can be cancelled (not started yet).
   * @return true if trip can be cancelled
   */
  public boolean canBeCancelled() {
    return isUpcomingTrip();
  }

  // ===== VALIDATION METHODS =====

  /**
   * Validate trip dates.
   * @throws IllegalArgumentException if dates are invalid
   */
  @AssertTrue(message = "End date must be after start date")
  public boolean isValidDateRange() {
    if (startDate == null || endDate == null) {
      return true; // Let @NotNull handle null validation
    }
    return !endDate.isBefore(startDate);
  }

  /**
   * Validate current bookings don't exceed capacity.
   * @throws IllegalArgumentException if bookings exceed capacity
   */
  @AssertTrue(message = "Current bookings cannot exceed capacity")
  public boolean isValidBookingCount() {
    if (capacity == null || currentBookings == null) {
      return true; // Let @NotNull handle null validation
    }
    return currentBookings <= capacity;
  }

  /**
   * Validate trip duration is reasonable (max 30 days).
   * @return true if duration is reasonable
   */
  @AssertTrue(message = "Trip duration cannot exceed 30 days")
  public boolean isValidDuration() {
    Integer duration = getDuration();
    return duration == null || duration <= 30;
  }

  // ===== EQUALS, HASHCODE, TOSTRING =====

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Trip trip = (Trip) o;
    return Objects.equals(id, trip.id) &&
        Objects.equals(location, trip.location) &&
        Objects.equals(startDate, trip.startDate) &&
        Objects.equals(endDate, trip.endDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, location, startDate, endDate);
  }

  @Override
  public String toString() {
    return "Trip{" +
        "id=" + id +
        ", location='" + location + '\'' +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        ", capacity=" + capacity +
        ", currentBookings=" + currentBookings +
        ", minCertificationRequired=" + minCertificationRequired +
        ", status='" + getStatus() + '\'' +
        '}';
  }

  // ===== BUILDER PATTERN (Optional) =====

  /**
   * Builder class for creating Trip instances.
   */
  public static class Builder {
    private String location;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String imageUrl;
    private Integer capacity;
    private DiveCertification minCertificationRequired;

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder startDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    public Builder endDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    public Builder imageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
      return this;
    }

    public Builder capacity(Integer capacity) {
      this.capacity = capacity;
      return this;
    }

    public Builder minCertificationRequired(DiveCertification certification) {
      this.minCertificationRequired = certification;
      return this;
    }

    public Trip build() {
      return new Trip(location, description, startDate, endDate,
          imageUrl, capacity, minCertificationRequired);
    }
  }

  /**
   * Create a new builder instance.
   * @return new Builder
   */
  public static Builder builder() {
    return new Builder();
  }
}
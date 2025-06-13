package ch.oceandive.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "trips", indexes = {
    @Index(name = "idx_trip_start_date", columnList = "startDate"),
    @Index(name = "idx_trip_location", columnList = "location"),
    @Index(name = "idx_trip_certification", columnList = "minCertificationRequired"),
    @Index(name = "idx_trip_availability", columnList = "currentBookings, capacity"),
    @Index(name = "idx_trip_status", columnList = "status"),
    @Index(name = "idx_trip_featured", columnList = "featured"),
    @Index(name = "idx_trip_slug", columnList = "slug")
})
public class Trip {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Hidden
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "Trip location is required")
  @Size(min = 2, max = 100, message = "Location must be between 2 and 100 characters")
  private String location;

  @Hidden
  @Column(nullable = false, length = 1000)
  @NotBlank(message = "Trip description is required")
  @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
  private String description;

  // Short description for homepage cards
  @Column(name = "short_description", length = 300)
  @Size(max = 300, message = "Short description cannot exceed 300 characters")
  private String shortDescription;

  @Column(nullable = false)
  @NotNull(message = "Start date is required")
  @Future(message = "Start date must be in the future")
  private LocalDate startDate;

  @Column(nullable = false)
  @NotNull(message = "End date is required")
  private LocalDate endDate;

  @Column(nullable = false)
  @CreationTimestamp
  @JsonIgnore
  private LocalDateTime createdAt;

  @Column(name = "image_url")
  @JsonIgnore
  private String imageUrl;

  @Column(nullable = false)
  @NotNull(message = "Capacity is required")
  @Min(value = 1, message = "Capacity must be at least 1")
  @Max(value = 50, message = "Capacity cannot exceed 50")
  private Integer capacity;

  @Column(name = "current_bookings", nullable = false)
  @Min(value = 0, message = "Current bookings cannot be negative")
  private Integer currentBookings;

  @Enumerated(EnumType.STRING)
  @Column(name = "min_certification_required", nullable = false)
  @NotNull(message = "Minimum certification is required")
  private DiveCertification minCertificationRequired;

  @Column(name = "price", precision = 10, scale = 2)
  @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
  @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
  private BigDecimal price;

  @Hidden
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @NotNull(message = "Status is required")
  private PublicationStatus status;

  @Hidden
  @Column(name = "featured", nullable = false)
  private Boolean featured = false;

  @Hidden
  @Column(name = "display_order", nullable = false)
  @Min(value = 0, message = "Display order cannot be negative")
  private Integer displayOrder = 0;

  @Column(name = "slug", unique = true, length = 200)
  @Size(max = 200, message = "Slug cannot exceed 200 characters")
  private String slug;

  @JsonIgnore
  @Column(name = "updated_at")
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  // Constructor with required fields
  public Trip() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.currentBookings = 0;
    this.status = PublicationStatus.PUBLISHED;
    this.featured = false;
    this.displayOrder = 0;
  }

  public Trip(String location, String description, LocalDate startDate, LocalDate endDate,
      String imageUrl, Integer capacity, DiveCertification minCertificationRequired,
      BigDecimal price) {
    this();
    this.location = location;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.imageUrl = imageUrl;
    this.capacity = capacity;
    this.minCertificationRequired = minCertificationRequired;
    this.price = price;
    this.slug = generateSlug(location);
  }

  @PreUpdate
  @PrePersist
  public void updateTimestampAndSlug() {
    this.updatedAt = LocalDateTime.now();
    if (this.slug == null || this.slug.isEmpty()) {
      this.slug = generateSlug(this.location + " " + this.startDate);
    }
    // Ensure short description exists
    if (this.shortDescription == null || this.shortDescription.isEmpty()) {
      this.shortDescription = this.description != null && this.description.length() > 150
          ? this.description.substring(0, 150) + "..."
          : this.description;
    }
    // Ensure current bookings is never null
    if (this.currentBookings == null) {
      this.currentBookings = 0;
    }
  }

  public String generateSlug(String input) {
    if (input == null || input.trim().isEmpty()) {
      return "trip-" + System.currentTimeMillis();
    }
    return input.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "")
        .substring(0, Math.min(input.length(), 190)); // Leave room for potential suffixes
  }

  // Enhanced helper methods
  public Integer getDuration() {
    if (startDate == null || endDate == null) {
      return 0;
    }
    return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
  }

  public void setDuration(Integer duration) {
    if (startDate != null && duration != null && duration > 0) {
      this.endDate = startDate.plusDays(duration - 1);
    }
  }

  public boolean isFullyBooked() {
    return getCurrentBookings() >= (capacity != null ? capacity : 0);
  }

  public void incrementBookings() {
    if (!isFullyBooked()) {
      setCurrentBookings(getCurrentBookings() + 1);
    }
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
   * Get available spots.
   *
   * @return number of available spots
   */
  public int getAvailableSpots() {
    return Math.max(0, (capacity != null ? capacity : 0) - getCurrentBookings());
  }

  /**
   * Get booking percentage.
   *
   * @return percentage of capacity booked (0-100)
   */
  public double getBookingPercentage() {
    if (capacity == null || capacity == 0) {
      return 0.0;
    }
    return (getCurrentBookings() * 100.0) / capacity;
  }

  /**
   * Decrement bookings by 1 if there are current bookings.
   *
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
   *
   * @return true if trip has ended
   */
  public boolean isPastTrip() {
    return endDate != null && endDate.isBefore(LocalDate.now());
  }

  /**
   * Check if trip is currently active (ongoing).
   *
   * @return true if trip is currently happening
   */
  public boolean isActiveTrip() {
    LocalDate today = LocalDate.now();
    return startDate != null && endDate != null &&
        !startDate.isAfter(today) && !endDate.isBefore(today);
  }

  /**
   * Check if trip is upcoming.
   *
   * @return true if trip hasn't started yet
   */
  public boolean isUpcomingTrip() {
    return startDate != null && startDate.isAfter(LocalDate.now());
  }

  /**
   * Check if trip starts soon (within next 7 days).
   *
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
   *
   * @return status description
   */
  public String getTripStatus() {
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
   *
   * @return true if trip can be safely modified
   */
  public boolean canBeModified() {
    return isUpcomingTrip() && getCurrentBookings() == 0;
  }

  /**
   * Check if trip can be cancelled (not started yet).
   *
   * @return true if trip can be cancelled
   */
  public boolean canBeCancelled() {
    return isUpcomingTrip();
  }

  // ===== VALIDATION METHODS =====

  /**
   * Validate trip dates.
   *
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
   *
   * @throws IllegalArgumentException if bookings exceed capacity
   */
  @AssertTrue(message = "Current bookings cannot exceed capacity")
  public boolean isValidBookingCount() {
    if (capacity == null) {
      return true; // Let @NotNull handle null validation
    }
    return getCurrentBookings() <= capacity;
  }

  /**
   * Validate trip duration is reasonable (max 30 days).
   *
   * @return true if duration is reasonable
   */
  @AssertTrue(message = "Trip duration cannot exceed 30 days")
  public boolean isValidDuration() {
    Integer duration = getDuration();
    return duration == null || duration <= 30;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public Trip setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public Trip setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription != null ? shortDescription.trim() : null;
    return this;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public Trip setPrice(BigDecimal price) {
    this.price = price;
    return this;
  }

  public PublicationStatus getStatus() {
    return status;
  }

  public Trip setStatus(PublicationStatus status) {
    this.status = status != null ? status : PublicationStatus.PUBLISHED;
    return this;
  }

  public Boolean getFeatured() {
    return featured;
  }

  public Trip setFeatured(Boolean featured) {
    this.featured = featured != null ? featured : false;
    return this;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public Trip setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder != null ? Math.max(0, displayOrder) : 0;
    return this;
  }

  public String getSlug() {
    return slug;
  }

  public Trip setSlug(String slug) {
    this.slug = slug != null ? slug.trim() : null;
    return this;
  }

  // ===== EQUALS, HASHCODE, AND TOSTRING =====

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Trip trip = (Trip) o;
    return Objects.equals(id, trip.id) &&
        Objects.equals(slug, trip.slug);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, slug);
  }

  @Override
  public String toString() {
    return "Trip{" +
        "id=" + id +
        ", location='" + location + '\'' +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        ", capacity=" + capacity +
        ", currentBookings=" + getCurrentBookings() +
        ", status=" + status +
        ", slug='" + slug + '\'' +
        '}';
  }
}
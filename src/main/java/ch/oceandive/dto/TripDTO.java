package ch.oceandive.dto;

import ch.oceandive.utils.PublicationStatus;
import ch.oceandive.utils.DiveCertification;
import ch.oceandive.model.Trip;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * DTO for {@link ch.oceandive.model.Trip}
 */
public class TripDTO implements Serializable {

  private final Long id;

  @NotBlank(message = "Trip location is required")
  @Size(min = 2, max = 100, message = "Location must be between 2 and 100 characters")
  private final String location;

  @NotBlank(message = "Trip description is required")
  @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
  private final String description;

  @Size(max = 300, message = "Short description cannot exceed 300 characters")
  private final String shortDescription;

  @NotNull(message = "Start date is required")
  private final LocalDate startDate;

  @NotNull(message = "End date is required")
  private final LocalDate endDate;

  private final LocalDateTime createdAt;
  private final String imageUrl;

  @NotNull(message = "Capacity is required")
  @Min(message = "Capacity must be at least 1", value = 1)
  private final Integer capacity;

  @Min(value = 0, message = "Current bookings cannot be negative")
  private final Integer currentBookings;

  @NotNull(message = "Minimum certification is required")
  private final DiveCertification minCertificationRequired;

  @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
  @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
  private final BigDecimal price;

  @NotNull(message = "Status is required")
  private final PublicationStatus status;

  private final Boolean featured;

  @Min(value = 0, message = "Display order cannot be negative")
  private final Integer displayOrder;

  @Size(max = 200, message = "Slug cannot exceed 200 characters")
  private final String slug;

  private final LocalDateTime updatedAt;

  // Constructor from Trip entity
  public TripDTO(Trip trip) {
    this.id = trip.getId();
    this.location = trip.getLocation();
    this.description = trip.getDescription();
    this.shortDescription = trip.getShortDescription();
    this.startDate = trip.getStartDate();
    this.endDate = trip.getEndDate();
    this.createdAt = trip.getCreatedAt();
    this.imageUrl = trip.getImageUrl();
    this.capacity = trip.getCapacity();
    this.currentBookings = trip.getCurrentBookings();
    this.minCertificationRequired = trip.getMinCertificationRequired();
    this.price = trip.getPrice();
    this.status = trip.getStatus();
    this.featured = trip.getFeatured();
    this.displayOrder = trip.getDisplayOrder();
    this.slug = trip.getSlug();
    this.updatedAt = trip.getUpdatedAt();
  }

  // Full constructor
  public TripDTO(Long id, String location, String description, String shortDescription,
      LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, String imageUrl,
      Integer capacity, Integer currentBookings, DiveCertification minCertificationRequired,
      BigDecimal price, PublicationStatus status, Boolean featured, Integer displayOrder, String slug,
      LocalDateTime updatedAt) {
    this.id = id;
    this.location = location;
    this.description = description;
    this.shortDescription = shortDescription;
    this.startDate = startDate;
    this.endDate = endDate;
    this.createdAt = createdAt;
    this.imageUrl = imageUrl;
    this.capacity = capacity;
    this.currentBookings = currentBookings;
    this.minCertificationRequired = minCertificationRequired;
    this.price = price;
    this.status = status;
    this.featured = featured;
    this.displayOrder = displayOrder;
    this.slug = slug;
    this.updatedAt = updatedAt;
  }


  /**
   * Get trip duration in days.
   */
  public Integer getDuration() {
    if (startDate == null || endDate == null) {
      return 0;
    }
    return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
  }

  /**
   * Get available spots.
   */
  public int getAvailableSpots() {
    return Math.max(0, (capacity != null ? capacity : 0) - (currentBookings != null ? currentBookings : 0));
  }

  /**
   * Get booking percentage.
   */
  public double getBookingPercentage() {
    if (capacity == null || capacity == 0) {
      return 0.0;
    }
    return ((currentBookings != null ? currentBookings : 0) * 100.0) / capacity;
  }

  /**
   * Check if trip is fully booked.
   */
  public boolean isFullyBooked() {
    return (currentBookings != null ? currentBookings : 0) >= (capacity != null ? capacity : 0);
  }

  /**
   * Check if trip is in the past.
   */
  public boolean isPastTrip() {
    return endDate != null && endDate.isBefore(LocalDate.now());
  }

  /**
   * Check if trip is currently active (ongoing).
   */
  public boolean isActiveTrip() {
    LocalDate today = LocalDate.now();
    return startDate != null && endDate != null &&
        !startDate.isAfter(today) && !endDate.isBefore(today);
  }

  /**
   * Check if trip is upcoming.
   */
  public boolean isUpcomingTrip() {
    return startDate != null && startDate.isAfter(LocalDate.now());
  }

  /**
   * Check if trip starts soon (within next 7 days).
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
   * Convert DTO to Trip entity.
   */
  public Trip toEntity() {
    Trip trip = new Trip();
    // Don't set ID for new entities, let it be auto-generated
    trip.setLocation(this.location);
    trip.setDescription(this.description);
    trip.setShortDescription(this.shortDescription);
    trip.setStartDate(this.startDate);
    trip.setEndDate(this.endDate);
    trip.setImageUrl(this.imageUrl);
    trip.setCapacity(this.capacity);
    trip.setCurrentBookings(this.currentBookings);
    trip.setMinCertificationRequired(this.minCertificationRequired);
    trip.setPrice(this.price);
    trip.setStatus(this.status);
    trip.setFeatured(this.featured);
    trip.setDisplayOrder(this.displayOrder);
    trip.setSlug(this.slug);
    return trip;
  }

  // Getters
  public Long getId() {
    return id;
  }

  public String getLocation() {
    return location;
  }

  public String getDescription() {
    return description;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public Integer getCurrentBookings() {
    return currentBookings;
  }

  public DiveCertification getMinCertificationRequired() {
    return minCertificationRequired;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public PublicationStatus getStatus() {
    return status;
  }

  public Boolean getFeatured() {
    return featured;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public String getSlug() {
    return slug;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    TripDTO tripDTO = (TripDTO) obj;
    return Objects.equals(this.id, tripDTO.id) &&
        Objects.equals(this.slug, tripDTO.slug);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, slug);
  }

  @Override
  public String toString() {
    return "TripDTO{" +
        "id=" + id +
        ", location='" + location + '\'' +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        ", capacity=" + capacity +
        ", currentBookings=" + currentBookings +
        ", status=" + status +
        ", slug='" + slug + '\'' +
        '}';
  }
}
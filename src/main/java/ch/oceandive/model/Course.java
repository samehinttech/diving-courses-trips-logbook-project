package ch.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Each course represents a certification training program.
 */
@Entity
@Table(name = "courses")
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "Course name is required")
  private String name;

  @Column(nullable = false, length = 1000)
  @NotBlank(message = "Course description is required")
  private String description;

  // Short description for homepage cards
  @Column(name = "short_description", length = 300)
  private String shortDescription;

  @Column(nullable = false)
  @NotNull(message = "Start date is required")
  private LocalDate startDate;

  @Column(nullable = false)
  @NotNull(message = "End date is required")
  private LocalDate endDate;

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  private String imageUrl;

  @Column(nullable = false)
  @NotNull(message = "Capacity is required")
  @Min(value = 1, message = "Capacity must be at least 1")
  private Integer capacity;

  @Column(name = "current_bookings", nullable = false)
  private Integer currentBookings;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DiveCertification minCertificationRequired;

  @Column(name = "price", precision = 10, scale = 2)
  @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private CourseStatus status;

  @Column(name = "featured", nullable = false)
  private Boolean featured = false;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder = 0;

  @Column(name = "slug", unique = true)
  private String slug;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  // Constructors
  public Course() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.currentBookings = 0;
    this.status = CourseStatus.PUBLISHED;
    this.featured = false;
    this.displayOrder = 0;
  }

  public Course(String name, String description, LocalDate startDate, LocalDate endDate,
      String imageUrl, Integer capacity, DiveCertification minCertificationRequired,
      BigDecimal price) {
    this();
    this.name = name;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.imageUrl = imageUrl;
    this.capacity = capacity;
    this.minCertificationRequired = minCertificationRequired;
    this.price = price;
    this.slug = generateSlug(name);
  }

  // NEW: Generate slug from course name
  public String generateSlug(String name) {
    if (name == null) return null;
    return name.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }

  // NEW: Update slug when name changes
  @PreUpdate
  @PrePersist
  public void updateTimestampAndSlug() {
    this.updatedAt = LocalDateTime.now();
    if (this.slug == null || this.slug.isEmpty()) {
      this.slug = generateSlug(this.name);
    }
    // Ensure short description exists
    if (this.shortDescription == null || this.shortDescription.isEmpty()) {
      this.shortDescription = this.description != null && this.description.length() > 150
          ? this.description.substring(0, 150) + "..."
          : this.description;
    }
  }

  // Enhanced helper methods
  public Integer getDuration() {
    if (startDate == null || endDate == null) {
      return 0;
    }
    return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
  }

  public void setDuration(Integer duration) {
    if (startDate != null && duration != null && duration > 0) {
      this.endDate = startDate.plusDays(duration - 1);
    }
  }

  public boolean isFullyBooked() {
    return currentBookings >= capacity;
  }

  public void incrementBookings() {
    if (currentBookings < capacity) {
      currentBookings++;
    }
  }

  public void decrementBookings() {
    if (currentBookings > 0) {
      currentBookings--;
    }
  }

  // Get difficulty level based on required certification
  public String getDifficultyLevel() {
    if (minCertificationRequired == null || minCertificationRequired == DiveCertification.NON_DIVER) {
      return "BEGINNER";
    } else if (minCertificationRequired.isAdvanced()) {
      return "ADVANCED";
    } else {
      return "INTERMEDIATE";
    }
  }
  //Check if course is available for new bookings
  public boolean isAvailableForBooking() {
    return status == CourseStatus.PUBLISHED &&
        !isFullyBooked() &&
        startDate.isAfter(LocalDate.now());
  }

  //Get availability status for display
  public String getAvailabilityStatus() {
    if (status != CourseStatus.PUBLISHED) {
      return status.getDisplayName();
    }
    if (isFullyBooked()) {
      return "FULL";
    }
    if (startDate.isBefore(LocalDate.now())) {
      return "PAST";
    }
    return "AVAILABLE";
  }

  //Get the certification this course provides
  public DiveCertification getProvidedCertification() {
    return DiveCertification.fromString(this.name);
  }

  // Check if this is a certification course
  public boolean isCertificationCourse() {
    return getProvidedCertification() != null;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) {
    this.name = name;
    this.slug = generateSlug(name);
  }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getShortDescription() { return shortDescription; }
  public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public String getImageUrl() { return imageUrl; }
  public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

  public Integer getCapacity() { return capacity; }
  public void setCapacity(Integer capacity) { this.capacity = capacity; }

  public Integer getCurrentBookings() { return currentBookings; }
  public void setCurrentBookings(Integer currentBookings) { this.currentBookings = currentBookings; }

  public DiveCertification getMinCertificationRequired() { return minCertificationRequired; }
  public void setMinCertificationRequired(DiveCertification minCertificationRequired) {
    this.minCertificationRequired = minCertificationRequired;
  }


  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }

  public CourseStatus getStatus() { return status; }
  public void setStatus(CourseStatus status) { this.status = status; }

  public Boolean getFeatured() { return featured; }
  public void setFeatured(Boolean featured) { this.featured = featured; }

  public Integer getDisplayOrder() { return displayOrder; }
  public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

  public String getSlug() { return slug; }
  public void setSlug(String slug) { this.slug = slug; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

  @Override
  public String toString() {
    return "Course{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", status=" + status +
        ", featured=" + featured +
        ", price=" + price +
        ", capacity=" + capacity +
        ", currentBookings=" + currentBookings +
        '}';
  }

}


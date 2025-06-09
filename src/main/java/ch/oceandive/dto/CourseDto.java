package ch.oceandive.dto;

import ch.oceandive.model.CourseStatus;
import ch.oceandive.model.DiveCertification;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO for {@link ch.oceandive.model.Course}
 */
public class CourseDto implements Serializable {

  private final Long id;
  @NotBlank(message = "Course name is required")
  private final String name;
  @NotBlank(message = "Course description is required")
  private final String description;
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
  private final Integer currentBookings;
  private final DiveCertification minCertificationRequired;
  private final BigDecimal price;
  private final CourseStatus status;
  private final Boolean featured;
  private final Integer displayOrder;
  private final String slug;
  private final LocalDateTime updatedAt;

  public CourseDto(Long id, String name, String description, String shortDescription,
      LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, String imageUrl,
      Integer capacity, Integer currentBookings,DiveCertification minCertificationRequired,
      BigDecimal price, CourseStatus status,Boolean featured, Integer displayOrder, String slug,
      LocalDateTime updatedAt) {
    this.id = id;
    this.name = name;
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

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
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

  public CourseStatus getStatus() {
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CourseDto entity = (CourseDto) o;
    return Objects.equals(this.id, entity.id) &&
        Objects.equals(this.name, entity.name) &&
        Objects.equals(this.description, entity.description) &&
        Objects.equals(this.shortDescription, entity.shortDescription) &&
        Objects.equals(this.startDate, entity.startDate) &&
        Objects.equals(this.endDate, entity.endDate) &&
        Objects.equals(this.createdAt, entity.createdAt) &&
        Objects.equals(this.imageUrl, entity.imageUrl) &&
        Objects.equals(this.capacity, entity.capacity) &&
        Objects.equals(this.currentBookings, entity.currentBookings) &&
        Objects.equals(this.minCertificationRequired, entity.minCertificationRequired) &&
        Objects.equals(this.price, entity.price) &&
        Objects.equals(this.status, entity.status) &&
        Objects.equals(this.featured, entity.featured) &&
        Objects.equals(this.displayOrder, entity.displayOrder) &&
        Objects.equals(this.slug, entity.slug) &&
        Objects.equals(this.updatedAt, entity.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, shortDescription, startDate, endDate, createdAt,
        imageUrl, capacity, currentBookings, minCertificationRequired, price,
        status, featured, displayOrder, slug, updatedAt);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" +
        "id = " + id + ", " +
        "name = " + name + ", " +
        "description = " + description + ", " +
        "shortDescription = " + shortDescription + ", " +
        "startDate = " + startDate + ", " +
        "endDate = " + endDate + ", " +
        "createdAt = " + createdAt + ", " +
        "imageUrl = " + imageUrl + ", " +
        "capacity = " + capacity + ", " +
        "currentBookings = " + currentBookings + ", " +
        "minCertificationRequired = " + minCertificationRequired + ", " +
        "price = " + price + ", " +
        "status = " + status + ", " +
        "featured = " + featured + ", " +
        "displayOrder = " + displayOrder + ", " +
        "slug = " + slug + ", " +
        "updatedAt = " + updatedAt + ")";
  }
}
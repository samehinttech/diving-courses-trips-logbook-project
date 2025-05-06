package ch.fhnw.oceandive.dto.admin_side;

import ch.fhnw.oceandive.model.DiveCertification;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * DTO for {@link ch.fhnw.oceandive.model.Course}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseDTO implements Serializable {

  private final Long id;
  @NotBlank(message = "Course title is required")
  private final String courseTitle;
  @NotBlank(message = "Description cannot be blank")
  private final String description;
  @NotNull(message = "Price cannot be null")
  @Positive(message = "Price must be positive")
  private final BigDecimal price;
  @NotNull(message = "Start date is required")
  @FutureOrPresent(message = "Start date must be in the future or present")
  private final LocalDate startDate;
  @NotNull(message = "End date is required")
  @Future(message = "End date must be in the future")
  private final LocalDate endDate;
  @NotNull(message = "Duration is required")
  @Positive(message = "Duration must be positive")
  private final Integer duration;
  @NotNull(message = "Max participants cannot be null")
  @Positive(message = "Max participants must be positive")
  private final Integer maxParticipants;
  @Positive(message = "Spots available must be positive")
  private final Integer spotsAvailable;
  private final DiveCertification requiredCertification;
  private final DiveCertification providedCertification;
  private final DiveCertification awardedCertification;
  private final List<String> includedItems;
  private final boolean isActive;
  private final boolean isDeleted;

  public CourseDTO(Long id, String courseTitle, String description, BigDecimal price,
      LocalDate startDate, LocalDate endDate, Integer duration, Integer maxParticipants,
      Integer spotsAvailable, DiveCertification requiredCertification,
      DiveCertification providedCertification, DiveCertification awardedCertification,
      List<String> includedItems, boolean isActive, boolean isDeleted) {
    this.id = id;
    this.courseTitle = courseTitle;
    this.description = description;
    this.price = price;
    this.startDate = startDate;
    this.endDate = endDate;
    this.duration = duration;
    this.maxParticipants = maxParticipants;
    this.spotsAvailable = spotsAvailable;
    this.requiredCertification = requiredCertification;
    this.providedCertification = providedCertification;
    this.awardedCertification = awardedCertification;
    this.includedItems = includedItems;
    this.isActive = isActive;
    this.isDeleted = isDeleted;
  }

  public Long getId() {
    return id;
  }

  public String getCourseTitle() {
    return courseTitle;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public Integer getDuration() {
    return duration;
  }

  public Integer getMaxParticipants() {
    return maxParticipants;
  }

  public Integer getSpotsAvailable() {
    return spotsAvailable;
  }
    
    private String imageUrl;
    
    public String getImageUrl() {
      return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
    }
    
    // Update constructor to include imageUrl
    public CourseDTO(Long id, String courseTitle, String description, BigDecimal price,
        LocalDate startDate, LocalDate endDate, Integer duration, Integer maxParticipants,
        Integer spotsAvailable, DiveCertification requiredCertification,
        DiveCertification providedCertification, DiveCertification awardedCertification,
        List<String> includedItems, boolean isActive, boolean isDeleted, String imageUrl) {
      this.id = id;
      this.courseTitle = courseTitle;
      this.description = description;
      this.price = price;
      this.startDate = startDate;
      this.endDate = endDate;
      this.duration = duration;
      this.maxParticipants = maxParticipants;
      this.spotsAvailable = spotsAvailable;
      this.requiredCertification = requiredCertification;
      this.providedCertification = providedCertification;
      this.awardedCertification = awardedCertification;
      this.includedItems = includedItems;
      this.isActive = isActive;
      this.isDeleted = isDeleted;
      this.imageUrl = imageUrl;
    }

  public DiveCertification getRequiredCertification() {
    return requiredCertification;
  }

  public DiveCertification getProvidedCertification() {
    return providedCertification;
  }

  public DiveCertification getAwardedCertification() {
    return awardedCertification;
  }

  public List<String> getIncludedItems() {
    return includedItems;
  }

  public boolean getIsActive() {
    return isActive;
  }

  public boolean getIsDeleted() {
    return isDeleted;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CourseDTO entity = (CourseDTO) obj;
    return Objects.equals(this.id, entity.id) &&
        Objects.equals(this.courseTitle, entity.courseTitle) &&
        Objects.equals(this.description, entity.description) &&
        Objects.equals(this.price, entity.price) &&
        Objects.equals(this.startDate, entity.startDate) &&
        Objects.equals(this.endDate, entity.endDate) &&
        Objects.equals(this.duration, entity.duration) &&
        Objects.equals(this.maxParticipants, entity.maxParticipants) &&
        Objects.equals(this.spotsAvailable, entity.spotsAvailable) &&
        Objects.equals(this.requiredCertification, entity.requiredCertification) &&
        Objects.equals(this.providedCertification, entity.providedCertification) &&
        Objects.equals(this.awardedCertification, entity.awardedCertification) &&
        Objects.equals(this.includedItems, entity.includedItems) &&
        Objects.equals(this.isActive, entity.isActive) &&
        Objects.equals(this.isDeleted, entity.isDeleted);
  }
}
package ch.fhnw.oceandive.dto.admin_side;

import ch.fhnw.oceandive.model.DiveCertification;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * DTO for {@link ch.fhnw.oceandive.model.Trip}
 * Contains all trip data for admin operations
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripDTO implements Serializable {

    private final Long id;
    
    @NotBlank(message = "Trip title is required")
    private final String tripTitle;
    
    @NotBlank(message = "Description cannot be blank")
    private final String description;
    
    @NotBlank(message = "Location cannot be blank")
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private final String location;
    
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
    private final BigDecimal duration;
    
    @NotNull(message = "Capacity cannot be null")
    @Positive(message = "Capacity must be positive")
    private final Integer capacity;
    
    @Positive(message = "Available spots must be positive")
    private final Integer availableSpots;
    
    private final DiveCertification requiredCertification;
    private final DiveCertification providedCertification;
    private final List<String> includedItems;
    private final boolean isActive;
    private final boolean isDeleted;
    private String imageUrl;

    // Constructor with all fields except imageUrl
    public TripDTO(Long id, String tripTitle, String description, String location,
                  BigDecimal price, LocalDate startDate, LocalDate endDate,
                  BigDecimal duration, Integer capacity, Integer availableSpots,
                  DiveCertification requiredCertification, DiveCertification providedCertification,
                  List<String> includedItems, boolean isActive, boolean isDeleted) {
        this.id = id;
        this.tripTitle = tripTitle;
        this.description = description;
        this.location = location;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.capacity = capacity;
        this.availableSpots = availableSpots;
        this.requiredCertification = requiredCertification;
        this.providedCertification = providedCertification;
        this.includedItems = includedItems;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
    }

    // Constructor with all fields including imageUrl
    public TripDTO(Long id, String tripTitle, String description, String location,
                  BigDecimal price, LocalDate startDate, LocalDate endDate,
                  BigDecimal duration, Integer capacity, Integer availableSpots,
                  DiveCertification requiredCertification, DiveCertification providedCertification,
                  List<String> includedItems, boolean isActive, boolean isDeleted, String imageUrl) {
        this.id = id;
        this.tripTitle = tripTitle;
        this.description = description;
        this.location = location;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.capacity = capacity;
        this.availableSpots = availableSpots;
        this.requiredCertification = requiredCertification;
        this.providedCertification = providedCertification;
        this.includedItems = includedItems;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
        this.imageUrl = imageUrl;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
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

    public BigDecimal getDuration() {
        return duration;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Integer getAvailableSpots() {
        return availableSpots;
    }

    public DiveCertification getRequiredCertification() {
        return requiredCertification;
    }

    public DiveCertification getProvidedCertification() {
        return providedCertification;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TripDTO entity = (TripDTO) obj;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.tripTitle, entity.tripTitle) &&
                Objects.equals(this.description, entity.description) &&
                Objects.equals(this.location, entity.location) &&
                Objects.equals(this.price, entity.price) &&
                Objects.equals(this.startDate, entity.startDate) &&
                Objects.equals(this.endDate, entity.endDate) &&
                Objects.equals(this.duration, entity.duration) &&
                Objects.equals(this.capacity, entity.capacity) &&
                Objects.equals(this.availableSpots, entity.availableSpots) &&
                Objects.equals(this.requiredCertification, entity.requiredCertification) &&
                Objects.equals(this.providedCertification, entity.providedCertification) &&
                Objects.equals(this.includedItems, entity.includedItems) &&
                Objects.equals(this.isActive, entity.isActive) &&
                Objects.equals(this.isDeleted, entity.isDeleted);
    }
}

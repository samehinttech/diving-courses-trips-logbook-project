package ch.oceandive.model;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trips")
public class Trip {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotBlank(message = "Trip name is required")
  private String tripName;

  @NotBlank(message = "Trip information is required")
  private String tripInfo;

  @NotBlank(message = "Location is required")
  @Size(max = 100)
  private String location;

  @NotNull
  @Positive(message = "Price must be positive")
  private BigDecimal price;

  @NotNull
  @Future(message = "Start date must be in the future")
  private LocalDate startDate;

  @NotNull
  @Future(message = "End date must be in the future")
  private LocalDate endDate;

  @NotNull
  @Positive(message = "Capacity must be positive")
  private Integer capacity;

  @Positive(message = "Spots available must be positive")
  private Integer spotsAvailable;

  @Enumerated(EnumType.STRING)
  private DiveCertification minimumCertification;

  @ElementCollection
  @CollectionTable(name = "trip_inclusions", joinColumns = @JoinColumn(name = "trip_id"))
  @Column(name = "included")
  private Set<String> inclusions = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "trip_requirements", joinColumns = @JoinColumn(name = "trip_id"))
  @Column(name = "requirement")
  private Set<String> requirements = new HashSet<>();

  @Column(nullable = false, updatable = true)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  private boolean isAvailable = true;

  // Default constructor
  public Trip() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }
  // Parameterized constructor
  public Trip(String tripName, String tripInfo, String location, BigDecimal price,
      LocalDate startDate, LocalDate endDate, Integer capacity, Integer spotsAvailable,
      DiveCertification minimumCertification, Set<String> inclusions, Set<String> requirements) {
    this.tripName = tripName;
    this.tripInfo = tripInfo;
    this.location = location;
    this.price = price;
    this.startDate = startDate;
    this.endDate = endDate;
    this.capacity = capacity;
    this.spotsAvailable = spotsAvailable;
    this.minimumCertification = minimumCertification;
    this.inclusions = inclusions;
    this.requirements = requirements;
  }
  // Getters and Setters
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getTripName() {
    return tripName;
  }
  public void setTripName(String tripName) {
    this.tripName = tripName;
  }
  public String getTripInfo() {
    return tripInfo;
  }
  public void setTripInfo(String tripInfo) {
    this.tripInfo = tripInfo;
  }
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
  }
  public BigDecimal getPrice() {
    return price;
  }
  public void setPrice(BigDecimal price) {
    this.price = price;
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
  public Integer getCapacity() {
    return capacity;
  }
  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }
  public Integer getSpotsAvailable() {
    return spotsAvailable;
  }
  public void setSpotsAvailable(Integer spotsAvailable) {
    this.spotsAvailable = spotsAvailable;
  }
  public DiveCertification getMinimumCertification() {
    return minimumCertification;
  }
  public void setMinimumCertification(DiveCertification minimumCertification) {
    this.minimumCertification = minimumCertification;
  }
  public Set<String> getInclusions() {
    return inclusions;
  }
  public void setInclusions(Set<String> inclusions) {
    this.inclusions = inclusions;
  }
  public Set<String> getRequirements() {
    return requirements;
  }
  public void setRequirements(Set<String> requirements) {
    this.requirements = requirements;
  }
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
  public boolean isAvailable() {
    return isAvailable;
  }
  public void setAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable;
  }
  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }
  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
  public boolean cancleBooking() {
    if (this.spotsAvailable > 0) {
      this.spotsAvailable++;
      return true;
    }
    return false;
  }
  public boolean bookTrip() {
    if (this.spotsAvailable > 0) {
      this.spotsAvailable--;
      return true;
    }
    return false;
  }
  public boolean isTripFull() {
    return this.spotsAvailable == 0;
  }
}

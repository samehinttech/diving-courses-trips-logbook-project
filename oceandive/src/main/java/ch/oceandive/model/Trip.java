package com.oceandive.model;

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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trips")
public class Trip {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotBlank
  @Column(length = 100, name = "trip_name")
  private String tripName;

  @NotBlank
  @Column(length = 1000, name = "trip_info")
  private String tripInfo;

  @NotBlank
  @Size(max = 100)
  private String location;

  @NotNull
  @Positive
  private BigDecimal price;

  @NotNull
  @Future
  @Column(name = "start_date")
  private LocalDate startDate;

  @NotNull
  @Future
  @Column(name = "end_date")
  private LocalDate endDate;

  @NotNull
  @Positive
  private Integer capacity;

  @Positive
  @Column(name = "spots_available")
  private Integer spotsAvailable;

  @Enumerated(EnumType.STRING)
  private DiveCertification minimumCertification;

  @ElementCollection
  @CollectionTable(name = "trip_inclusions", joinColumns = @JoinColumn(name = "trip_id"))
  @Column(name = "inclusion")
  private Set<String> inclusions = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "trip_requirements", joinColumns = @JoinColumn(name = "trip_id"))
  @Column(name = "requirement")
  private Set<String> requirements = new HashSet<>();

  private boolean active = true;

  // Default constructor
  public Trip() {
  }

  // Parameterized constructor
  public Trip(String tripName, String tripInfo, BigDecimal price,
      Integer capacity, Integer spotsAvailable,
      DiveCertification minimumCertification, String location, LocalDate startDate,
      LocalDate endDate) {
    this.tripName = tripName;
    this.tripInfo = tripInfo;
    this.price = price;
    this.capacity = capacity;
    this.spotsAvailable = spotsAvailable;
    this.minimumCertification = minimumCertification;
    this.location = location;
    this.startDate = startDate;
    this.endDate = endDate;

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

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  // Helper methods
  public void addInclusion(String inclusion) {
    inclusions.add(inclusion);
  }

  public void removeInclusion(String inclusion) {
    inclusions.remove(inclusion);
  }

  public void addRequirement(String requirement) {
    requirements.add(requirement);
  }

  public void removeRequirement(String requirement) {
    requirements.remove(requirement);
  }

  public boolean hasAvailableSpots() {
    return spotsAvailable > 0;
  }

  public void bookSpot() {
    if (spotsAvailable > 0) {
      spotsAvailable--;
    } else {
      throw new IllegalStateException("No available spots for this trip");
    }
  }

  public void cancelBooking() {
    if (cancelBooking) {
      spotsAvailable++;
    } else {
      spotsAvailable = spotsAvailable;
    }
  }
}

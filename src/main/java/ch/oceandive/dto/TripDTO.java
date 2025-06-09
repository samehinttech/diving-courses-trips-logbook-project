package ch.oceandive.dto;

import ch.fhnw.oceandive.model.DiveCertification;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;



public class TripDTO implements Serializable {

  private final Long id;
  private final String location;
  private final String description;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final Integer duration;
  private final LocalDateTime createdAt;
  private final String imageUrl;
  private final Integer capacity;
  private final Integer currentBookings;
  private final DiveCertification minCertificationRequired;

  public TripDTO(Long id, String location, String description, LocalDate startDate, LocalDate endDate,
      Integer duration, LocalDateTime createdAt, String imageUrl, Integer capacity,
      Integer currentBookings, DiveCertification minCertificationRequired) {
    this.id = id;
    this.location = location;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.duration = duration;
    this.createdAt = createdAt;
    this.imageUrl = imageUrl;
    this.capacity = capacity;
    this.currentBookings = currentBookings;
    this.minCertificationRequired = minCertificationRequired;
  }

  public Long getId() {
    return id;
  }

  public String getLocation() {
    return location;
  }

  public String getDescription() {
    return description;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TripDTO entity = (TripDTO) o;
    return Objects.equals(this.id, entity.id) &&
        Objects.equals(this.location, entity.location) &&
        Objects.equals(this.description, entity.description) &&
        Objects.equals(this.startDate, entity.startDate) &&
        Objects.equals(this.endDate, entity.endDate) &&
        Objects.equals(this.duration, entity.duration) &&
        Objects.equals(this.createdAt, entity.createdAt) &&
        Objects.equals(this.imageUrl, entity.imageUrl) &&
        Objects.equals(this.capacity, entity.capacity) &&
        Objects.equals(this.currentBookings, entity.currentBookings) &&
        Objects.equals(this.minCertificationRequired, entity.minCertificationRequired);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, location, description, startDate, endDate, duration, createdAt, imageUrl,
        capacity, currentBookings, minCertificationRequired);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" +
        "id = " + id + ", " +
        "location = " + location + ", " +
        "description = " + description + ", " +
        "startDate = " + startDate + ", " +
        "endDate = " + endDate + ", " +
        "duration = " + duration + ", " +
        "createdAt = " + createdAt + ", " +
        "imageUrl = " + imageUrl + ", " +
        "capacity = " + capacity + ", " +
        "currentBookings = " + currentBookings + ", " +
        "minCertificationRequired = " + minCertificationRequired + ")";
  }
}
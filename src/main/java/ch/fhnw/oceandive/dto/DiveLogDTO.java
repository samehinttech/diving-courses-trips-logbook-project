package ch.fhnw.oceandive.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO for {@link ch.fhnw.oceandive.model.DiveLog}
 */
public class DiveLogDTO implements Serializable {

  private final Long id;
  private final Integer diveNumber;
  private final String location;
  private final LocalDateTime startTime;
  private final LocalDateTime endTime;
  private final Integer duration;
  private final Double waterTemperature;
  private final Double airTemperature;
  private final String notes;
  private final LocalDate diveDate;

  public DiveLogDTO(Long id, Integer diveNumber, String location, LocalDateTime startTime,
      LocalDateTime endTime, Integer duration, Double waterTemperature, Double airTemperature,
      String notes, LocalDate diveDate) {
    this.id = id;
    this.diveNumber = diveNumber;
    this.location = location;
    this.startTime = startTime;
    this.endTime = endTime;
    this.duration = duration;
    this.waterTemperature = waterTemperature;
    this.airTemperature = airTemperature;
    this.notes = notes;
    this.diveDate = diveDate;
  }

  // Constructor without diveDate for backward compatibility
  public DiveLogDTO(Long id, Integer diveNumber, String location, LocalDateTime startTime,
      LocalDateTime endTime, Integer duration, Double waterTemperature, Double airTemperature,
      String notes) {
    this(id, diveNumber, location, startTime, endTime, duration, waterTemperature, airTemperature, 
        notes, startTime != null ? startTime.toLocalDate() : null);
  }

  public Long getId() {
    return id;
  }

  public Integer getDiveNumber() {
    return diveNumber;
  }

  public String getLocation() {
    return location;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public Integer getDuration() {
    return duration;
  }

  public Double getWaterTemperature() {
    return waterTemperature;
  }

  public Double getAirTemperature() {
    return airTemperature;
  }

  public String getNotes() {
    return notes;
  }

  public LocalDate getDiveDate() {
    return diveDate;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    DiveLogDTO entity = (DiveLogDTO) obj;
    return Objects.equals(this.id, entity.id) &&
        Objects.equals(this.diveNumber, entity.diveNumber) &&
        Objects.equals(this.location, entity.location) &&
        Objects.equals(this.startTime, entity.startTime) &&
        Objects.equals(this.endTime, entity.endTime) &&
        Objects.equals(this.duration, entity.duration) &&
        Objects.equals(this.waterTemperature, entity.waterTemperature) &&
        Objects.equals(this.airTemperature, entity.airTemperature) &&
        Objects.equals(this.notes, entity.notes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, diveNumber, location, startTime, endTime, duration, waterTemperature,
        airTemperature, notes);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" +
        "id = " + id + ", " +
        "diveNumber = " + diveNumber + ", " +
        "location = " + location + ", " +
        "startTime = " + startTime + ", " +
        "endTime = " + endTime + ", " +
        "duration = " + duration + ", " +
        "waterTemperature = " + waterTemperature + ", " +
        "airTemperature = " + airTemperature + ", " +
        "notes = " + notes + ")";
  }
}

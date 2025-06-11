package ch.oceandive.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.io.Serializable;
/**
 * Data Transfer Object for DiveLog
 */
public class DiveLogDTO implements Serializable{

  private Long id;

  @NotNull(message = "Dive number is required")
  @Min(value = 1, message = "Dive number must be positive")
  private Integer diveNumber;

  @NotBlank(message = "Location is required")
  @Size(max = 255, message = "Location must be less than 255 characters")
  private String location;

  @NotNull(message = "Start time is required")
  private LocalTime startTime;

  @NotNull(message = "End time is required")
  private LocalTime endTime;

  private Integer duration;

  @DecimalMin(value = "-5.0", message = "Water temperature must be realistic (minimum -5°C)")
  @DecimalMax(value = "40.0", message = "Water temperature must be realistic (maximum 40°C)")
  private Double waterTemperature;

  @DecimalMin(value = "-20.0", message = "Air temperature must be realistic (minimum -20°C)")
  @DecimalMax(value = "50.0", message = "Air temperature must be realistic (maximum 50°C)")
  private Double airTemperature;

  @Size(max = 2000, message = "Notes must be less than 2000 characters")
  private String notes;

  @NotNull(message = "Dive date is required")
  private LocalDate diveDate;

  // Constructors
  public DiveLogDTO() {
  }

  public DiveLogDTO(Long id, Integer diveNumber, String location, LocalTime startTime,
      LocalTime endTime, Integer duration, Double waterTemperature,
      Double airTemperature, String notes, LocalDate diveDate) {
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

  // Builder pattern for convenience
  public static DiveLogDTOBuilder builder() {
    return new DiveLogDTOBuilder();
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getDiveNumber() {
    return diveNumber;
  }

  public void setDiveNumber(Integer diveNumber) {
    this.diveNumber = diveNumber;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public Double getWaterTemperature() {
    return waterTemperature;
  }

  public void setWaterTemperature(Double waterTemperature) {
    this.waterTemperature = waterTemperature;
  }

  public Double getAirTemperature() {
    return airTemperature;
  }

  public void setAirTemperature(Double airTemperature) {
    this.airTemperature = airTemperature;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public LocalDate getDiveDate() {
    return diveDate;
  }

  public void setDiveDate(LocalDate diveDate) {
    this.diveDate = diveDate;
  }

  // Builder class
  public static class DiveLogDTOBuilder {

    private Long id;
    private Integer diveNumber;
    private String location;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer duration;
    private Double waterTemperature;
    private Double airTemperature;
    private String notes;
    private LocalDate diveDate;

    public DiveLogDTOBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public DiveLogDTOBuilder diveNumber(Integer diveNumber) {
      this.diveNumber = diveNumber;
      return this;
    }

    public DiveLogDTOBuilder location(String location) {
      this.location = location;
      return this;
    }

    public DiveLogDTOBuilder startTime(LocalTime startTime) {
      this.startTime = startTime;
      return this;
    }

    public DiveLogDTOBuilder endTime(LocalTime endTime) {
      this.endTime = endTime;
      return this;
    }

    public DiveLogDTOBuilder duration(Integer duration) {
      this.duration = duration;
      return this;
    }

    public DiveLogDTOBuilder waterTemperature(Double waterTemperature) {
      this.waterTemperature = waterTemperature;
      return this;
    }

    public DiveLogDTOBuilder airTemperature(Double airTemperature) {
      this.airTemperature = airTemperature;
      return this;
    }

    public DiveLogDTOBuilder notes(String notes) {
      this.notes = notes;
      return this;
    }

    public DiveLogDTOBuilder diveDate(LocalDate diveDate) {
      this.diveDate = diveDate;
      return this;
    }

    public DiveLogDTO build() {
      return new DiveLogDTO(id, diveNumber, location, startTime, endTime,
          duration, waterTemperature, airTemperature, notes, diveDate);
    }
  }
}
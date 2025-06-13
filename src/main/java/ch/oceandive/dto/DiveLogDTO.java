package ch.oceandive.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;

/**
 * Data Transfer Object for DiveLog with enhanced helper methods
 */
public class DiveLogDTO implements Serializable {

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

  // ===== HELPER METHODS FOR DISPLAY =====

  /**
   * Get formatted duration for display
   */
  public String getFormattedDuration() {
    if (duration == null || duration <= 0) {
      return "0 min";
    }

    int hours = duration / 60;
    int minutes = duration % 60;

    if (hours > 0) {
      return String.format("%dh %02dm", hours, minutes);
    } else {
      return String.format("%d min", minutes);
    }
  }

  /**
   * Get formatted start time for display
   */
  public String getFormattedStartTime() {
    if (startTime == null) {
      return "Not recorded";
    }
    try {
      return startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    } catch (Exception e) {
      return "Invalid time";
    }
  }

  /**
   * Get formatted end time for display
   */
  public String getFormattedEndTime() {
    if (endTime == null) {
      return "Not recorded";
    }
    try {
      return endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    } catch (Exception e) {
      return "Invalid time";
    }
  }

  /**
   * Get formatted dive date for display
   */
  public String getFormattedDate() {
    if (diveDate == null) {
      return "Not recorded";
    }
    return diveDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
  }

  /**
   * Get temperature display string
   */
  public String getTemperatureDisplay() {
    if (waterTemperature != null && airTemperature != null) {
      return String.format("Water: %.1f°C, Air: %.1f°C", waterTemperature, airTemperature);
    } else if (waterTemperature != null) {
      return String.format("Water: %.1f°C", waterTemperature);
    } else if (airTemperature != null) {
      return String.format("Air: %.1f°C", airTemperature);
    } else {
      return "No temperature data";
    }
  }

  /**
   * Get abbreviated notes for table display
   */
  public String getAbbreviatedNotes(int maxLength) {
    if (notes == null || notes.trim().isEmpty()) {
      return "No notes";
    }

    String trimmedNotes = notes.trim();
    if (trimmedNotes.length() <= maxLength) {
      return trimmedNotes;
    }

    return trimmedNotes.substring(0, maxLength - 3) + "...";
  }

  /**
   * Check if this dive has notes
   */
  public boolean hasNotes() {
    return notes != null && !notes.trim().isEmpty();
  }

  /**
   * Check if this dive has temperature data
   */
  public boolean hasTemperatureData() {
    return waterTemperature != null || airTemperature != null;
  }

  /**
   * Get dive depth category based on duration (rough estimate)
   */
  public String getDiveCategory() {
    if (duration == null) {
      return "Unknown";
    }

    if (duration < 30) {
      return "Short dive";
    } else if (duration < 60) {
      return "Standard dive";
    } else {
      return "Long dive";
    }
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

  // toString, equals, hashCode
  @Override
  public String toString() {
    return "DiveLogDTO{" +
        "id=" + id +
        ", diveNumber=" + diveNumber +
        ", location='" + location + '\'' +
        ", diveDate=" + diveDate +
        ", duration=" + duration +
        '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof DiveLogDTO that)) return false;
    return java.util.Objects.equals(id, that.id) &&
        java.util.Objects.equals(diveNumber, that.diveNumber);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, diveNumber);
  }
}
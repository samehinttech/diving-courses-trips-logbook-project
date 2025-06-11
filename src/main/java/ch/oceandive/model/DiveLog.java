package ch.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "dive_logs")
public class DiveLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Dive number is required")
  @Min(value = 1, message = "Dive number must be positive")
  @Column(nullable = false)
  private Integer diveNumber;

  @NotBlank(message = "Location is required")
  @Size(max = 255, message = "Location must be less than 255 characters")
  @Column(nullable = false)
  private String location;

  @NotNull(message = "Start time is required")
  @Column(nullable = false)
  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime startTime;

  @NotNull(message = "End time is required")
  @Column(nullable = false)
  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime endTime;

  @NotNull(message = "Dive date is required")
  @Column(name = "dive_date", nullable = false)
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate diveDate;

  @NotNull(message = "Duration is required")
  @Min(value = 1, message = "Duration must be positive")
  @Column(nullable = false)
  private Integer duration;

  @DecimalMin(value = "-5.0", message = "Water temperature must be realistic (minimum -5°C)")
  @DecimalMax(value = "40.0", message = "Water temperature must be realistic (maximum 40°C)")
  private Double waterTemperature;

  @DecimalMin(value = "-20.0", message = "Air temperature must be realistic (minimum -20°C)")
  @DecimalMax(value = "50.0", message = "Air temperature must be realistic (maximum 50°C)")
  private Double airTemperature;

  @Size(max = 2000, message = "Notes must be less than 2000 characters")
  @Column(length = 2000)
  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private PremiumUser user;

  // Default constructor
  public DiveLog() {
  }

  // Constructor with essential fields
  public DiveLog(Integer diveNumber, String location, LocalTime startTime,
      LocalTime endTime, LocalDate diveDate, Integer duration) {
    this.diveNumber = diveNumber;
    this.location = location;
    this.startTime = startTime;
    this.endTime = endTime;
    this.diveDate = diveDate;
    this.duration = duration;
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

  public LocalDate getDiveDate() {
    return diveDate;
  }

  public void setDiveDate(LocalDate diveDate) {
    this.diveDate = diveDate;
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

  public PremiumUser getUser() {
    return user;
  }

  public void setUser(PremiumUser user) {
    this.user = user;
  }

  // Business logic methods
  public String getFormattedDuration() {
    if (duration == null) {
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

  // toString method
  @Override
  public String toString() {
    return "DiveLog{" +
        "id=" + id +
        ", diveNumber=" + diveNumber +
        ", location='" + location + '\'' +
        ", diveDate=" + diveDate +
        ", duration=" + duration +
        '}';
  }

  // equals and hashCode based on (user + diveNumber)
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DiveLog diveLog)) {
      return false;
    }
    return user != null && diveNumber != null &&
        user.equals(diveLog.user) && diveNumber.equals(diveLog.diveNumber);
  }

  @Override
  public int hashCode() {
    return 31 * (user != null ? user.hashCode() : 0) +
        (diveNumber != null ? diveNumber.hashCode() : 0);
  }
}
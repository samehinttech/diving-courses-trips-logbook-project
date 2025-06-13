package ch.oceandive.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "dive_logs")
public class DiveLog {

  @Id
  @JsonIgnore
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
  @Column(nullable = false, columnDefinition = "TIME(6)") // Database storing time in microseconds
  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime startTime;

  @NotNull(message = "End time is required")
  @Column(nullable = false, columnDefinition = "TIME(6)")
  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime endTime;

  @NotNull(message = "Dive date is required")
  @Column(name = "dive_date", nullable = false)
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate diveDate;

  // Remove @NotNull and @Min validation - duration can be calculated/null
  @Column(nullable = true)
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
    this.startTime = normalizeTime(startTime);
    this.endTime = normalizeTime(endTime);
    this.diveDate = diveDate;
    this.duration = duration;
  }

  // Helper method to normalize time (remove microseconds) After all pains with time missmatch
  private LocalTime normalizeTime(LocalTime time) {
    if (time == null) {
      return null;
    }
    try {
      return time.truncatedTo(ChronoUnit.SECONDS);
    } catch (Exception e) {
      // Fallback: create new LocalTime with just hours, minutes, seconds
      return LocalTime.of(time.getHour(), time.getMinute(), time.getSecond());
    }
  }

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
    return normalizeTime(startTime);
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = normalizeTime(startTime);
  }

  public LocalTime getEndTime() {
    return normalizeTime(endTime);
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = normalizeTime(endTime);
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

  public String getFormattedStartTime() {
    LocalTime time = getStartTime();
    return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : "--:--";
  }

  public String getFormattedEndTime() {
    LocalTime time = getEndTime();
    return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : "--:--";
  }

  // toString method
  @Override
  public String toString() {
    return "DiveLog{" +
        "id=" + id +
        ", diveNumber=" + diveNumber +
        ", location='" + location + '\'' +
        ", diveDate=" + diveDate +
        ", startTime=" + getFormattedStartTime() +
        ", endTime=" + getFormattedEndTime() +
        ", duration=" + duration +
        '}';
  }

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
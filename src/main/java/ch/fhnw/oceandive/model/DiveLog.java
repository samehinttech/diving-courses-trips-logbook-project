package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entity representing a dive log created by a registered user. Only users with accounts can create and
 * manage dive logs.
 */
@Entity
@Table(name = "dive_logs")
public class DiveLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Integer diveNumber;

  @Column(nullable = false)
  private String location;

  @Column(nullable = false)
  private LocalDateTime startTime;

  @Column(nullable = false)
  private LocalDateTime endTime;

  @Column(name = "DIVE_DATE", nullable = false)
  private LocalDate diveDate;

  @Column(nullable = false)
  private Integer duration;

  private Double waterTemperature;

  private Double airTemperature;

  @Column(length = 2000)
  private String notes;


  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private PremiumUser premiumUser;

  // Default constructor
  public DiveLog() {
  }

  //parameterized constructor
  public DiveLog(Integer diveNumber, String location, LocalDateTime startTime,
      LocalDateTime endTime,
      Double waterTemperature, Double airTemperature, String notes, PremiumUser premiumUser) {
    this.diveNumber = diveNumber;
    this.location = location;
    this.startTime = startTime;
    this.endTime = endTime;
    this.duration = calculateDuration(startTime, endTime);
    this.waterTemperature = waterTemperature;
    this.airTemperature = airTemperature;
    this.notes = notes;
    this.premiumUser = premiumUser;
    // Set diveDate from startTime
    this.diveDate = startTime != null ? startTime.toLocalDate() : null;
  }

  // Helper method to calculate duration in minutes
  private Integer calculateDuration(LocalDateTime start, LocalDateTime end) {
    return (int) Duration.between(start, end).toMinutes();
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

  public LocalDateTime getStartTime() {
    return startTime;
  }

  /**
   * Sets the start time of the dive log and calculates the duration if end time is already set.
   */
  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
    if (this.endTime != null) {
      this.duration = calculateDuration(startTime, this.endTime);
    }
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  /**
   * Sets the end time of the dive log and calculates the duration if start time is already set.
   */
  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
    if (this.startTime != null) {
      this.duration = calculateDuration(this.startTime, endTime);
    }
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

  public PremiumUser getPremiumUser() {
    return premiumUser;
  }
  public void setPremiumUser(PremiumUser premiumUser) {
    this.premiumUser = premiumUser;
  }

  public LocalDate getDiveDate() {
    return diveDate;
  }

  public void setDiveDate(LocalDate diveDate) {
    this.diveDate = diveDate;
  }
}

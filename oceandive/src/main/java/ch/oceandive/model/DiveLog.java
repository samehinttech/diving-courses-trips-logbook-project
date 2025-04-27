package ch.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dive Log Entity.
 */


@Entity
@Table(name = "dive_logs")
public class DiveLog {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotNull
  @Positive
  private Integer diveNumber;

  @NotNull
  @PastOrPresent
  private LocalDate diveDate;

  @NotNull
  @Size(max = 100)
  private String diveLocation;

  private Float airTemperature;

  private Float surfaceTemperature;

  @NotNull
  private LocalTime startTime;

  @NotNull
  private LocalTime endTime;

  @NotNull
  @Positive
  private Float maxDepth;

  @Size(max = 1000)
  @Column(length = 1000)
  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  // Default constructor
  public DiveLog() {
  }

  // Parameterized constructor
  public DiveLog(Integer diveNumber, LocalDate diveDate, String diveLocation,
      Float airTemperature, Float surfaceTemperature,
      LocalTime startTime, LocalTime endTime, Float maxDepth,
      String notes, User user) {
    this.diveNumber = diveNumber;
    this.diveDate = diveDate;
    this.diveLocation = diveLocation;
    this.airTemperature = airTemperature;
    this.surfaceTemperature = surfaceTemperature;
    this.startTime = startTime;
    this.endTime = endTime;
    this.maxDepth = maxDepth;
    this.notes = notes;
    this.user = user;
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

  public LocalDate getDiveDate() {
    return diveDate;
  }

  public void setDiveDate(LocalDate diveDate) {
    this.diveDate = diveDate;
  }

  public String getDiveLocation() {
    return diveLocation;
  }

  public void setDiveLocation(String diveLocation) {
    this.diveLocation = diveLocation;
  }

  public Float getAirTemperature() {
    return airTemperature;
  }

  public void setAirTemperature(Float airTemperature) {
    this.airTemperature = airTemperature;
  }

  public Float getSurfaceTemperature() {
    return surfaceTemperature;
  }

  public void setSurfaceTemperature(Float surfaceTemperature) {
    this.surfaceTemperature = surfaceTemperature;
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

  public Float getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(Float maxDepth) {
    this.maxDepth = maxDepth;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}

package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Table(name = "dive_logs",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dive_number", "user_id"})
    },
    indexes = {
        @Index(name = "idx_dive_number", columnList = "dive_number"),
        @Index(name = "idx_dive_date", columnList = "dive_date")
    })
public class DiveLog {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(nullable = false)
  private Long id;



  @NotNull
  @Positive
  @Column(name = "dive_number", nullable = false)
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
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  public DiveLog() {
  }
  public DiveLog(Integer diveNumber, LocalDate diveDate, String diveLocation, Float airTemperature,
      Float surfaceTemperature, LocalTime startTime, LocalTime endTime, Float maxDepth, String notes) {
    this.diveNumber = diveNumber;
    this.diveDate = diveDate;
    this.diveLocation = diveLocation;
    this.airTemperature = airTemperature;
    this.surfaceTemperature = surfaceTemperature;
    this.startTime = startTime;
    this.endTime = endTime;
    this.maxDepth = maxDepth;
    this.notes = notes;
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
    if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("End time must be after start time");
    }
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
  public UserEntity getUser() {
    return user;
  }
  public void setUser(UserEntity user) {
    this.user = user;
  }
}

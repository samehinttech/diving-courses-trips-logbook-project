package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "course_title", "user_id"})
    },
    indexes = {
        @Index(name = "idx_course_tile", columnList = "course_title"),
        @Index(name = "idx_course_id", columnList = "course_id"),
        @Index(name = "idx_course_start_date", columnList = "start_date")
    }
)
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "course_id", nullable = false)
  private Long id;

  @NotBlank
  @Column(name = "course_title", nullable = false)
  private String courseTitle;

  @NotBlank
  @Size(max = 1000)
  private String description;


  @NotBlank
  @Size(max = 100)
  private String location;

  @NotNull
  @Positive
  private BigDecimal price;

  @NotNull
  @FutureOrPresent
  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;


  @NotNull
  @Future
  private LocalDate endDate;

  @NotNull
  @Positive
  private Integer duration;

  @NotNull
  @Positive
  private Integer maxParticipants;

  @NotNull
  @Positive
  private Integer capacity;

  @Positive
  private Integer spotsAvailable;

  @Enumerated(EnumType.STRING)
  @Column(name = "prerequisite")
  private DiveCertification coursePrerequisite;
  
  @Enumerated(EnumType.STRING)
  private DiveCertification providedCertification;
  
  
  @ElementCollection
  @CollectionTable(name = "included",
      joinColumns = @JoinColumn(name = "course_id"))
  @Column(name= "prerequisite")
  private List<String> prerequisite  = new ArrayList<>();

  @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Booking> bookings = new ArrayList<>();


  private boolean isActive = true;
  private boolean isDeleted = false;

  public Course() {
  }
  public Course(String courseTitle, String description, String location, BigDecimal price,
      LocalDate startDate, LocalDate endDate, Integer duration, Integer maxParticipants,
      Integer capacity, Integer spotsAvailable, DiveCertification coursePrerequisite,
      DiveCertification providedCertification) {
    this.courseTitle = courseTitle;
    this.description = description;
    this.location = location;
    this.price = price;
    this.startDate = startDate;
    this.endDate = endDate;
    this.duration = duration;
    this.maxParticipants = maxParticipants;
    this.capacity = capacity;
    this.spotsAvailable = spotsAvailable;
    this.coursePrerequisite = coursePrerequisite;
    this.providedCertification = providedCertification;
  }
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getCourseTitle() {
    return courseTitle;
  }
  public void setCourseTitle(String courseTitle) {
    this.courseTitle = courseTitle;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
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
  public Integer getDuration() {
    return duration;
  }
  public void setDuration(Integer duration) {
    this.duration = duration;
  }
  public Integer getMaxParticipants() {
    return maxParticipants;
  }
  public void setMaxParticipants(Integer maxParticipants) {
    this.maxParticipants = maxParticipants;
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
  public DiveCertification getCoursePrerequisite() {
    return coursePrerequisite;
  }
  public void setCoursePrerequisite(DiveCertification coursePrerequisite) {
    this.coursePrerequisite = coursePrerequisite;
  }
  public DiveCertification getProvidedCertification() {
    return providedCertification;
  }
  public void setProvidedCertification(DiveCertification providedCertification) {
    this.providedCertification = providedCertification;
  }
  public List<String> getPrerequisite() {
    return prerequisite;
  }
  public void setPrerequisite(List<String> prerequisite) {
    this.prerequisite = prerequisite;
  }
  public boolean isActive() {
    return isActive;
  }
  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
  public boolean isDeleted() {
    return isDeleted;
  }
  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }
}

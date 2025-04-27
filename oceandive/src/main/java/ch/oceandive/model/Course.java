package ch.oceandive.model;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;


/**
 * Represents a diving course that users can book.
 */

  @Entity
  @Table(name = "courses")
  public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Course name is required")
  private String courseName;

  @NotBlank(message = "Course information is required")
  @Size(max = 1000, message = "Course information must be less than 1000 characters")
  private String courseInfo;

  @NotNull(message = "Course price is required")
  @Positive
  @Digits(integer = 10, fraction = 2, message = "Course price should be a positive number with up to 2 decimal places")
  private BigDecimal coursePrice;

  @NotNull(message = "Course duration in days is required")
  @Positive(message = "Course duration in days must be positive")
  private Integer courseDurationDays;

  @Enumerated(EnumType.STRING)
  @NotNull(message = "prerequisite field is required")
  private DiveCertification requiredCertification;

  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private DiveCertification providedCertification;

  @ElementCollection
  @CollectionTable(name = "course_inclusions", joinColumns = @JoinColumn(name = "course_id"))
  @Column(name = "included")
  private Set<String> inclusions = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "course_requirements", joinColumns = @JoinColumn(name = "course_id"))
  @Column(name = "requirement")
  private Set<String> requirements = new HashSet<>();

  private boolean active;

  // Constructors (default and parameterized)
  public Course() {
  }
  public Course(String courseName, String courseInfo, BigDecimal coursePrice,
      Integer courseDurationDays,
      DiveCertification requiredCertification, DiveCertification providedCertification,
      Set<String> inclusions, Set<String> requirements) {
    this.courseName = courseName;
    this.courseInfo = courseInfo;
    this.coursePrice = coursePrice;
    this.courseDurationDays = courseDurationDays;
    this.requiredCertification = requiredCertification;
    this.providedCertification = providedCertification;
    this.inclusions = inclusions;
    this.requirements = requirements;
  }
  // Getters and Setters
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getCourseName() {
    return courseName;
  }
  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }
  public String getCourseInfo() {
    return courseInfo;
  }

  public void setCourseInfo(String courseInfo) {
    this.courseInfo = courseInfo;
  }
  public BigDecimal getCoursePrice() {
    return coursePrice;
  }
  public void setCoursePrice(BigDecimal coursePrice) {
    this.coursePrice = coursePrice;
  }
  public Integer getCourseDurationDays() {
    return courseDurationDays;
  }
  public void setCourseDurationDays(Integer courseDurationDays) {
    this.courseDurationDays = courseDurationDays;
  }
  public DiveCertification getRequiredCertification() {
    return requiredCertification;
  }
  public void setRequiredCertification(DiveCertification requiredCertification) {
    this.requiredCertification = requiredCertification;
  }
  public DiveCertification getProvidedCertification() {
    return providedCertification;
  }
  public void setProvidedCertification(DiveCertification providedCertification) {
    this.providedCertification = providedCertification;
  }
  public Set<String> getInclusions() {
    return inclusions;
  }
  public void setInclusions(Set<String> inclusions) {
    this.inclusions = inclusions;
  }
  public Set<String> getRequirements() {
    return requirements;
  }
  public void setRequirements(Set<String> requirements) {
    this.requirements = requirements;
  }
  public boolean isActive() {
    return active;
  }
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Heloper method to add/remove a requirement to the course if needed.
   */
  public void addRequirement(String requirement) {
    this.requirements.add(requirement);
}
  public void removeRequirement(String requirement) {
    this.requirements.remove(requirement);
  }

  /**
   * Helper method to add/remove an inclusion or what is included for course if needed.
   */

  public void addInclusions(Set<String> inclusions) {
    this.inclusions.addAll(inclusions);
  }
  public void removeInclusions(Set<String> inclusions) {
    this.inclusions.removeAll(inclusions);
  }
  public void addRequirements(Set<String> requirements) {
    this.requirements.addAll(requirements);
  }
  public void removeRequirements(Set<String> requirements) {
    this.requirements.removeAll(requirements);
  }
}
package com.oceandive.model;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
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
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotBlank
  @Size(max = 100)
  @Column(name = "course_name")
  private String courseName;

  @NotBlank
  @Size(max = 1000)
  @Column(name = "course_info", length = 1000)
  private String courseInfo;

  @NotNull
  @Positive
  @Column(name = "course_price")
  private BigDecimal coursePrice;

  @NotNull
  @Positive
  @Column(name = "course_duration_days")
  private Integer courseDurationDays;

  @Enumerated(EnumType.STRING)
  @Column(name = "required_certification")
  private DiveCertification requiredCertification;

  @Enumerated(EnumType.STRING)
  @Column(name = "provided_certification")
  private DiveCertification providedCertification;

  @ElementCollection
  @CollectionTable(name = "course_inclusions", joinColumns = @JoinColumn(name = "course_id"))
  @Column(name = "inclusion")
  private Set<String> inclusions = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "course_requirements", joinColumns = @JoinColumn(name = "course_id"))
  @Column(name = "requirement")
  private Set<String> requirements = new HashSet<>();// I chose to use HashSet to ensure that no duplicate elements are stored and better performance

  private boolean active = true;

  // Default constructor
  public Course() {
  }

  // Parameterized constructor
  public Course(String courseName, String courseInfo, BigDecimal coursePrice,
      Integer courseDurationDays,
      DiveCertification requiredCertification, DiveCertification providedCertification) {
    this.courseName = courseName;
    this.courseInfo = courseInfo;
    this.coursePrice = coursePrice;
    this.courseDurationDays = courseDurationDays;
    this.requiredCertification = requiredCertification;
    this.providedCertification = providedCertification;
  }
  // Getters and Setters


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Set<String> getRequirements() {
    return requirements;
  }

  public void setRequirements(Set<String> requirements) {
    this.requirements = requirements;
  }

  public Set<String> getInclusions() {
    return inclusions;
  }

  public void setInclusions(Set<String> inclusions) {
    this.inclusions = inclusions;
  }

  public DiveCertification getProvidedCertification() {
    return providedCertification;
  }

  public void setProvidedCertification(DiveCertification providedCertification) {
    this.providedCertification = providedCertification;
  }

  public DiveCertification getRequiredCertification() {
    return requiredCertification;
  }

  public void setRequiredCertification(DiveCertification requiredCertification) {
    this.requiredCertification = requiredCertification;
  }

  public Integer getCourseDurationDays() {
    return courseDurationDays;
  }

  public void setCourseDurationDays(Integer courseDurationDays) {
    this.courseDurationDays = courseDurationDays;
  }

  public BigDecimal getCoursePrice() {
    return coursePrice;
  }

  public void setCoursePrice(BigDecimal coursePrice) {
    this.coursePrice = coursePrice;
  }

  public String getCourseInfo() {
    return courseInfo;
  }

  public void setCourseInfo(String courseInfo) {
    this.courseInfo = courseInfo;
  }

  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

// Helper methods if needed later
  public void addInclusion(String inclusion) {
    this.inclusions.add(inclusion);
  }
  public void removeInclusion(String inclusion) {
    this.inclusions.remove(inclusion);
  }
  public void addRequirement(String requirement) {
    this.requirements.add(requirement);
  }
  public void removeRequirement(String requirement) {
    this.requirements.remove(requirement);
  }
}
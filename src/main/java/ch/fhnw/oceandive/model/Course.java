package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
  @Column(columnDefinition = "TEXT")
  private String description;

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

  @Positive
  private Integer spotsAvailable;

  @Enumerated(EnumType.STRING)
  private DiveCertification requiredCertification;
  
  @Enumerated(EnumType.STRING)
  private DiveCertification providedCertification;

  @Enumerated(EnumType.STRING)
  private DiveCertification awardedCertification;

  @ElementCollection
  @CollectionTable(
      name = "course_included_items", // Updated table name for clarity
      joinColumns = @JoinColumn(name = "course_id")
  )
  @Column(name = "items") // Updated column name for clarity
  private List<String> includedItems = new ArrayList<>();

  @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Booking> bookings = new ArrayList<>();

  private boolean isActive = true;
  private boolean isDeleted = false;

  @Column(name = "image_url")
  private String imageUrl;
  
  public Course() {
  }

  public Course(Long id, String courseTitle, String description, BigDecimal price,
      LocalDate startDate, LocalDate endDate, Integer duration, Integer maxParticipants,
      Integer spotsAvailable, DiveCertification requiredCertification,
      DiveCertification providedCertification, DiveCertification awardedCertification) {
    this.id = id;
    this.courseTitle = courseTitle;
    this.description = description;
    this.price = price;
    this.startDate = startDate;
    this.endDate = endDate;
    this.duration = duration;
    this.maxParticipants = maxParticipants;
    this.spotsAvailable = spotsAvailable;
    this.requiredCertification = requiredCertification;
    this.providedCertification = providedCertification;
    this.awardedCertification = awardedCertification;
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
  public Integer getSpotsAvailable() {
    return spotsAvailable;
  }
  public void setSpotsAvailable(Integer spotsAvailable) {
    this.spotsAvailable = spotsAvailable;
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
  public DiveCertification getAwardedCertification() {
    return awardedCertification;
  }
  public void setAwardedCertification(DiveCertification awardedCertification) {
    this.awardedCertification = awardedCertification;
  }
  public List<Booking> getBookings() {
    return bookings;
  }
  public void setBookings(List<Booking> bookings) {
    this.bookings = bookings;
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
  public List<String> getIncludedItems() {
    return includedItems;
  }
  public void setIncludedItems(List<String> includedItems) {
    this.includedItems = includedItems;
  }
  public void addBooking(Booking booking) {
    this.bookings.add(booking);
    booking.setCourse(this);
  }
  public void removeBooking(Booking booking) {
    this.bookings.remove(booking);
    booking.setCourse(null);
  }
  
  public String getImageUrl() {
    return imageUrl;
  }
  
  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}

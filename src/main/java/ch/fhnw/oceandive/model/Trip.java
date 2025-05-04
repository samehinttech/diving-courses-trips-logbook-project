package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"trip_id", "trip_title", "user_id"})
    },
    indexes = {
        @Index(name = "idx_trip_title", columnList = "trip_title"),
        @Index(name = "idx_trip_id", columnList = "trip_id"),
        @Index(name = "idx_trip_start_date", columnList = "start_date")
    })
public class Trip {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "trip_id", nullable = false)
  private Long id;

  @NotBlank
  @Column(name = "trip_title", nullable = false)
  private String tripTitle;

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
  private BigDecimal duration;


  @NotNull
  @Positive
  private Integer capacity;

  @NotNull
  @Positive
  private Integer availableSpots;

  @Enumerated(EnumType.STRING)
  @Column(name  = "requirments")
  private DiveCertification requiredCertification;

  @Enumerated(EnumType.STRING)
  private DiveCertification providedCertification;

  @ElementCollection
  @CollectionTable(name = "included", joinColumns = @JoinColumn(name = "trip_id"))
  @Column(name = "included")
  private List<String> includedItems = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "requirements", joinColumns = @JoinColumn(name = "trip_id"))
  @Column(name = "required")
  private List<String> requirement = new ArrayList<>();


  @OneToMany(mappedBy = "trip", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Booking> bookings = new ArrayList<>();
  public Trip() {
  }

  public Trip(Long id, String tripTitle, String description, String location, BigDecimal price,
      LocalDate startDate, LocalDate endDate, BigDecimal duration, Integer capacity,
      Integer availableSpots, DiveCertification requiredCertification, List<String> includedItems) {
    this.id = id;
    this.tripTitle = tripTitle;
    this.description = description;
    this.location = location;
    this.price = price;
    this.startDate = startDate;
    this.endDate = endDate;
    this.duration = duration;
    this.capacity = capacity;
    this.availableSpots = availableSpots;
    this.requiredCertification = requiredCertification;
    this.includedItems = includedItems;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTripTitle() {
    return tripTitle;
  }

  public void setTripTitle(String tripTitle) {
    this.tripTitle = tripTitle;
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

  public BigDecimal getDuration() {
    return duration;
  }

  public void setDuration(BigDecimal duration) {
    this.duration = duration;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  public Integer getAvailableSpots() {
    return availableSpots;
  }

  public void setAvailableSpots(Integer availableSpots) {
    this.availableSpots = availableSpots;
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

  public List<String> getIncludedItems() {
    return includedItems;
  }

  public void setIncludedItems(List<String> includedItems) {
    this.includedItems = includedItems;
  }

  public List<String> getRequirement() {
    return requirement;
  }

  public void setRequirement(List<String> requirement) {
    this.requirement = requirement;
  }

  public List<Booking> getBookings() {
    return bookings;
  }

  public void setBookings(List<Booking> bookings) {
    this.bookings = bookings;
  }
}

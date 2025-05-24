package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Entity representing a diving course offered by the system.
 * Courses can be booked by users and customers.
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String imageUrl;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer currentBookings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiveCertification minCertificationRequired;

    // Default constructor
    public Course() {
        this.createdAt = LocalDateTime.now();
        this.currentBookings = 0;
    }

    //Parameterized constructor
    public Course(String name, String description, LocalDate startDate, LocalDate endDate, 
                 String imageUrl, Integer capacity, 
                 DiveCertification minCertificationRequired) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.capacity = capacity;
        this.currentBookings = 0;
        this.minCertificationRequired = minCertificationRequired;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    /**
     * Calculate the duration in days between startDate and endDate
     * @return the duration in days
     */
    public Integer getDuration() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1; // +1 to include both start and end days
    }

    /**
     * Set the duration by adjusting the endDate based on the startDate
     * @param duration the duration in days
     */
    public void setDuration(Integer duration) {
        if (startDate != null && duration != null && duration > 0) {
            this.endDate = startDate.plusDays(duration - 1); // -1 because the duration includes the start day
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCurrentBookings() {
        return currentBookings;
    }

    public void setCurrentBookings(Integer currentBookings) {
        this.currentBookings = currentBookings;
    }

    public DiveCertification getMinCertificationRequired() {
        return minCertificationRequired;
    }

    public void setMinCertificationRequired(DiveCertification minCertificationRequired) {
        this.minCertificationRequired = minCertificationRequired;
    }

    // Helper method to check if the course is fully booked
    public boolean isFullyBooked() {
        return currentBookings >= capacity;
    }

    // Helper method to increment bookings
    public void incrementBookings() {
        if (currentBookings < capacity) {
            currentBookings++;
        }
    }
}

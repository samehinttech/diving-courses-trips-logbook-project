package ch.fhnw.oceandive.dto;

import ch.fhnw.oceandive.model.DiveCertification;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO for public bookings (no authentication required)
 * Used when guests book trips or courses without creating an account
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicBookingDTO implements Serializable {

    private Long tripId;
    private Long courseId;
    private Integer numberOfBookings;
    
    // Contact information for guest bookings
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private DiveCertification diveCertification;

    // Default constructor
    public PublicBookingDTO() {
    }

    // Constructor with all fields
    public PublicBookingDTO(Long tripId, Long courseId, Integer numberOfBookings,
                           String firstName, String lastName, String email, 
                           String phoneNumber, DiveCertification diveCertification) {
        this.tripId = tripId;
        this.courseId = courseId;
        this.numberOfBookings = numberOfBookings;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.diveCertification = diveCertification;
    }

    // Getters and setters
    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getNumberOfBookings() {
        return numberOfBookings;
    }

    public void setNumberOfBookings(Integer numberOfBookings) {
        this.numberOfBookings = numberOfBookings;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public DiveCertification getDiveCertification() {
        return diveCertification;
    }

    public void setDiveCertification(DiveCertification diveCertification) {
        this.diveCertification = diveCertification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicBookingDTO that = (PublicBookingDTO) o;
        return Objects.equals(tripId, that.tripId) &&
                Objects.equals(courseId, that.courseId) &&
                Objects.equals(numberOfBookings, that.numberOfBookings) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(email, that.email) &&
                Objects.equals(phoneNumber, that.phoneNumber) &&
                diveCertification == that.diveCertification;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, courseId, numberOfBookings, firstName, lastName, email, phoneNumber, diveCertification);
    }
}
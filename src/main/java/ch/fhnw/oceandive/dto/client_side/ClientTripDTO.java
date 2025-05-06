package ch.fhnw.oceandive.dto.client_side;

import ch.fhnw.oceandive.model.DiveCertification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Trip entity with only public-facing information.
 * This is used for client-side interactions to limit exposure of sensitive data.
 */
public class PublicTripDTO {
    private Long id;
    private String tripTitle;
    private String description;
    private String location;
    private BigDecimal price;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal duration;
    private Integer availableSpots;
    private DiveCertification requiredCertification;
    private List<String> includedItems = new ArrayList<>();
    private String imageUrl;

    // Default constructor
    public PublicTripDTO() {
    }

    // Constructor with all fields
    public PublicTripDTO(Long id, String tripTitle, String description, String location, 
                        BigDecimal price, LocalDate startDate, LocalDate endDate, 
                        BigDecimal duration, Integer availableSpots, 
                        DiveCertification requiredCertification, List<String> includedItems,
                        String imageUrl) {
        this.id = id;
        this.tripTitle = tripTitle;
        this.description = description;
        this.location = location;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.availableSpots = availableSpots;
        this.requiredCertification = requiredCertification;
        this.includedItems = includedItems;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
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

    public List<String> getIncludedItems() {
        return includedItems;
    }

    public void setIncludedItems(List<String> includedItems) {
        this.includedItems = includedItems;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

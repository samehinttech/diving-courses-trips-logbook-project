package ch.fhnw.oceandive.dto.client_side;

import ch.fhnw.oceandive.model.DiveCertification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Course entity with only public-facing information.
 * This is used for client-side interactions to limit exposure of sensitive data.
 */
public class ClientCourseDTO {
    private Long id;
    private String courseTitle;
    private String description;
    private BigDecimal price;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer duration;
    private Integer spotsAvailable;
    private DiveCertification requiredCertification;
    private DiveCertification awardedCertification;
    private List<String> includedItems = new ArrayList<>();
    private String imageUrl;

    // Default constructor
    public ClientCourseDTO() {
    }

    // Parameterized constructor
    public ClientCourseDTO(Long id, String courseTitle, String description, BigDecimal price,
                      LocalDate startDate, LocalDate endDate, Integer duration,
                      Integer spotsAvailable, DiveCertification requiredCertification,
                      DiveCertification awardedCertification, List<String> includedItems,
                      String imageUrl) {
        this.id = id;
        this.courseTitle = courseTitle;
        this.description = description;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.spotsAvailable = spotsAvailable;
        this.requiredCertification = requiredCertification;
        this.awardedCertification = awardedCertification;
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

    public DiveCertification getAwardedCertification() {
        return awardedCertification;
    }

    public void setAwardedCertification(DiveCertification awardedCertification) {
        this.awardedCertification = awardedCertification;
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

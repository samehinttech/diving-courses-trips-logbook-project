package ch.fhnw.oceandive.dto;

import ch.fhnw.oceandive.model.DiveLog;
import ch.fhnw.oceandive.model.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * DTO for {@link ch.fhnw.oceandive.model.DiveLog}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiveLogDTO implements Serializable {

    private final Long id;
    private final Integer diveNumber;
    private final LocalDate diveDate;
    private final String diveLocation;
    private final Float airTemperature;
    private final Float surfaceTemperature;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Float maxDepth;
    private final String notes;
    private final String userId;

    public DiveLogDTO(Long id, Integer diveNumber, LocalDate diveDate, String diveLocation,
                      Float airTemperature, Float surfaceTemperature, LocalTime startTime,
                      LocalTime endTime, Float maxDepth, String notes, String userId) {
        this.id = id;
        this.diveNumber = diveNumber;
        this.diveDate = diveDate;
        this.diveLocation = diveLocation;
        this.airTemperature = airTemperature;
        this.surfaceTemperature = surfaceTemperature;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxDepth = maxDepth;
        this.notes = notes;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public Integer getDiveNumber() {
        return diveNumber;
    }

    public LocalDate getDiveDate() {
        return diveDate;
    }

    public String getDiveLocation() {
        return diveLocation;
    }

    public Float getAirTemperature() {
        return airTemperature;
    }

    public Float getSurfaceTemperature() {
        return surfaceTemperature;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Float getMaxDepth() {
        return maxDepth;
    }

    public String getNotes() {
        return notes;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        DiveLogDTO entity = (DiveLogDTO) obj;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.diveNumber, entity.diveNumber) &&
                Objects.equals(this.diveDate, entity.diveDate) &&
                Objects.equals(this.diveLocation, entity.diveLocation) &&
                Objects.equals(this.airTemperature, entity.airTemperature) &&
                Objects.equals(this.surfaceTemperature, entity.surfaceTemperature) &&
                Objects.equals(this.startTime, entity.startTime) &&
                Objects.equals(this.endTime, entity.endTime) &&
                Objects.equals(this.maxDepth, entity.maxDepth) &&
                Objects.equals(this.notes, entity.notes) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, diveNumber, diveDate, diveLocation, airTemperature, 
                surfaceTemperature, startTime, endTime, maxDepth, notes, userId);
    }

    // Mapper methods to convert DiveLog to DiveLogDTO and vice versa
    public static DiveLogDTO fromEntity(DiveLog diveLog) {
        return new DiveLogDTO(
                diveLog.getId(),
                diveLog.getDiveNumber(),
                diveLog.getDiveDate(),
                diveLog.getDiveLocation(),
                diveLog.getAirTemperature(),
                diveLog.getSurfaceTemperature(),
                diveLog.getStartTime(),
                diveLog.getEndTime(),
                diveLog.getMaxDepth(),
                diveLog.getNotes(),
                diveLog.getUser() != null ? diveLog.getUser().getId().toString() : null
        );
    }

    public static DiveLog toEntity(DiveLogDTO diveLogDTO, UserEntity user) {
        DiveLog diveLog = new DiveLog(
                diveLogDTO.getDiveNumber(),
                diveLogDTO.getDiveDate(),
                diveLogDTO.getDiveLocation(),
                diveLogDTO.getAirTemperature(),
                diveLogDTO.getSurfaceTemperature(),
                diveLogDTO.getStartTime(),
                diveLogDTO.getEndTime(),
                diveLogDTO.getMaxDepth(),
                diveLogDTO.getNotes()
        );
        diveLog.setId(diveLogDTO.getId());
        diveLog.setUser(user);
        return diveLog;
    }
}

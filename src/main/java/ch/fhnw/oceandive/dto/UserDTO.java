package ch.fhnw.oceandive.dto;

import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.model.Role.RoleName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO for {@link ch.fhnw.oceandive.model.UserEntity}
 * Supports both admin and client views with conditional field visibility
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

  private final String id;
  @Size(max = 70)
  @NotBlank
  private final String firstName;
  @Size(max = 70)
  @NotBlank
  private final String lastName;
  @Size(max = 100)
  @Email
  @NotBlank
  private final String email;
  @Size(max = 50)
  @NotBlank
  private final String username;
  @Size(min = 8, max = 100)
  @NotBlank
  private final String password;
  private final DiveCertification diveCertification;
  private final LocalDateTime issuedOn;
  private final LocalDateTime modifiedOn;
  private final RoleName userType;
  private final Integer bookingsCount;
  private final boolean isAdmin; // Controls field visibility

  /**
   * Admin constructor - includes all fields
   */
  public UserDTO(String id, String firstName, String lastName, String email, String username,
      String password, DiveCertification diveCertification, LocalDateTime issuedOn,
      LocalDateTime modifiedOn, RoleName userType, Integer bookingsCount) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.username = username;
    this.password = password;
    this.diveCertification = diveCertification;
    this.issuedOn = issuedOn;
    this.modifiedOn = modifiedOn;
    this.userType = userType;
    this.bookingsCount = bookingsCount;
    this.isAdmin = true;
  }
  
  /**
   * Client constructor - includes only client-visible fields
   * Sets restricted fields to null
   */
  public UserDTO(String firstName, String lastName, String username,
      DiveCertification diveCertification) {
    this.id = null;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = null;
    this.username = username;
    this.password = null;
    this.diveCertification = diveCertification;
    this.issuedOn = null;
    this.modifiedOn = null;
    this.userType = null;
    this.bookingsCount = null;
    this.isAdmin = false;
  }

  @JsonIgnore
  public boolean isAdmin() {
    return isAdmin;
  }

  @JsonProperty
  public String getId() {
    return isAdmin ? id : null;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  @JsonProperty
  public String getEmail() {
    return isAdmin ? email : null;
  }

  public String getUsername() {
    return username;
  }

  @JsonIgnore
  public String getPassword() {
    return isAdmin ? password : null;
  }

  public DiveCertification getDiveCertification() {
    return diveCertification;
  }

  @JsonProperty
  public LocalDateTime getIssuedOn() {
    return isAdmin ? issuedOn : null;
  }

  @JsonProperty
  public LocalDateTime getModifiedOn() {
    return isAdmin ? modifiedOn : null;
  }

  @JsonProperty
  public RoleName getUserType() {
    return isAdmin ? userType : null;
  }

  @JsonProperty
  public Integer getBookingsCount() {
    return isAdmin ? bookingsCount : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserDTO entity = (UserDTO) o;
    return isAdmin == entity.isAdmin &&
        Objects.equals(this.id, entity.id) &&
        Objects.equals(this.firstName, entity.firstName) &&
        Objects.equals(this.lastName, entity.lastName) &&
        Objects.equals(this.email, entity.email) &&
        Objects.equals(this.username, entity.username) &&
        Objects.equals(this.password, entity.password) &&
        Objects.equals(this.diveCertification, entity.diveCertification) &&
        Objects.equals(this.issuedOn, entity.issuedOn) &&
        Objects.equals(this.modifiedOn, entity.modifiedOn) &&
        Objects.equals(this.userType, entity.userType) &&
        Objects.equals(this.bookingsCount, entity.bookingsCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, email, username, password, diveCertification,
        issuedOn, modifiedOn, userType, bookingsCount, isAdmin);
  }
}
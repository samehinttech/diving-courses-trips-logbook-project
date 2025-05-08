package ch.fhnw.oceandive.dto;

import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.model.Role.RoleName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO for {@link ch.fhnw.oceandive.model.UserEntity}
 * Supports both admin and client views with conditional field visibility
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO implements Serializable {

  private String id;
  @Size(max = 70)
  @NotBlank
  private String firstName;
  @Size(max = 70)
  @NotBlank
  private String lastName;
  @Size(max = 100)
  @Email
  @NotBlank
  private String email;
  @Size(max = 50)
  @NotBlank
  private String username;
  @Size(min = 8, max = 100)
  @NotBlank
  private String password;
  private DiveCertification diveCertification;
  private LocalDateTime issuedOn;
  private LocalDateTime modifiedOn;
  private RoleName userType;
  private Integer bookingsCount;
  private boolean isAdmin = false; // Controls field visibility

  /**
   * Default constructor for JSON deserialization
   */
  public UserDTO() {
  }

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

  public void setAdmin(boolean admin) {
    isAdmin = admin;
  }

  @JsonProperty
  public String getId() {
    return isAdmin ? id : null;
  }

  public void setId(String id) {
    this.id = id;
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

  @JsonProperty
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public DiveCertification getDiveCertification() {
    return diveCertification;
  }

  public void setDiveCertification(DiveCertification diveCertification) {
    this.diveCertification = diveCertification;
  }

  @JsonProperty
  public LocalDateTime getIssuedOn() {
    return isAdmin ? issuedOn : null;
  }

  public void setIssuedOn(LocalDateTime issuedOn) {
    this.issuedOn = issuedOn;
  }

  @JsonProperty
  public LocalDateTime getModifiedOn() {
    return isAdmin ? modifiedOn : null;
  }

  public void setModifiedOn(LocalDateTime modifiedOn) {
    this.modifiedOn = modifiedOn;
  }

  @JsonProperty
  public RoleName getUserType() {
    return isAdmin ? userType : null;
  }

  public void setUserType(RoleName userType) {
    this.userType = userType;
  }

  @JsonProperty
  public Integer getBookingsCount() {
    return isAdmin ? bookingsCount : null;
  }

  public void setBookingsCount(Integer bookingsCount) {
    this.bookingsCount = bookingsCount;
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

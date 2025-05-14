package ch.fhnw.oceandive.dto;

import ch.fhnw.oceandive.model.DiveCertification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;


public class PremiumUserDTO implements Serializable {

  private final Long id;
  @NotEmpty(message = "Please enter your first name")
  private final String firstName;
  @NotEmpty(message = "Please enter your last name")
  private final String lastName;
  @Email(message = "Please enter a valid email address")
  @NotEmpty(message = "Please enter your email")
  private final String email;
  private final String mobile;
  private final DiveCertification diveCertification;
  private final String username;
  @NotEmpty(message = "Please enter a password")
  private final String password;
  private final String role;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  public PremiumUserDTO(Long id, String firstName, String lastName, String email, String mobile,
      DiveCertification diveCertification, String username, String password, String role,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.mobile = mobile;
    this.diveCertification = diveCertification;
    this.username = username;
    this.password = password;
    this.role = role;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getMobile() {
    return mobile;
  }

  public DiveCertification getDiveCertification() {
    return diveCertification;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getRole() {
    return role;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    PremiumUserDTO entity = (PremiumUserDTO) o;
    return Objects.equals(this.id, entity.id) &&
        Objects.equals(this.firstName, entity.firstName) &&
        Objects.equals(this.lastName, entity.lastName) &&
        Objects.equals(this.email, entity.email) &&
        Objects.equals(this.mobile, entity.mobile) &&
        Objects.equals(this.diveCertification, entity.diveCertification) &&
        Objects.equals(this.username, entity.username) &&
        Objects.equals(this.password, entity.password) &&
        Objects.equals(this.role, entity.role) &&
        Objects.equals(this.createdAt, entity.createdAt) &&
        Objects.equals(this.updatedAt, entity.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, email, mobile, diveCertification, username,
        password, role, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" +
        "id = " + id + ", " +
        "firstName = " + firstName + ", " +
        "lastName = " + lastName + ", " +
        "email = " + email + ", " +
        "mobile = " + mobile + ", " +
        "diveCertification = " + diveCertification + ", " +
        "username = " + username + ", " +
        "password = " + password + ", " +
        "role = " + role + ", " +
        "createdAt = " + createdAt + ", " +
        "updatedAt = " + updatedAt + ")";
  }
}
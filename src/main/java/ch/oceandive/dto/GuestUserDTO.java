package ch.oceandive.dto;

import ch.oceandive.utils.DiveCertification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Objects;


public class GuestUserDTO implements Serializable {

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
  private final String role;

  public GuestUserDTO(Long id, String firstName, String lastName, String email, String mobile,
      DiveCertification diveCertification, String role) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.mobile = mobile;
    this.diveCertification = diveCertification;
    this.role = role;
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

  public String getRole() {
    return role;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    GuestUserDTO entity = (GuestUserDTO) obj;
    return Objects.equals(this.id, entity.id) &&
        Objects.equals(this.firstName, entity.firstName) &&
        Objects.equals(this.lastName, entity.lastName) &&
        Objects.equals(this.email, entity.email) &&
        Objects.equals(this.mobile, entity.mobile) &&
        Objects.equals(this.diveCertification, entity.diveCertification) &&
        Objects.equals(this.role, entity.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, email, mobile, diveCertification, role);
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
        "role = " + role + ")";
  }
}
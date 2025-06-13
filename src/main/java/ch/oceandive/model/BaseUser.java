package ch.oceandive.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

/**
 * Base class for all user types in the system. Contains common fields shared by PremiumUser, Admin,
 * and GuestUser.
 */
@JsonIgnoreProperties()
@MappedSuperclass
public abstract class BaseUser {

  @Id
  @Hidden
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @NotEmpty(message = "Please enter your first name")
  @Column(nullable = false)
  protected String firstName;

  @NotEmpty(message = "Please enter your last name")
  @Column(nullable = false)
  protected String lastName;

  @NotEmpty(message = "Please enter your email")
  @Column(nullable = false)
  @Email(message = "Please enter a valid email address")
  protected String email;

  @NotEmpty(message = "Please enter your mobile number")
  protected String mobile;

  @Column(nullable = false)
  @Hidden
  protected String role;

  @JsonIgnore
  @Column(name = "password_reset_token", unique = true)
  protected String passwordResetToken;

  @JsonIgnore
  @Column(name = "password_reset_token_expiry", unique = true)
  protected LocalDateTime passwordResetTokenExpiry;

  // Default constructor
  public BaseUser() {
  }

  // Parameterized constructor
  public BaseUser(String firstName, String lastName, String email, String mobile, String role) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.mobile = mobile;
    this.role = role;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getPasswordResetToken() {
    return passwordResetToken;
  }

  public void setPasswordResetToken(String passwordResetToken) {
    this.passwordResetToken = passwordResetToken;
  }

  public LocalDateTime getPasswordResetTokenExpiry() {
    return passwordResetTokenExpiry;
  }

  public void setPasswordResetTokenExpiry(LocalDateTime passwordResetTokenExpiry) {
    this.passwordResetTokenExpiry = passwordResetTokenExpiry;
  }
  public void cleanupExpiredPasswordResetTokens() {
    // This method can be overridden in subclasses if needed
  }
}

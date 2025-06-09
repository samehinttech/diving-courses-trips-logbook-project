package ch.oceandive.model;

import ch.oceandive.validation.PasswordPattern;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "premium_users", indexes = {
    @Index(name = "idx_premium_user_email", columnList = "email"),
    @Index(name = "idx_premium_user_username", columnList = "username")
})
@AttributeOverrides({
    @AttributeOverride(name = "passwordResetToken", column = @Column(name = "password_reset_token")),
    @AttributeOverride(name = "passwordResetTokenExpiry", column = @Column(name = "password_reset_token_expiry"))
})
public class PremiumUser extends BaseUser implements DiveCertificationHolder {

  @Enumerated(EnumType.STRING)
  private DiveCertification diveCertification;

  @NotEmpty(message = "Please enter a username")
  @Column(nullable = false, unique = true)
  private String username;

  @NotEmpty(message = "Please enter a password")
  @Column(nullable = false)
  @PasswordPattern
  private String password;
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  // Default constructor
  public PremiumUser() {
  }

  // Parameterized constructor
  public PremiumUser(String firstName, String lastName, String email, String mobile,
      DiveCertification diveCertification, String username, String password, String role) {
    super(firstName, lastName, email, mobile, role);
    this.diveCertification = diveCertification;
    this.username = username;
    this.password = password;
  }

  public DiveCertification getDiveCertification() {
    return diveCertification;
  }

  public void setDiveCertification(DiveCertification diveCertification) {
    this.diveCertification = diveCertification;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
  @Override
  public void cleanupExpiredPasswordResetTokens() {
    if (getPasswordResetTokenExpiry() != null && getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
      setPasswordResetToken(null);
      setPasswordResetTokenExpiry(null);
    }
  }
}

package ch.oceandive.model;

import ch.oceandive.utils.DiveCertificationHolder;
import ch.oceandive.utils.DiveCertification;
import ch.oceandive.validation.PasswordPattern;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;
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
  @Hidden
  private String password;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  @JsonIgnore
  private LocalDateTime createdAt;

  @Column(nullable = false)
  @UpdateTimestamp
  @JsonIgnore
  private LocalDateTime updatedAt;

  @OneToMany
      (mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DiveLog> diveLogs;

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

  public List<DiveLog> getDiveLogs() {
    return diveLogs;
  }

  public PremiumUser setDiveLogs(List<DiveLog> diveLogs) {
    this.diveLogs = diveLogs;
    return this;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  // Cleanup expired password reset tokens if exists
  @Override
  public void cleanupExpiredPasswordResetTokens() {
    if (getPasswordResetTokenExpiry() != null && getPasswordResetTokenExpiry().isBefore(
        LocalDateTime.now())) {
      setPasswordResetToken(null);
      setPasswordResetTokenExpiry(null);
    }
  }
}

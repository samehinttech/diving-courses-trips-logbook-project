package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "premium_users", indexes = {
    @Index(name = "idx_premium_user_email", columnList = "email"),
    @Index(name = "idx_premium_user_username", columnList = "username")
})
public class PremiumUser extends BaseUser implements DiveCertificationHolder {

  @Enumerated(EnumType.STRING)
  private DiveCertification diveCertification;

  @NotEmpty(message = "Please enter a username")
  @Column(nullable = false, unique = true)
  private String username;

  @NotEmpty(message = "Please enter a password")
  @Column(nullable = false)
  private String password;
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  // One-to-many relationship with DiveLog
  @OneToMany(mappedBy = "premiumUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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


  public List<DiveLog> getDiveLogs() {
    return diveLogs;
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

  public void setDiveLogs(List<DiveLog> diveLogs) {
    this.diveLogs = diveLogs;
  }

  public DiveLog addDiveLog(DiveLog diveLog) {
    if (this.diveLogs == null) {
      this.diveLogs = new ArrayList<>();
    }
    this.diveLogs.add(diveLog);
    diveLog.setPremiumUser(this);
    return diveLog;
  }

  public DiveLog removeDiveLog(DiveLog diveLog) {
    if (this.diveLogs == null) {
      this.diveLogs = new ArrayList<>();
    }
    this.diveLogs.remove(diveLog);
    diveLog.setPremiumUser(null);
    return diveLog;
  }
}

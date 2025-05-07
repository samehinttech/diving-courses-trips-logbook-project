package ch.fhnw.oceandive.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.*;
import io.swagger.v3.oas.annotations.Hidden;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedBy;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"})
    },
    indexes = @Index(name = "idx_username", columnList = "username")
)
public class UserEntity {

  @Id
  @Column(unique = true, nullable = false)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Hidden
  private String id;

  @NotBlank
  @Size(max = 70)
  private String firstName;

  @NotBlank
  @Size(max = 70)
  private String lastName;

  @Email
  @NotBlank
  @Size(max = 100)
  @Column(name = "email", unique = true)
  private String email;

  @NotBlank
  @Size(max = 50)
  @Column(name = "username", unique = true)
  private String username;

  @NotBlank
  @Size(min = 8, max = 100)
  @Hidden
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;

  @Enumerated(EnumType.STRING)
  private DiveCertification diveCertification;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime issuedOn;

  @UpdateTimestamp
  @Column(insertable = false)
  private LocalDateTime modifiedOn;



  @Enumerated(EnumType.STRING)
  @Column(name = "user_type")
  private Role.RoleName userType = Role.RoleName.ROLE_USER_ACCOUNT; // ROLE_USER_ACCOUNT by default

 @Column(nullable = true)
  private Integer bookingsCount = 0;

  @Transient
  private boolean temporary;

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<DiveLog> diveLogs = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<Booking> bookings = new ArrayList<>();

  @ManyToMany(cascade = {
      CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
  })
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"})
  )
  private List<Role> roles = new ArrayList<>();

  // Default constructor for JPA
  public UserEntity() {}

  public UserEntity(String id, String firstName, String lastName, String email,DiveCertification diveCertification,
      String username, String password, LocalDateTime issuedOn) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.diveCertification = diveCertification;
    this.username = username;
    this.password = password;
    this.issuedOn = issuedOn;
  }

  public void addRole(Role role) {
    if (role == null) {
      throw new IllegalArgumentException("Role cannot be null");
    }
    roles.add(role);
    updateUserType(); // Always update userType when roles change
  }

  public void removeRole(Role role) {
    if (role == null) return;
    if (roles.remove(role)) {
      role.getUsers().remove(this);
    }
    updateUserType(); // Keep userType in sync
  }

  public boolean roleExists(String roleName) {
    return roles.stream()
        .anyMatch(role -> role.getRole().equalsIgnoreCase(Role.DEFAULT_ROLE_PREFIX + roleName));
  }

  private void updateUserType() {
    if (roleExists("ADMIN")) {
      this.userType = Role.RoleName.ROLE_ADMIN;
    } else if (roleExists("GUEST")) {
      this.userType = Role.RoleName.ROLE_GUEST;
    } else {
      this.userType = Role.RoleName.ROLE_USER_ACCOUNT; // Default user type
    }
  }

  public Role.RoleName getUserType() {
    return userType;
  }

  public List<Role> getRoles() {
    return roles;
  }

  public String getId() {
    return id;
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<DiveLog> getDiveLogs() {
    return diveLogs;
  }

  public void setDiveLogs(List<DiveLog> diveLogs) {
    this.diveLogs = diveLogs;
  }

  public boolean isTemporary() {
    return temporary;
  }

  public void setTemporary(boolean temporary) {
    this.temporary = temporary;
  }
  
  public DiveCertification getDiveCertification() {
    return diveCertification;
  }
  
  public void setDiveCertification(DiveCertification diveCertification) {
    this.diveCertification = diveCertification;
  }
  
  public LocalDateTime getIssuedOn() {
    return issuedOn == null ? LocalDateTime.now() : issuedOn;
  }
  
  public LocalDateTime getModifiedOn() {
    return modifiedOn;
  }
  
  public void setIssuedOn(LocalDateTime issuedOn) {
    this.issuedOn = issuedOn;

  }
  
  public void setUserType(Role.RoleName userType) {
    this.userType = userType;
  }
  
  public Integer getBookingsCount() {
    return bookingsCount;
  }
  
  public void setBookingsCount(Integer bookingsCount) {
    this.bookingsCount = bookingsCount;
  }
  
  public List<Booking> getBookings() {
    return bookings;
  }
  
  public void setBookings(List<Booking> bookings) {
    this.bookings = bookings;
  }
  
  public void setRoles(List<Role> roles) {
    this.roles = roles;
    updateUserType(); // Always update userType when roles change
  }
}
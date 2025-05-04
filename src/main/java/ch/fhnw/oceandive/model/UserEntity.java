package ch.fhnw.oceandive.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.*;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDateTime;


@Entity
@Table(name = "users",
uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"})
    },
indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_username", columnList = "username")})
public class UserEntity {

  private static final String DEFAULT_USER_TYPE = "USER";
  private static final int INITIAL_BOOKINGS_COUNT = 0;

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

  @Column(name = "user_type")
  private String userType = DEFAULT_USER_TYPE;

  @NotNull
  private Integer bookingsCount = INITIAL_BOOKINGS_COUNT;

  @Transient
  private boolean temporary;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<DiveLog> diveLogs = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Booking> bookings = new ArrayList<>();

  @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
      CascadeType.REFRESH})
  @JoinTable(name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"}))
  private List<Role> roles = new ArrayList<>();

  // Default constructor for JPA
  public UserEntity() {
  }

  // Main constructor
  public UserEntity(String id, String firstName, String lastName, String email,
      String username, String password) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.username = username;
    this.password = password;
    this.userType = DEFAULT_USER_TYPE;
  }

  // HELPER METHODS THAT IS TIGHT DIRECTLY TO THE USER ENTITY AND CANNOT BE DECOUPLED
  public void addRole(Role role) {
    if (role == null) {
      throw new IllegalArgumentException("Role cannot be null");
    }
    roles.add(role);
  }

  public void removeRole(Role role) {
    if (role == null) return;
    if (roles.remove(role)) {
      role.getUsers().remove(this);
    }
  }

  public void addDiveLog(DiveLog diveLog) {
    diveLogs.add(diveLog);
    diveLog.setUser(this);
  }

  public void removeDiveLog(DiveLog diveLog) {
    diveLogs.remove(diveLog);
    diveLog.setUser(null);
  }

  public boolean isAdmin() {
    return roleExists("ADMIN") || "Admin".equalsIgnoreCase(userType);
  }

  public boolean isUserAccount() {
    return roleExists("USER") || "UserAccount".equalsIgnoreCase(userType);
  }

  public boolean isGuest() {
    return roleExists("GUEST") || "Guest".equalsIgnoreCase(userType);
  }

  public boolean roleExists(String roleName) {
    final String formattedRoleName = !roleName.startsWith(Role.DEFAULT_ROLE_PREFIX) ?
        Role.DEFAULT_ROLE_PREFIX + roleName : roleName;
    return roles.stream().anyMatch(role -> role.getRole().equalsIgnoreCase(formattedRoleName));
  }

  public void incrementBookingsCount() {
    bookingsCount++;
  }

  public void decrementBookingsCount() {
    if (bookingsCount > 0) {
      bookingsCount--;
    }
  }
// getters and setters
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

  public DiveCertification getDiveCertification() {
    return diveCertification;
  }

  public void setDiveCertification(DiveCertification diveCertification) {
    this.diveCertification = diveCertification;
  }

  public LocalDateTime getIssuedOn() {
    return issuedOn;
  }

  public LocalDateTime getModifiedOn() {
    return modifiedOn;
  }

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  public Integer getBookingsCount() {
    return bookingsCount;
  }

  public void setBookingsCount(Integer bookingsCount) {
    this.bookingsCount = bookingsCount;
  }

  public boolean isTemporary() {
    return temporary;
  }

  public void setTemporary(boolean temporary) {
    this.temporary = temporary;
  }

  public List<DiveLog> getDiveLogs() {
    return diveLogs;
  }

  public void setDiveLogs(List<DiveLog> diveLogs) {
    this.diveLogs = diveLogs;
  }

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }
}

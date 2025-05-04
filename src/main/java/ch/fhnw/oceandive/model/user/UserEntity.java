package ch.fhnw.oceandive.model.user;

import ch.fhnw.oceandive.model.activity.DiveLog;
import ch.fhnw.oceandive.model.activity.DiveCertification;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ListIterator;
import org.hibernate.annotations.CreationTimestamp;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "users")
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
  @Column(unique = true)
  private String email;

  @NotBlank
  @Size(max = 50)
  @Column(unique = true)
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
  private LocalDateTime createdOn;

  @CreationTimestamp
  @Column(updatable = true)
  private LocalDateTime updatedOn;

  @Column(name = "user_type")
  private String userType = DEFAULT_USER_TYPE;

  @NotNull
  private Integer bookingsCount = INITIAL_BOOKINGS_COUNT;

  @Transient
  private boolean temporary;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private Set<DiveLog> diveLogs = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"}))
  private Set<Role> roles = new HashSet<>();

  // Default constructor (required by JPA)
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


  // HELPER METHODS, THAT IS DIRECTLY TIGHT TO THE USERS, AND CANNOT BE DECOUPLED
  public void addRole(Role role) {
    roles.add(role);
    role.getUsers().add(this);
  }

  public void removeRole(Role role) {
    roles.remove(role);
    role.getUsers().remove(this);
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
    return roleExists("USER_ACCOUNT") || "UserAccount".equalsIgnoreCase(userType);
  }

  public boolean isGuest() {
    return roleExists("GUEST") || "Guest".equalsIgnoreCase(userType);
  }

  public boolean roleExists(String roleName) {
    return roles.stream().anyMatch(role -> role.getRoleName().equalsIgnoreCase(roleName));
  }

  public void incrementBookingsCount() {
    bookingsCount++;
  }

  public void decrementBookingsCount() {
    if (bookingsCount > 0) {
      bookingsCount--;
    }
  }

  public Set<Role> getRoles() {
    return roles;
  }
}
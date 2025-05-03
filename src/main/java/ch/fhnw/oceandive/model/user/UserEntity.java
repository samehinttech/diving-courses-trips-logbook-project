package ch.fhnw.oceandive.model.user;
import ch.fhnw.oceandive.model.activity.DiveLog;
import ch.fhnw.oceandive.model.activity.DiveCertification;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;



@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Hidden
  private Long id;

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
  @Size(min=8, max = 100)
  @Hidden
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
  private String userType = "USER";

  @NotNull
  private Integer bookingsCount = 0;

  @Transient
  private boolean temporary = false;

  @OneToMany
      (mappedBy = "users", fetch = FetchType.LAZY)
  private Set<DiveLog> diveLogs = new HashSet<>();


  @ManyToMany(fetch = FetchType.EAGER) // EAGER to load roles immediately
  @JoinTable(
      name = "users_roles",
      joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id")}
      , inverseJoinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "id")}
  )
  private Set<Role> roles = new HashSet<>();

  public UserEntity() {
    // Default constructor
  }

  public UserEntity(Long id, String firstName, String lastName, String email, String username,
      String password, DiveCertification diveCertification, LocalDateTime createdOn,
      LocalDateTime updatedOn, String userType, Integer bookingsCount, boolean temporary,
      Set<DiveLog> diveLogs, Set<Role> roles) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.username = username;
    this.password = password;
    this.diveCertification = diveCertification;
    this.createdOn = createdOn;
    this.updatedOn = updatedOn;
    this.userType = userType;
    this.bookingsCount = bookingsCount;
    this.temporary = temporary;
    this.diveLogs = diveLogs;
    this.roles = roles;
  }
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
  public LocalDateTime getCreatedOn() {
    return createdOn;
  }
  public void setCreatedOn(LocalDateTime createdOn) {
    this.createdOn = createdOn;
  }
  public LocalDateTime getUpdatedOn() {
    return updatedOn;
  }
  public void setUpdatedOn(LocalDateTime updatedOn) {
    this.updatedOn = updatedOn;
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
  public Set<DiveLog> getDiveLogs() {
    return diveLogs;
  }
  public void setDiveLogs(Set<DiveLog> diveLogs) {
    this.diveLogs = diveLogs;
  }
  public Set<Role> getRoles() {
    return roles;
  }
  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }
  public void addRole(Role role) {
    this.roles.add(role);
    role.getUsers().add(this);
  }
  public void removeRole(Role role) {
    this.roles.remove(role);
    role.getUsers().remove(this);
  }
  public void addDiveLog(DiveLog diveLog) {
    this.diveLogs.add(diveLog);
  }
  public void removeDiveLog(DiveLog diveLog) {
    this.diveLogs.remove(diveLog);
  }
}

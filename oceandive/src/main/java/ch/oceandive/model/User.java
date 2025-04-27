package ch.oceandive.model;
/**
 * User model class.
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotBlank(message = "First name is required")
  @Column(nullable = false)
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Column(nullable = false)
  private String lastName;


  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Column(unique = true, nullable = false)
  private String email;

  @NotBlank(message = "Username is required")
  @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
  @Column(unique = true, nullable = false)
  private String username;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 50, message = "Password must be between 6 and 50 characters")
  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DiveCertification diveCertification;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "userRoles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role")
  private Set<String> roles = new HashSet<>();


  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<DiveLog> diveLogs = new HashSet<>();

  // Default constructor
  public User() {
  }
  // Parameterized constructor
  public User(String firstName, String lastName, String email, String username, String password,
      DiveCertification diveCertification, Set<String> roles) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.username = username;
    this.password = password;
    this.diveCertification = diveCertification;
    this.roles = roles;
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
  public Set<String> getRoles() {
    return roles;
  }
  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }
  public void addRole(String role) {
    this.roles.add(role);
  }
  public Set<DiveLog> getDiveLogs() {
    return diveLogs;
  }
  public void setDiveLogs(Set<DiveLog> diveLogs) {
    this.diveLogs = diveLogs;
  }
  public void addDiveLog(DiveLog diveLog) {
    this.diveLogs.add(diveLog);
    diveLog.setUser(this);
  }
  public void removeDiveLog(DiveLog diveLog) {
    this.diveLogs.remove(diveLog);
    diveLog.setUser(null);
  }
  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", email='" + email + '\'' +
        ", username='" + username + '\'' +
        ", password='" + password + '\'' +
        ", diveCertification=" + diveCertification +
        ", roles=" + roles +
        '}';
  }








}






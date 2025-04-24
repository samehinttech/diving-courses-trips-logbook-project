package com.oceandive.model;
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

  @NotBlank
  @Size(max = 50)
  @Column(name = "first_name")
  private String firstName;

  @NotBlank
  @Size(max = 50)
  @Column(name = "last_name")
  private String lastName;

  @NotBlank
  @Size(max = 50)
  @Column(unique = true)
  private String username;

  @Email
  @NotBlank
  @Size(max = 100)
  @Column(unique = true)
  private String email;


  @NotBlank
  @Size(max = 100)
  private String password;

  @Enumerated(EnumType.STRING)
  private DiveCertification diveCertification;


  // Each user can have multiple dive logs
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<DiveLog> diveLogs = new HashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role")
  private Set<String> roles = new HashSet<>();

  // Default constructor
  public User() {
  }

  // Parameterized constructor
  public User(String firstName, String lastName, String username, String email, String password, ) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.username = username;
    this.email = email;
    this.password = password;
    this.roles.add("ROLE_USER"); // Default role
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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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

  public Set<DiveLog> getDiveLogs() {
    return diveLogs;
  }

  public void setDiveLogs(Set<DiveLog> diveLogs) {
    this.diveLogs = diveLogs;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  // Helper methods to be removed if not needed later
  public void addDiveLog(DiveLog diveLog) {
    diveLogs.add(diveLog);
    diveLog.setUser(this);
  }

  public void removeDiveLog(DiveLog diveLog) {
    diveLogs.remove(diveLog);
    diveLog.setUser(null);
  }

  public void addRole(String role) {
    roles.add(role);
  }

  public boolean hasRole(String role) {
    return roles.contains(role);
  }
}






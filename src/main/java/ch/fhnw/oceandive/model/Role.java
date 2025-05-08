package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "roles")
public class Role {

  static final String DEFAULT_ROLE_PREFIX = "ROLE_";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private RoleName roleName;


  @ManyToMany(mappedBy = "roles")
  private List<UserEntity> users = new ArrayList<>();


  public Role() { }

  public Role(String role) {
    this.roleName = RoleName.valueOf(formatRoleName(role));
  }
  public enum RoleName {
    ROLE_USER_ACCOUNT,
    ROLE_ADMIN,
    ROLE_GUEST;

    public boolean equals(String obj) {
      if (obj == null) return false;
      return this.name().equalsIgnoreCase(obj);
    }
  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public RoleName getRoleName() {
    return roleName;
  }

  public void setRole(String roleName) {
    if (roleName == null || roleName.isBlank()) {
      throw new IllegalArgumentException("Role name cannot be null or blank");
    }
    this.roleName = RoleName.valueOf(formatRoleName(roleName));
  }

  public List<UserEntity> getUsers() {
    return users;
  }

  public void setUsers(List<UserEntity> user) {
   if (user == null) {
   throw new IllegalArgumentException("User cannot be null");
    }
    this.users = user;
  }

  // HELPER METHOD THAT DIRECTLY TIGHT TO ROLE LOGIC
  public void addUser(UserEntity user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }
    this.users.add(user);
    user.addRole(this);
  }

  public void removeUser(UserEntity user) {
    if (user == null) return;
    if (this.users.remove(user)) {
      user.removeRole(this);
    }
  }

  private String formatRoleName(String roleName) {
    if (!roleName.startsWith(DEFAULT_ROLE_PREFIX)) {
      return DEFAULT_ROLE_PREFIX + roleName.toUpperCase().replace("ROLE_", "");
    }
    return roleName.toUpperCase();
  }
}

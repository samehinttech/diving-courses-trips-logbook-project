package ch.fhnw.oceandive.model.user;

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

  @NotBlank
  @Column(unique = true, nullable = false)
  private String role;

  @ManyToMany(mappedBy = "roles")
  private List<UserEntity> users = new ArrayList<>();

  public Role() {
  }

  public Role(String role) {
    this.role = formatRoleName(role);
  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String roleName) {
    this.role = formatRoleName(roleName);
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

  // Helper Methods
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

package ch.fhnw.oceandive.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "roles")
public class Role {

  private static final String DEFAULT_ROLE_PREFIX = "ROLE_";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotBlank
  @Column(unique = true, nullable = false)
  private String roleName;

  @ManyToMany(mappedBy = "roles")
  private Set<UserEntity> users = new HashSet<>();

  public Role() {}

  public Role(String roleName) {
    this.roleName = formatRoleName(roleName);
  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = formatRoleName(roleName);
  }

  public Set<UserEntity> getUsers() {
    return users;
  }

  public void setUsers(Set<UserEntity> users) {
    this.users = users;
  }

  // Helper Methods
  public void addUser(UserEntity user) {
    users.add(user);
    user.getRoles().add(this);
  }

  public void removeUser(UserEntity user) {
    users.remove(user);
    user.getRoles().remove(this);
  }

  private String formatRoleName(String roleName) {
    // Ensures roles follow a consistent naming convention (e.g., "ROLE_ADMIN")
    if (!roleName.startsWith(DEFAULT_ROLE_PREFIX)) {
      return DEFAULT_ROLE_PREFIX + roleName.toUpperCase().replace("ROLE_", "");
    }
    return roleName.toUpperCase();
  }
}

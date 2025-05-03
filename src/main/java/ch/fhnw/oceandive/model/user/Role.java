package ch.fhnw.oceandive.model.user;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "roles")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String roleName;


  @ManyToMany(mappedBy = "roles")
  private Set<UserEntity> users = new HashSet<>();

  public Set<UserEntity> getUsers() {
    return users;
  }

  public void setUsers(Set<UserEntity> users) {
    this.users = users;
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
    this.roleName = roleName;
  }
  public Role(String roleName) {
    this.roleName = roleName;
  }
  public Role() {
  }
}

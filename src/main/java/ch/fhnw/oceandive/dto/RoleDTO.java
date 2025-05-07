package ch.fhnw.oceandive.dto;

import ch.fhnw.oceandive.model.Role;
import ch.fhnw.oceandive.model.Role.RoleName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO for {@link ch.fhnw.oceandive.model.Role}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleDTO implements Serializable {

  private final Long id;
  private final RoleName roleName;

  public RoleDTO(Long id, RoleName roleName) {
    this.id = id;
    this.roleName = roleName;
  }

  public Long getId() {
    return id;
  }

  public RoleName getRoleName() {
    return roleName;
  }

  @Override
  public boolean equals(Object Obj) {
    if (this == Obj)
      return true;
    if (Obj == null || getClass() != Obj.getClass())
      return false;
    RoleDTO entity = (RoleDTO) Obj;
    return Objects.equals(this.id, entity.id) &&
        Objects.equals(this.roleName, entity.roleName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, roleName);
  }
  // Mapper method to convert Role to RoleDTO and vice versa
  public static RoleDTO fromEntity(Role role) {
    return new RoleDTO(role.getId(), role.getRoleName());
  }
  public static Role toEntity(RoleDTO roleDTO) {
    Role role = new Role();
    role.setId(roleDTO.getId());
    role.setRole(roleDTO.getRoleName().name());
    return role;
  }
}
package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.dto.RoleDTO;
import ch.fhnw.oceandive.model.Role;
import ch.fhnw.oceandive.model.Role.RoleName;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;



@Repository
@Component
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByRoleName(Role.RoleName roleName);
    
    boolean existsByRoleName(Role.RoleName roleName);

    List<Role> findAllByRoleNameIn(List<Role.RoleName> roleNames);












}

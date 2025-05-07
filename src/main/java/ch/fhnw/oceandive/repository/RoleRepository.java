package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByRoleName(Role.RoleName roleName);
    
    boolean existsByRoleName(Role.RoleName roleName);
}

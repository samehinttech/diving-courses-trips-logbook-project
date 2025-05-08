package ch.fhnw.oceandive.service;

import ch.fhnw.oceandive.dto.RoleDTO;
import ch.fhnw.oceandive.model.Role;
import ch.fhnw.oceandive.model.Role.RoleName;
import ch.fhnw.oceandive.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing roles in the application.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    /**
     * Find Role by name.
     */

    @Transactional(readOnly = true)
    public RoleDTO findByRoleName(RoleName roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new EntityNotFoundException(roleName + " does not exist"));
        return RoleDTO.fromEntity(role);
    }

    /**
     * Get all roles.
     */
    @Transactional(readOnly = true)
    public List<RoleDTO> findAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a new role.
     */
    @Transactional
    public RoleDTO createRole(String roleName) {
        if (roleRepository.existsByRoleName(RoleName.valueOf(roleName))) {
            throw new IllegalArgumentException(roleName + " already exists");
        }
        
        Role role = new Role();
        role.setRole(roleName);
        Role savedRole = roleRepository.save(role);
        return RoleDTO.fromEntity(savedRole);
    }

    /**
     * Check if a role exists by name.
     */
   // TODO  Maybe boilerplate code, but let's see
    @Transactional(readOnly = true)
    public boolean roleExists(RoleName roleName) {
        return roleRepository.existsByRoleName(roleName);
    }

    /**
     * Initialize default roles if they don't exist.
     * This can be called during application startup.
     * If it would makes sense
     */
    @Transactional
    public void initializeDefaultRoles() {
        // Create default roles if they don't exist
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role role = new Role();
                role.setRole(roleName.name());
                roleRepository.save(role);
            }
        }
    }

    /**
     * Get a role by ID.
     */
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
        return RoleDTO.fromEntity(role);
    }

    /**
     * Delete a role by ID.
     */
    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new EntityNotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }
}

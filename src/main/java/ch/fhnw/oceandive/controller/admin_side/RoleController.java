package ch.fhnw.oceandive.controller.admin_side;

import ch.fhnw.oceandive.dto.RoleDTO;
import ch.fhnw.oceandive.model.Role.RoleName;
import ch.fhnw.oceandive.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * These endpoints are only accessible to users with the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * GET /api/admin/roles : Get all roles.
     */
    @GetMapping("retrieve-user-roles")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.findAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * GET /api/admin/roles/{id} : Get the role with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * GET /api/admin/roles/name/{roleName} : Get role by name.
     */
    @GetMapping("/name/{roleName}")
    public ResponseEntity<RoleDTO> getRoleByName(@PathVariable String roleName) {
        try {
            RoleName name = RoleName.valueOf(roleName);
            RoleDTO role = roleService.findByRoleName(name);
            return ResponseEntity.ok(role);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/admin/roles : Create a new role.
     */
    @PostMapping
    public ResponseEntity<?> createRole(@Valid @RequestBody Map<String, String> requestBody) {
        String roleName = requestBody.get("roleName");
        if (roleName == null || roleName.isBlank()) {
            return ResponseEntity.badRequest().body("Role name cannot be null or blank");
        }
        
        try {
            RoleDTO createdRole = roleService.createRole(roleName);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/admin/roles/{id} : Delete a role.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/admin/roles/check/{roleName} : Check if a role exists.
     */
    @GetMapping("/check/{roleName}")
    public ResponseEntity<Map<String, Boolean>> checkRoleExists(@PathVariable String roleName) {
        try {
            RoleName name = RoleName.valueOf(roleName);
            boolean exists = roleService.roleExists(name);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
    }

    /**
     * POST /api/admin/roles/initialize : Initialize default roles.
     */
    @PostMapping("/initialize")
    public ResponseEntity<Void> initializeDefaultRoles() {
        roleService.initializeDefaultRoles();
        return ResponseEntity.ok().build();
    }
}

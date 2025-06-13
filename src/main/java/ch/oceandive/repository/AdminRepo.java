package ch.oceandive.repository;

import ch.oceandive.model.Admin;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Long> {

    // Find all admins
    List<Admin> findAllByRole(String role);
    // Find an admin by username
    Admin findByUsername(String username);
    // Find an admin by email
    Admin findByEmail(String email);
    // Find an admin by username or email but not by ID
    List<Admin> findByUsernameAndIdNot(String s, Long aLong);
    // Find an admin by email but not by ID
    List<Admin> findByEmailAndIdNot(String s, Long aLong);

    // Find an admin by id
    List<Admin> findAdminById (Long id);
}
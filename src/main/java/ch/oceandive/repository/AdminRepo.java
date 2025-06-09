package ch.oceandive.repository;

import ch.oceandive.model.Admin;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Long> {

    Admin findByUsername(String username);

    Admin findByEmail(String email);

    List<Admin> findByUsernameAndIdNot(String s, Long aLong);

    List<Admin> findByEmailAndIdNot(String s, Long aLong);
}
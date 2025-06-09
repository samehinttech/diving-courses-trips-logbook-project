package ch.oceandive.repository;

import ch.fhnw.oceandive.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Long> {

    Admin findByUsername(String username);

    Admin findByEmail(String email);

}
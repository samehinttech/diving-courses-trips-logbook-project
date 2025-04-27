package ch.oceandive.repository;

import ch.oceandive.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends  JpaRepository<User, Long> {


  /**
   * Find a user by username.
   * @param username the username to search for
   * @return an Optional containing the user if found, or empty if not found
   */
  Optional<User> findUserByUsername(String username);

  /**
   * Find a user by email.
   * @param email the email to search for
   * @return an Optional containing the user if found, or empty if not found
   */
  Optional<User> findUserByEmail(String email);

  /**
   * Find a user by ID.
   * @param id the ID to search for
   * @return an Optional containing the user if found, or empty if not found
   */
  Optional<User> findUserById(Long id);

  /**
   * Check if a username exists.
   * @param username the username to check
   * @return true if the username exists, false otherwise
   */
  boolean existsUserByUsername(String username);

  /**
   * Check if an email exists.
   * @param email the email to check
   * @return true if the email exists, false otherwise
   */
  boolean existsUserByEmail(String email);
}

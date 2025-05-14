package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.GuestUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestUserRepo extends JpaRepository<GuestUser, Long> {

  /**
   * Find a GuestUser by their mobile number.
   */
  GuestUser findByMobile(String mobile);
  /**
   * Find a GuestUser by their email address.
   */
  GuestUser findByEmail(String email);

  /**
   * Find a GuestUser by their username.
   */
  List<GuestUser> findByIdNot(Long id);
  /**
   * Find a GuestUser by their email address and exclude the one with the given ID.
   */
  List<GuestUser> findByEmailAndIdNot(String email, Long id);


}
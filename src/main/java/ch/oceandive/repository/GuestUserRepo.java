package ch.oceandive.repository;

import ch.oceandive.model.GuestUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestUserRepo extends JpaRepository<GuestUser, Long> {

  // Find a GuestUser by their mobile number.
  GuestUser findByMobile(String mobile);

  // Find a GuestUser by their mobile number and exclude ID.
  List<GuestUser> findByMobileAndIdNot(String mobile, Long id);

 // Find a GuestUser by their email address.
  GuestUser findByEmail(String email);

  // Find a GuestUser by their email address and exclude ID.
  List<GuestUser> findByEmailAndIdNot(String email, Long id);

}

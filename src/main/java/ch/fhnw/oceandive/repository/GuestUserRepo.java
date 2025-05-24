package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.GuestUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface GuestUserRepo extends JpaRepository<GuestUser, Long> {

  // Find a GuestUser by their mobile number.
  GuestUser findByMobile(String mobile);
  // Find a GuestUser by their mobile number and exclude ID.
  List<GuestUser> findByMobileAndIdNot(String mobile, Long id);
 // Find a GuestUser by their email address.
  GuestUser findByEmail(String email);

 // Find a GuestUser by their ID.
  List<GuestUser> findByIdNot(Long id);
 // Find a GuestUser by their email address and exclude ID.
  List<GuestUser> findByEmailAndIdNot(String email, Long id);

}

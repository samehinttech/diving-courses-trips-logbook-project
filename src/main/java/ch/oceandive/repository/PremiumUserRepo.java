package ch.oceandive.repository;

import ch.oceandive.model.PremiumUser;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PremiumUserRepo extends JpaRepository<PremiumUser, Long> {

  // Find a PremiumUser by their username.
  PremiumUser findByUsername(String username);

  // Find a PremiumUser by their email address.
  PremiumUser findByEmail(String email);

  // Find a PremiumUser by email, excluding ID.
  List<PremiumUser> findByEmailAndIdNot(String email, Long id);

  // Find a PremiumUser by their username or email, excluding ID.
  List<PremiumUser> findByUsernameAndIdNot(String username, Long id);


  //Clear expired password reset tokens
  @Transactional
  @Modifying
  @Query("UPDATE PremiumUser u SET u.passwordResetToken = null,"
      + " u.passwordResetTokenExpiry = null WHERE u.passwordResetTokenExpiry <= :now")
  int clearExpiredPasswordResetTokens(LocalDateTime now);

  // Find a PremiumUser by their password reset token.
  PremiumUser findByPasswordResetToken(String token);
}

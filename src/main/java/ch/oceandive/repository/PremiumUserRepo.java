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

  PremiumUser findByUsername(String username);

  PremiumUser findByEmail(String email);

  List<PremiumUser> findAllByCreatedAt(LocalDateTime createdAt);

  List<PremiumUser> findAllByUpdatedAt(LocalDateTime updatedAt);

  List<PremiumUser> findByIdNot(Long id);

  List<PremiumUser> findByEmailAndIdNot(String email, Long id);

  List<PremiumUser> findByUsernameAndIdNot(String username, Long id);

  @Transactional
  @Modifying
  @Query("UPDATE PremiumUser u SET u.passwordResetToken = null,"
      + " u.passwordResetTokenExpiry = null WHERE u.passwordResetTokenExpiry <= :now")
  int clearExpiredPasswordResetTokens(LocalDateTime now);

  PremiumUser findByPasswordResetToken(String token);
}

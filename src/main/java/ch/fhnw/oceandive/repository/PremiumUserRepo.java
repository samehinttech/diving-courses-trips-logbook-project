package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.PremiumUser;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PremiumUserRepo extends JpaRepository<PremiumUser, Long> {

    PremiumUser findByUsername(String username);

    PremiumUser findByEmail(String email);

  List<PremiumUser> findAllByCreatedAt(LocalDateTime createdAt);

  List<PremiumUser> findAllByUpdatedAt(LocalDateTime updatedAt);

  List<PremiumUser> findByIdNot(Long id);

  List<PremiumUser> findByEmailAndIdNot(String email, Long id);

  List<PremiumUser> findByUsernameAndIdNot(String username, Long id);

}
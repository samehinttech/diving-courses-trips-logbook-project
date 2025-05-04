package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.user.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  List<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByEmail(String email);


  Optional<UserEntity> findByFirstName(String firstName);

  Optional<UserEntity> findByLastName(String lastName);

  Optional<UserEntity> findByUsernameAndFirstNameAndLastName
      (String username, String firstName,
          String lastName);

  Optional<UserEntity> findByUsernameAndEmail(String username, String email);

  Optional<UserEntity> findByUsernameAndFirstNameAndEmail
      (String username, String firstName,
          String email);

  List<UserEntity> findUserById(String id);
}

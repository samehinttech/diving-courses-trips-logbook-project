package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.Role;
import ch.fhnw.oceandive.model.UserEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  // Find all users that have a count
  @EntityGraph(attributePaths = {"diveLogs", "bookings"})
  @Query("SELECT u FROM UserEntity u")
  List<UserEntity> findAllUsers();

  // find all admins
  List<UserEntity> findAllByRolesContaining(Role role);
  UserEntity findById(long userId);

  // Find methods by (username, email)
  @EntityGraph(attributePaths = {"diveLogs", "bookings"})
  UserEntity findByUsername(String username);
  UserEntity findByEmail(String email);

  // Existence check methods by (username, email)
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);

  // Delete methods by (username, email)
  void deleteByUsername(String username);
  void deleteUserByEmail(String email);

  // Update methods
  @Modifying
  @Transactional
  @Query("UPDATE UserEntity u SET u.firstName = :#{#user.firstName}, " +
      "u.lastName = :#{#user.lastName}, " +
      "u.email = :#{#user.email}, " +
      "u.username = :#{#user.username}, " +
      "u.password = :#{#user.password}, " +
      "u.diveCertification = :#{#user.diveCertification}, " +
      "u.modifiedOn = CURRENT_TIMESTAMP " +
      "WHERE u.username = :username")
  void updateUserByUsername(@Param("username") String username, @Param("user") UserEntity user);

  @Modifying
  @Transactional
  @Query("UPDATE UserEntity u SET u.firstName = :#{#user.firstName}, " +
      "u.lastName = :#{#user.lastName}, " +
      "u.email = :#{#user.email}, " +
      "u.username = :#{#user.username}, " +
      "u.password = :#{#user.password}, " +
      "u.diveCertification = :#{#user.diveCertification}, " +
      "u.modifiedOn = CURRENT_TIMESTAMP " +
      "WHERE u.email = :email")
  void updateUserByEmail(@Param("email") String email, @Param("user") UserEntity user);

  @Lock(LockModeType.WRITE)
  @Query("SELECT u FROM UserEntity u WHERE u.username = :username")
  Optional<UserEntity> findByUsernameForUpdate(@Param("username") String username);

  @Query("SELECT u FROM UserEntity u WHERE u.userType = :userType OR :userType = 'ROLE_ADMIN'")
  List<UserEntity> fetchUsersByRole(@Param("userType") String userType);





}
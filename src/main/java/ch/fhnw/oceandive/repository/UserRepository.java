package ch.fhnw.oceandive.repository;

import ch.fhnw.oceandive.model.activity.DiveCertification;
import ch.fhnw.oceandive.model.user.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.glassfish.jaxb.core.v2.model.core.ID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findById(String id);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByFirstName(String firstName);
    List<UserEntity> findByLastName(String lastName);
    List<UserEntity> findByDiveCertification(DiveCertification diveCertification);
    long countByRoles_RoleName(String roleName);
    List<UserEntity> findByCreatedOnBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<UserEntity> findByTemporaryTrue();
    void deleteByTemporaryTrue();


}

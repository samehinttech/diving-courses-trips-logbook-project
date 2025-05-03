package ch.fhnw.oceandive.model.activity;

import ch.fhnw.oceandive.model.user.UserEntity;
import jakarta.persistence.*;


@Entity
@Table(name = "DIVE_LOGS")
public class DiveLog {

  @Id
  @GeneratedValue
  private Long id;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;




  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }
  public UserEntity getUser() {
    return user;
  }
  public void setUser(UserEntity user) {
    this.user = user;
  }
}

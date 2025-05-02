package ch.fhnw.oceandive.model.activity;

import jakarta.persistence.*;


@Entity(name = "dive_logs")
public class DiveLog {

  @Id
  @GeneratedValue
  private Long id;

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }
}

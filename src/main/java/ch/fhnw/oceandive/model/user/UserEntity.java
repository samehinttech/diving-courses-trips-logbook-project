package ch.fhnw.oceandive.model.user;

import ch.fhnw.oceandive.model.Role;

import ch.fhnw.oceandive.model.activity.DiveLog;
import ch.fhnw.oceandive.model.activity.DiveCertification;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;



@Entity(name = "users")
public class UserEntity {

  @Id
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Hidden
  private Long id;

  @NotBlank
  @Size(max = 70)
  private String firstName;

  @NotBlank
  @Size(max = 70)
  private String lastName;

  @Email
  @NotBlank
  @Size(max = 100)
  @Column(unique = true)
  private String email;

  @NotBlank
  @Size(max = 50)
  @Column(unique = true)
  private String username;

  @NotBlank
  @Size(min=8, max = 100)
  @Hidden
  private String password;

  @Enumerated(EnumType.STRING)
  private DiveCertification diveCertification;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdOn;

  @CreationTimestamp
  @Column(updatable = true)
  private LocalDateTime updatedOn;

  @Column(name = "user_type")
  private String userType = "USER";

  @NotNull
  private Integer bookingsCount = 0;

  @Transient
  private boolean temporary = false;

  @OneToMany
      (mappedBy = "user", fetch = FetchType.LAZY)
  private Set<DiveLog> diveLogs = new HashSet<>();


  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"})
  )
  private Set<Role> roles = new HashSet<>();


  public UserEntity() {
    // Default constructor
  }

  public UserEntity(String firstName, String lastName, String email, String username,
      String password) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.username = username;
    this.password = password;
    this.userType = "USER";
    this.roles.add(defaultRole()); // TODO default role Logic
  }
}

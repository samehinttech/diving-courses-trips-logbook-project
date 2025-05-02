package ch.fhnw.oceandive.model;
import ch.fhnw.oceandive.model.activity.DiveLog;
import ch.fhnw.oceandive.model.activity.DiveCertification;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.usertype.UserType;


@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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


  @OneToMany
      (mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<DiveLog> diveLogs = new HashSet<>();

  @ElementCollection
      (fetch  = FetchType.EAGER)
  @CollectionTable(name = "roles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role")
  private Set<String> roles = new HashSet<>();

















}

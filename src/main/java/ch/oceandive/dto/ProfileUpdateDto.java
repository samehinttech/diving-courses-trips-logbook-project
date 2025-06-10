package ch.oceandive.dto;

public class ProfileUpdateDto {
  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private String mobile;
  private String username;
  private String diveCertification;
  private String roleLimitation;

  // Constructors
  public ProfileUpdateDto() {}

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getDiveCertification() {
    return diveCertification;
  }

  public void setDiveCertification(String diveCertification) {
    this.diveCertification = diveCertification;
  }

  public String getRoleLimitation() {
    return roleLimitation;
  }

  public void setRoleLimitation(String roleLimitation) {
    this.roleLimitation = roleLimitation;
  }
}
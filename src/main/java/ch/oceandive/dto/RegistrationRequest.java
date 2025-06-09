package ch.oceandive.dto;

import ch.oceandive.model.DiveCertification;
import ch.oceandive.validation.PasswordPattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration requests
 */
public class RegistrationRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Mobile number must be between 8 and 15 digits and may start with +")
    private String mobile;
    
    private DiveCertification diveCertification;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$", message = "Username can only contain letters, numbers, periods, underscores, and hyphens")
    private String username;
    
    @NotBlank(message = "Password is required")
    @PasswordPattern
    private String password;
    
    // For admin registrations - defines the admin's role limitations
    private String roleLimitation;
    
    // Default constructor
    public RegistrationRequest() {}
    
    // Full constructor
    public RegistrationRequest(String firstName, String lastName, String email, String mobile, 
                               DiveCertification diveCertification, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.diveCertification = diveCertification;
        this.username = username;
        this.password = password;
    }
    
    // Admin constructor with role limitation
    public RegistrationRequest(String firstName, String lastName, String email, String mobile, 
                               String username, String password, String roleLimitation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.username = username;
        this.password = password;
        this.roleLimitation = roleLimitation;
    }
    
    // Getters and Setters
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
    
    public DiveCertification getDiveCertification() {
        return diveCertification;
    }
    
    public void setDiveCertification(DiveCertification diveCertification) {
        this.diveCertification = diveCertification;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRoleLimitation() {
        return roleLimitation;
    }
    
    public void setRoleLimitation(String roleLimitation) {
        this.roleLimitation = roleLimitation;
    }
    // Helper method for backwards compatibility with string-based systems
    public String getDiveCertificationString() {
        return diveCertification != null ? diveCertification.name() : null;
    }

    public void setDiveCertificationString(String diveCertificationString) {
        this.diveCertification = DiveCertification.fromString(diveCertificationString);
    }

    // Helper methods for display purposes
    public String getDiveCertificationDisplayName() {
        return diveCertification != null ? diveCertification.getDisplayName() : "";
    }

    public String getDiveCertificationFullDisplayName() {
        return diveCertification != null ? diveCertification.getFullDisplayName() : "";
    }

    public String getDiveCertificationLowercase() {
        return diveCertification != null ? diveCertification.getDisplayNameLowercase() : "";
    }

    public String getDiveCertificationUppercase() {
        return diveCertification != null ? diveCertification.getDisplayNameUppercase() : "";
    }
}
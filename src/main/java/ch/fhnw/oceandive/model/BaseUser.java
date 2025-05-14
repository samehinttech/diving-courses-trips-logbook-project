package ch.fhnw.oceandive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * Base class for all user types in the system.
 * Contains common fields shared by PremiumUser, Admin, and GuestUser.
 */
@MappedSuperclass
public abstract class BaseUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @NotEmpty(message = "Please enter your first name")
    @Column(nullable = false)
    protected String firstName;

    @NotEmpty(message = "Please enter your last name")
    @Column(nullable = false)
    protected String lastName;

    @NotEmpty(message = "Please enter your email")
    @Column(nullable = false)
    @Email(message = "Please enter a valid email address")
    protected String email;

    @NotEmpty(message = "Please enter your mobile number")
    protected String mobile;

    @Column(nullable = false)
    protected String role;

    // Default constructor
    public BaseUser() {
    }

    // Parameterized constructor
    public BaseUser(String firstName, String lastName, String email, String mobile, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.role = role;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

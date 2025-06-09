package ch.oceandive.model;

import ch.fhnw.oceandive.validation.PasswordPattern;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing an administrator in the system.
 * Admins can manage courses, trips, users, and dive logs.
 */
@Entity
@Table(name = "admins", indexes = {
    @Index(name = "idx_admin_email", columnList = "email"),
    @Index(name = "idx_admin_username", columnList = "username")
})
public class Admin extends BaseUser {

    @NotEmpty(message = "Please enter a username")
    @Column(nullable = false, unique = true)
    private String username;

    @NotEmpty(message = "Please enter a password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @PasswordPattern
    private String password;

    private String roleLimitation;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    // Default constructor
    public Admin() {
    }

    // Parameterized constructor
    public Admin(String firstName, String lastName, String email, String mobile, String password,
                String role, String roleLimitation) {
        super(firstName, lastName, email, mobile, role);
        this.password = password;
        this.roleLimitation = roleLimitation;
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
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

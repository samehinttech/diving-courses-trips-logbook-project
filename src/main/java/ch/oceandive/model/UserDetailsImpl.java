package ch.oceandive.model;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Custom implementation of UserDetails for Spring Security.
 * This class represents the authenticated user and their roles.
 * It provides methods to access user information and check roles.
 */
public class UserDetailsImpl implements UserDetails {

  private final String username;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(String username, String password, Collection<? extends GrantedAuthority> authorities) {
    this.username = username;
    this.password = password;
    this.authorities = authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  /**
   * Get the primary role of the user
   */
  public String getRole() {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> auth.startsWith("ROLE_"))
        .findFirst()
        .orElse("NO_ROLE");
  }

  /**
   * Check if user has a specific role
   */
  public boolean hasRole(String role) {
    String roleToCheck = role.startsWith("ROLE_") ? role : "ROLE_" + role;
    return authorities.stream()
        .anyMatch(auth -> auth.getAuthority().equals(roleToCheck));
  }

  @Override
  public String toString() {
    return "UserDetailsImpl{" +
        "username='" + username + '\'' +
        ", authorities=" + authorities +
        '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof UserDetailsImpl that)) return false;
    return username.equals(that.username);
  }

  @Override
  public int hashCode() {
    return username.hashCode();
  }
}
package ch.fhnw.oceandive.securityConfig;

import ch.fhnw.oceandive.model.UserEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * This class represents a login request and also implements UserDetails for authentication.
 * It's used for both receiving login credentials from clients and for authentication within Spring Security.
 */
public class CustomUserDetails implements UserDetails {

  private String username;
  private String password;
  private List<GrantedAuthority> authorities;
  private boolean accountNonExpired = true;
  private boolean accountNonLocked = true;
  private boolean credentialsNonExpired = true;
  private boolean enabled = true;

  // Default constructor for JSON deserialization
  public CustomUserDetails() {
  }

  public CustomUserDetails(UserEntity userEntity) {
    this.username = userEntity.getUsername();
    this.password = userEntity.getPassword();
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userEntity.getUserType().name()));
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setAuthorities(List<GrantedAuthority> authorities) {
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
    return accountNonExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}

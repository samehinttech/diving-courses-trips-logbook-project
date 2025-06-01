package ch.fhnw.oceandive.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserDetailsImpl implements UserDetails {

  private static final Logger logger = LoggerFactory.getLogger(UserDetailsImpl.class);

  private String username;
  private String password;
  private List<GrantedAuthority> authorities;

  public UserDetailsImpl(PremiumUser premiumUser, Admin admin) {
    initializeUserDetails(premiumUser, admin);
  }

  private void initializeUserDetails(PremiumUser premiumUser, Admin admin) {
    authorities = new ArrayList<>();

    // Log a warning if both user types are provided
    if (premiumUser != null && admin != null) {
      logger.warn("Both user  and Admin provided to the user details - this may indicate a security issue");
    }

    // Handle admin if present (admins have priority)
    if (admin != null) {
      username = admin.getUsername();
      password = admin.getPassword();
      String role = "ROLE_" + admin.getRole().toUpperCase();
      authorities.add(new SimpleGrantedAuthority(role));
      logger.debug("Created UserDetails for Admin: {} with role: {}", username, role);
    }
    // Handle premium user if present
    else if (premiumUser != null) {
      username = premiumUser.getUsername();
      password = premiumUser.getPassword();
      String role = "ROLE_" + premiumUser.getRole().toUpperCase();
      authorities.add(new SimpleGrantedAuthority(role));
      logger.debug("Created UserDetails for user: {} with role: {}", username, role);
    }

    // Add basic read authority
    authorities.add(new SimpleGrantedAuthority("READ"));
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
    return true; // Default implementation
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // Default implementation
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // Default implementation
  }

  @Override
  public boolean isEnabled() {
    return true; // Default implementation
  }
}
package ch.fhnw.oceandive.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserDetailsImpl implements UserDetails {

  private String username;
  private String password;
  private List<GrantedAuthority> authorities;

  public UserDetailsImpl(PremiumUser premiumUser, Admin admin) {
    initializeUserDetails(premiumUser, admin);
  }

  private void initializeUserDetails(PremiumUser premiumUser, Admin admin) {
    authorities = new ArrayList<>();

    // Handle premium user if present
    if (premiumUser != null) {
      username = premiumUser.getUsername();
      password = premiumUser.getPassword();
      authorities.add(new SimpleGrantedAuthority("ROLE_" + premiumUser.getRole().toUpperCase()));
    }

    // Handle admin if present
    if (admin != null) {
      // If a premium user is null, use admin credentials
      if (premiumUser == null) {
        username = admin.getUsername();
        password = admin.getPassword();
      }
      authorities.add(new SimpleGrantedAuthority("ROLE_" + admin.getRole().toUpperCase()));
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

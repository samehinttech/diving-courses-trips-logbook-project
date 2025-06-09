package ch.oceandive.model;

import ch.oceandive.dto.AdminDTO;
import ch.oceandive.dto.PremiumUserDTO;
import ch.oceandive.service.AdminService;
import ch.oceandive.service.PremiumUserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * UserDetailsService implementation that loads user details for authentication.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final PremiumUserService premiumUserService;
  private final AdminService adminService;

  public UserDetailsServiceImpl(PremiumUserService premiumUserService, AdminService adminService) {
    this.premiumUserService = premiumUserService;
    this.adminService = adminService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Try to find user as Premium User first
    try {
      PremiumUserDTO premiumUserDTO = premiumUserService.getPremiumUserByUsername(username);
      if (premiumUserDTO != null) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM"));

        return new UserDetailsImpl(
            premiumUserDTO.getUsername(),
            premiumUserDTO.getPassword(),
            authorities
        );
      }
    } catch (Exception e) {
      // Continue to try admin lookup
    }
    // Try to find user as Admin
    try {
      AdminDTO adminDTO = adminService.getAdminByUsername(username);
      if (adminDTO != null) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        return new UserDetailsImpl(
            adminDTO.getUsername(),
            adminDTO.getPassword(),
            authorities
        );
      }
    } catch (Exception e) {
      // Continue to throw exception
    }
    throw new UsernameNotFoundException("User not found: " + username);
  }
  // Maybe I will use it TODO check this method
  public UserDetails loadUserByUsernameOrEmail(String usernameOrEmail) throws UsernameNotFoundException {
    // First try as username
    try {
      return loadUserByUsername(usernameOrEmail);
    } catch (UsernameNotFoundException e) {
      // If username fails, try as email (you'd need to add these methods to your services)
      // For now, just rethrow the exception
      throw new UsernameNotFoundException("User not found: " + usernameOrEmail);
    }
  }
   // Check if a user exists by username
  public boolean userExists(String username) {
    try {
      loadUserByUsername(username);
      return true;
    } catch (UsernameNotFoundException e) {
      return false;
    }
  }
}
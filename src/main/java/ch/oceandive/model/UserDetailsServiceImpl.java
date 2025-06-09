package ch.oceandive.model;

import ch.oceandive.dto.AdminDTO;
import ch.oceandive.dto.PremiumUserDTO;
import ch.oceandive.service.AdminService;
import ch.oceandive.service.PremiumUserService;
import org.slf4j.Logger;
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
  Logger logger = org.slf4j.LoggerFactory.getLogger(UserDetailsServiceImpl.class);

  public UserDetailsServiceImpl(PremiumUserService premiumUserService, AdminService adminService) {
    this.premiumUserService = premiumUserService;
    this.adminService = adminService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // debugging to check where the error occurs when trying to login
    logger.debug("loadUserByUsername: {}", username);
    // Try to find user as Premium User first
    try {
      PremiumUserDTO premiumUserDTO = premiumUserService.getPremiumUserByUsername(username);
      if (premiumUserDTO != null) {
        logger.debug("Found user as Premium User: {},password hash: {}", username, premiumUserDTO.getPassword()
        != null? "exists" : "not exists");
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM"));

        return new UserDetailsImpl(
            premiumUserDTO.getUsername(),
            premiumUserDTO.getPassword(),
            authorities
        );
      }
    } catch (Exception e) {
      logger.error("Error finding Premium User: {}", e.getMessage());
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
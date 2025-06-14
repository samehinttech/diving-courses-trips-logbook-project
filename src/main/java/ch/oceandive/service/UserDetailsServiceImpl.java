package ch.oceandive.service;

import ch.oceandive.model.Admin;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.repository.AdminRepo;
import ch.oceandive.repository.PremiumUserRepo;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService implementation that loads user details for authentication.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final PremiumUserService premiumUserService;
  private final AdminService adminService;
  private final PremiumUserRepo premiumUserRepo;
  private final AdminRepo adminRepo;


  public UserDetailsServiceImpl(PremiumUserService premiumUserService, AdminService adminService, PremiumUserRepo premiumUserRepo,
      PremiumUserRepo premiumUserRepo1, AdminRepo adminRepo) {
    this.premiumUserService = premiumUserService;
    this.adminService = adminService;
    this.premiumUserRepo = premiumUserRepo1;
    this.adminRepo = adminRepo;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    PremiumUser premiumUser = premiumUserRepo.findByUsername(username);
    if (premiumUser != null) {
      return User.builder()
          .username(premiumUser.getUsername())
          .password(premiumUser.getPassword())
          .authorities(premiumUser.getRole())
          .build();
    } else {
      // Try to find a user as Admin
      Admin admin = adminRepo.findByUsername(username);
      if (admin != null) {
        return User.builder()
            .username(admin.getUsername())
            .password(admin.getPassword())
            .authorities(admin.getRole())
            .build();
      }
    }
    // If no user found, throw exception
    throw new UsernameNotFoundException("{} No one found with username: " + username);
  }
}
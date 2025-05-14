package ch.fhnw.oceandive.model;

import ch.fhnw.oceandive.repository.AdminRepo;
import ch.fhnw.oceandive.repository.PremiumUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private PremiumUserRepo premiumUserRepo;
  @Autowired
  private AdminRepo adminRepo;


  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Try to find the user by username in both repositories first
    Admin admin = adminRepo.findByUsername(username);
    PremiumUser premiumUser = premiumUserRepo.findByUsername(username);

    // If not found by username, try email (only if needed)
    if (admin == null && premiumUser == null) {
      admin = adminRepo.findByEmail(username);
      // Only check the premium user by email if admin is still null
      if (admin == null) {
        premiumUser = premiumUserRepo.findByEmail(username);
      }
    }
    // If neither user type is found, throw an exception
    if (admin == null && premiumUser == null) {
      throw new UsernameNotFoundException("User with username/email " + username + " not found");
    }
    // Create and return UserDetailsImpl with the found user(s)
    return new UserDetailsImpl(premiumUser, admin);

  }
}

package ch.fhnw.oceandive.model;

import ch.fhnw.oceandive.repository.AdminRepo;
import ch.fhnw.oceandive.repository.PremiumUserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

  private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);


  private final PremiumUserRepo premiumUserRepo;

  private final AdminRepo adminRepo;

  public UserDetailsServiceImpl(PremiumUserRepo premiumUserRepo, AdminRepo adminRepo) {
    this.premiumUserRepo = premiumUserRepo;
    this.adminRepo = adminRepo;
  }

  /**
   * Loads user by username or email with priority given to Admin accounts.
   * If a user exists in both Admin and PremiumUser repositories, the Admin account will be used.
   */
  @Override
  public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    // First check for Admin by username
    Admin admin = adminRepo.findByUsername(identifier);
    if (admin != null) {
      logger.debug("Found Admin by username: {}", identifier);
      return new UserDetailsImpl(null, admin); // Admin only
    }
    
    // Then check for PremiumUser by username
    PremiumUser premiumUser = premiumUserRepo.findByUsername(identifier);
    if (premiumUser != null) {
      logger.debug("Found user by username: {}", identifier);
      return new UserDetailsImpl(premiumUser, null); // PremiumUser only
    }
    
    // If not found by username, check for Admin by email
    admin = adminRepo.findByEmail(identifier);
    if (admin != null) {
      logger.debug("Found Admin by email: {}", identifier);
      return new UserDetailsImpl(null, admin); // Admin only
    }
    
    // Finally, check for PremiumUser by email
    premiumUser = premiumUserRepo.findByEmail(identifier);
    if (premiumUser != null) {
      logger.debug("Found user by email: {}", identifier);
      return new UserDetailsImpl(premiumUser, null); // PremiumUser only
    }
    
    // If user not found, throw exception
    logger.warn("No user found with username/email: {}", identifier);
    throw new UsernameNotFoundException("User with username/email " + identifier + " not found");
  }
}
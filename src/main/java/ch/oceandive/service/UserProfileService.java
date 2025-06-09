package ch.oceandive.service;

import ch.oceandive.dto.AdminDTO;
import ch.oceandive.dto.PremiumUserDTO;
import ch.oceandive.repository.UserProfileRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Collection;

/**
 * Implementation of the UserProfileRepo interface that handles different user types.
 */
@Service
public class UserProfileService implements UserProfileRepo {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);
    
    private final PremiumUserService premiumUserService;
    private final AdminService adminService;
    public UserProfileService(PremiumUserService premiumUserService, AdminService adminService) {
        this.premiumUserService = premiumUserService;
        this.adminService = adminService;
    }

    @Override
    public String loadUserProfile(String username, Model model) {
        logger.info("Loading profile for user: {}", username);
        
        if (hasRole("ROLE_ADMIN")) {
            AdminDTO adminProfile = adminService.getAdminByUsername(username);
            model.addAttribute("user", adminProfile);
            model.addAttribute("userType", "admin");
            model.addAttribute("pageTitle", "Admin Profile - OceanDive");
            return "admin";
        } else if (hasRole("ROLE_PREMIUM")) {
            PremiumUserDTO userProfile = premiumUserService.getPremiumUserByUsername(username);
            model.addAttribute("user", userProfile);
            model.addAttribute("userType", "premium");
            model.addAttribute("pageTitle", "My Profile - OceanDive");
            return "premium";
        } else {
            logger.warn("User {} has no valid role for profile access", username);
            throw new SecurityException("You don't have the required role to access this profile. Required roles: ROLE_ADMIN or ROLE_PREMIUM");
        }
    }

    @Override
    public String loadUserProfileForEdit(String username, Model model) throws Exception {
        logger.info("Loading edit profile for user: {}", username);
        
        if (hasRole("ROLE_ADMIN")) {
            AdminDTO adminProfile = adminService.getAdminByUsername(username);
            model.addAttribute("user", adminProfile);
            model.addAttribute("userType", "admin");
            model.addAttribute("pageTitle", "Edit Admin Profile - OceanDive");
            return "admin";
        } else if (hasRole("ROLE_PREMIUM")) {
            PremiumUserDTO userProfile = premiumUserService.getPremiumUserByUsername(username);
            model.addAttribute("user", userProfile);
            model.addAttribute("userType", "premium");
            model.addAttribute("pageTitle", "Edit Profile - OceanDive");
            return "premium";
        } else {
            logger.warn("User {} has no valid role for profile edit access", username);
            throw new SecurityException("You don't have the required role to edit this profile. Required roles: ROLE_ADMIN or ROLE_PREMIUM");
        }
    }

    @Override
    public String updateUserProfile(String username, Object user) {
        logger.info("Updating profile for user: {}", username);
        
        if (hasRole("ROLE_ADMIN") && user instanceof AdminDTO adminDTO) {
            adminService.updateAdmin(adminDTO.getId(), adminDTO);
            return "Admin profile updated successfully";
        } else if (hasRole("ROLE_PREMIUM") && user instanceof PremiumUserDTO userDTO) {
            premiumUserService.updatePremiumUser(userDTO.getId(), userDTO);
            return "Profile updated successfully";
        } else {
            logger.warn("User {} has no valid role or data for profile update", username);
            throw new SecurityException("You don't have the required role to update this profile or invalid data provided");
        }
    }
    
    /**
     * Helper method to check if the current user has a specific role.
     * 
     * @param role The role to check for
     * @return true if the user has the role, false otherwise
     */
    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<?> authorities = authentication != null ? authentication.getAuthorities() : null;
        return authorities != null && authorities.contains(new SimpleGrantedAuthority(role));
    }
}
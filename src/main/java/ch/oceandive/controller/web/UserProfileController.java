package ch.oceandive.controller.web;


import ch.oceandive.model.Admin;
import ch.oceandive.model.DiveCertification;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.repository.AdminRepo;
import ch.oceandive.repository.PremiumUserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/my-profile")
public class UserProfileController {

  private final PremiumUserRepo premiumUserRepo;
  private final AdminRepo adminRepo;

  public UserProfileController(PremiumUserRepo premiumUserRepo, AdminRepo adminRepo) {
    this.premiumUserRepo = premiumUserRepo;
    this.adminRepo = adminRepo;
  }

  /**
   * Display user profile
   */
  @GetMapping
  public String viewProfile(Model model, Authentication authentication) {
    // Check if authentication exists AND is authenticated
    if (authentication == null || !authentication.isAuthenticated()) {
      return "redirect:/login";
    }

    String username = authentication.getName();
    Object user = null;
    String userType = null;

    // Check if user is admin
    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
      Admin admin = adminRepo.findByUsername(username);
      if (admin != null) {
        user = admin;
        userType = "admin";
      }
    }
    // Otherwise check premium user
    else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PREMIUM"))) {
      PremiumUser premiumUser = premiumUserRepo.findByUsername(username);
      if (premiumUser != null) {
        user = premiumUser;
        userType = "premium";
      }
    }

    if (user == null) {
      return "redirect:/login";
    }

    model.addAttribute("user", user);
    model.addAttribute("userType", userType);
    model.addAttribute("pageTitle", "My Profile - OceanDive");

    return "user-profile";
  }

  /**
   * Show edit profile form
   */
  @GetMapping("/edit")
  public String editProfile(Model model, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return "redirect:/login";
    }

    String username = authentication.getName();
    Object user = null;
    String userType = null;

    // Check if user is admin
    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
      Admin admin = adminRepo.findByUsername(username);
      if (admin != null) {
        user = admin;
        userType = "admin";
      }
    }
    // Otherwise check premium user
    else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PREMIUM"))) {
      PremiumUser premiumUser = premiumUserRepo.findByUsername(username);
      if (premiumUser != null) {
        user = premiumUser;
        userType = "premium";
      }
    }

    if (user == null) {
      return "redirect:/login";
    }

    model.addAttribute("user", user);
    model.addAttribute("userType", userType);
    model.addAttribute("pageTitle", "Edit Profile - OceanDive");

    return "profile-edit";
  }

  /**
   * Update user profile
   */
  @PostMapping("/update")
  public String updateProfile(
      @RequestParam(required = false) String firstName,
      @RequestParam(required = false) String lastName,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String mobile,
      @RequestParam(required = false) String diveCertification,
      @RequestParam(required = false) String roleLimitation,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {

    if (authentication == null || !authentication.isAuthenticated()) {
      return "redirect:/login";
    }

    String username = authentication.getName();
    boolean updated = false;

    try {
      // Update admin profile
      if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        Admin admin = adminRepo.findByUsername(username);
        if (admin != null) {
          admin.setFirstName(firstName);
          admin.setLastName(lastName);
          admin.setEmail(email);
          admin.setMobile(mobile);
          admin.setRoleLimitation(roleLimitation);
          admin.setUpdatedAt(LocalDateTime.now());
          adminRepo.save(admin);
          updated = true;
        }
      }
      // Update premium user profile
      else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PREMIUM"))) {
        PremiumUser premiumUser = premiumUserRepo.findByUsername(username);
        if (premiumUser != null) {
          premiumUser.setFirstName(firstName);
          premiumUser.setLastName(lastName);
          premiumUser.setEmail(email);
          premiumUser.setMobile(mobile);
          if (diveCertification != null && !diveCertification.isEmpty()) {
            try {
              premiumUser.setDiveCertification(DiveCertification.valueOf(diveCertification));
            } catch (IllegalArgumentException e) {
              // Handle invalid enum value
              redirectAttributes.addFlashAttribute("error", "Invalid dive certification selected");
              return "redirect:/my-profile/edit";
            }
          }
          premiumUser.setUpdatedAt(LocalDateTime.now());
          premiumUserRepo.save(premiumUser);
          updated = true;
        }
      }

      if (updated) {
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
      } else {
        redirectAttributes.addFlashAttribute("error", "Unable to update profile. Please try again.");
      }

    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "An error occurred while updating your profile: " + e.getMessage());
    }

    return "redirect:/my-profile";
  }
}


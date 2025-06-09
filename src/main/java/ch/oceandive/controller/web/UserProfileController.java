package ch.oceandive.controller.web;

import ch.oceandive.repository.UserProfileRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling user profile pages
 * Using /user-profile to avoid conflicts with Spring Data REST
 */
@Controller
public class UserProfileController {

  private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

  private final UserProfileRepo userProfileRepo;
  private static final String PAGE_TITLE_PROFILE = "My Profile - OceanDive";
  private static final String PAGE_TITLE_EDIT = "Edit-MY-Profile - OceanDive";

  public UserProfileController(UserProfileRepo userProfileRepo) {
    this.userProfileRepo = userProfileRepo;
    logger.info("ProfileController initialized successfully");
  }

  /**
   * Redirect /profile to /user-profile for convenience
   */
  @GetMapping("/profile")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PREMIUM')")
  public String redirectToProfile() {
    logger.info("Redirecting /profile to /user-profile");
    return "redirect:/user-profile";
  }

  /**
   * Display the user profile page
   * Requires either ADMIN or PREMIUM role
   * Changed URL to /user-profile to avoid Spring Data REST conflict
   */
  @GetMapping("/user-profile")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PREMIUM')")
  public String showProfile(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
    logger.info("ProfileController.showProfile() called for user: {}", authentication.getName());
    model.addAttribute("pageTitle", PAGE_TITLE_PROFILE);

    // Debug logging to see what's in authentication
    logger.info("Current user: {}", authentication.getName());
    logger.info("Authorities: {}", authentication.getAuthorities());
    authentication.getAuthorities().forEach(auth ->
        logger.info("Authority: {}", auth.getAuthority()));

    try {
      String username = authentication.getName();
      logger.info("Loading profile for user: {}", username);
      String viewName = userProfileRepo.loadUserProfile(username, model);
      logger.info("Profile loaded successfully with viewName: {}", viewName);
      return viewName;
    } catch (SecurityException e) {
      logger.warn("Access denied: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/login";
    } catch (Exception e) {
      logger.error("Error loading profile for user: {}", e.getMessage(), e);
      redirectAttributes.addFlashAttribute("error", "Error loading profile information: " + e.getMessage());
      return "redirect:/";
    }
  }

  /**
   * Handle profile edit page
   * Requires either ADMIN or PREMIUM role
   */
  @GetMapping("/user-profile/edit")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PREMIUM')")
  public String showEditProfile(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
    logger.info("ProfileController.showEditProfile() called for user: {}", authentication.getName());
    model.addAttribute("pageTitle", PAGE_TITLE_EDIT);
    try {
      String username = authentication.getName();
      String viewName = userProfileRepo.loadUserProfileForEdit(username, model);
      return viewName;
    } catch (SecurityException e) {
      logger.warn("Access denied for edit profile: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/login";
    } catch (Exception e) {
      logger.error("Error loading edit profile for user.", e);
      redirectAttributes.addFlashAttribute("error", "Error loading profile information: " + e.getMessage());
      return "redirect:/user-profile";
    }
  }

  /**
   * Handle profile update form submission
   * Requires either ADMIN or PREMIUM role
   */
  @PostMapping("/user-profile/update")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PREMIUM')")
  public String updateProfile(@ModelAttribute Object user, Authentication authentication,
      RedirectAttributes redirectAttributes) {
    logger.info("ProfileController.updateProfile() called for user: {}", authentication.getName());
    try {
      String username = authentication.getName();
      String successMessage = userProfileRepo.updateUserProfile(username, user);
      redirectAttributes.addFlashAttribute("success", successMessage);
      return "redirect:/user-profile";
    } catch (SecurityException e) {
      logger.warn("Access denied for profile update: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/login";
    } catch (Exception e) {
      logger.error("Error updating profile: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
      return "redirect:/user-profile/edit";
    }
  }
}
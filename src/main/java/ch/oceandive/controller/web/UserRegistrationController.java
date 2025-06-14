package ch.oceandive.controller.web;

import ch.oceandive.dto.PremiumUserDTO;
import ch.oceandive.dto.RegistrationRequest;
import ch.oceandive.exceptionHandler.DuplicateResourceException;
import ch.oceandive.exceptionHandler.ValidationException;
import ch.oceandive.service.PremiumUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// MVC Controller for handling user registration form
@Controller
public class UserRegistrationController {

  private static final Logger logger = LoggerFactory.getLogger(UserRegistrationController.class);

  private final PremiumUserService premiumUserService;

  public UserRegistrationController(PremiumUserService premiumUserService) {
    this.premiumUserService = premiumUserService;
  }

  // Show the registration form
  @GetMapping("/register")
  public String showRegistrationForm(Model model) {
    model.addAttribute("registrationRequest", new RegistrationRequest());
    return "register";
  }

  // Handle registration form submission
  @PostMapping("/register")
  public String processRegistration(
      @Valid @ModelAttribute("registrationRequest") RegistrationRequest registrationRequest,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes) {

    // If there are validation errors, return to the form
    if (bindingResult.hasErrors()) {
      logger.warn("Registration form has validation errors for user: {}",
          registrationRequest.getUsername());
      return "register";
    }
    try {
      // Convert RegistrationRequest to PremiumUserDTO
      PremiumUserDTO userDto = registrationRequest.toPremiumUserDTO();
      // Call service directly instead of making HTTP calls
      premiumUserService.createPremiumUser(userDto);
      logger.info("Registration successful for username: {}", registrationRequest.getUsername());
      // Add a success message and redirect to log in
      redirectAttributes.addFlashAttribute("successMessage",
          "Registration successful! Please log in with your credentials.");
      return "redirect:/login";
    } catch (DuplicateResourceException e) {
      logger.error("Registration failed for user: {} - duplicate resource",
          registrationRequest.getUsername());
      model.addAttribute("errorMessage",
          "Username or email already exists. Please choose different credentials.");
      return "register";

    } catch (ValidationException e) {
      logger.error("Registration failed for user: {} - validation error",
          registrationRequest.getUsername());
      model.addAttribute("errorMessage",
          "Invalid registration data. Please check your information and try again.");
      // You can also add specific field errors if needed
      if (e.getErrors() != null) {
        model.addAttribute("errors", e.getErrors());
      }
      return "register";

    } catch (Exception e) {
      logger.error("Unexpected error during registration for user: {}",
          registrationRequest.getUsername(), e);
      model.addAttribute("errorMessage",
          "An unexpected error occurred. Please try again later.");
      return "register";
    }
  }
}
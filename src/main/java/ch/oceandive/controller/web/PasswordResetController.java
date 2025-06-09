package ch.oceandive.controller.web;

import ch.oceandive.dto.ForgotPasswordForm;
import ch.oceandive.dto.EmailVerificationForm;
import ch.oceandive.dto.ResetPasswordForm;
import ch.oceandive.model.PremiumUser;
import ch.oceandive.service.ResetPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class PasswordResetController {

  private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);
  private final ResetPasswordService resetPasswordService;

  public PasswordResetController(ResetPasswordService resetPasswordService) {
    this.resetPasswordService = resetPasswordService;
  }

  /**
   * Show forgot password form (GET)
   */
  @GetMapping("/forgot-password")
  public String showForgotPasswordForm(Model model) {
    model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
    model.addAttribute("pageTitle", "Forgot Password - OceanDive");
    return "reset/forgot-password";
  }

  /**
   * Process forgot password request (POST)
   */
  @PostMapping("/forgot-password")
  public String processForgotPassword(@ModelAttribute ForgotPasswordForm forgotPasswordForm,
      RedirectAttributes redirectAttributes) {
    try {
      if (forgotPasswordForm.getEmail() == null ||
          forgotPasswordForm.getEmail().trim().isEmpty() ||
          !isValidEmail(forgotPasswordForm.getEmail())) {
        redirectAttributes.addFlashAttribute("error",
            "Please enter a valid email address.");
        return "redirect:/forgot-password";
      }
      String email = forgotPasswordForm.getEmail().trim().toLowerCase();
      resetPasswordService.initiatePasswordReset(email);

      redirectAttributes.addFlashAttribute("success",
          "If an account with " + email + " exists, we've sent password reset instructions.");

    } catch (Exception e) {
      logger.error("Error processing forgot password request", e);
      redirectAttributes.addFlashAttribute("error",
          "An error occurred. Please try again later.");
    }

    return "redirect:/forgot-password";
  }

  /**
   * Show email verification form for reset token (GET)
   */
  @GetMapping("/reset-password")
  public String showEmailVerificationForm(@RequestParam("token") String token, Model model) {
    if (!resetPasswordService.validateResetToken(token)) {
      model.addAttribute("error", "Invalid or expired reset token.");
      model.addAttribute("pageTitle", "Invalid Token - OceanDive");
      return "reset/reset-password-error";
    }

    EmailVerificationForm verificationForm = new EmailVerificationForm(token);
    model.addAttribute("emailVerificationForm", verificationForm);
    model.addAttribute("pageTitle", "Verify Your Email - OceanDive");
    model.addAttribute("step", "verification");

    return "reset/reset-password";
  }

  /**
   * Process email verification and password reset (POST)
   */
  @PostMapping("/reset-password")
  public String processResetPassword(@ModelAttribute EmailVerificationForm emailVerificationForm,
      @RequestParam(required = false) String password,
      @RequestParam(required = false) String confirmPassword,
      @RequestParam(required = false) String step,
      Model model,
      RedirectAttributes redirectAttributes) {
    try {
      // Step 1: Email Verification
      if ("verification".equals(step) || password == null || password.trim().isEmpty()) {
        return handleEmailVerification(emailVerificationForm, model);
      }
      // Step 2: Password Reset
      else {
        return handlePasswordReset(emailVerificationForm.getToken(),
            emailVerificationForm.getEmail(),
            password, confirmPassword, redirectAttributes);
      }

    } catch (Exception e) {
      logger.error("Error processing reset password", e);
      model.addAttribute("error", "An error occurred. Please try again.");
      model.addAttribute("emailVerificationForm", emailVerificationForm);
      return "reset/reset-password";
    }
  }

  /**
   * Handle email verification step
   */
  private String handleEmailVerification(EmailVerificationForm verificationForm, Model model) {
    if (!resetPasswordService.validateResetToken(verificationForm.getToken())) {
      model.addAttribute("error", "Invalid or expired reset token.");
      return "reset/reset-password-error";
    }

    PremiumUser tokenUser = resetPasswordService.getUserByResetToken(verificationForm.getToken());
    if (tokenUser == null) {
      model.addAttribute("error", "Invalid reset token.");
      return "reset/reset-password-error";
    }

    String enteredEmail = verificationForm.getEmail().trim().toLowerCase();
    String tokenEmail = tokenUser.getEmail().toLowerCase();

    if (!enteredEmail.equals(tokenEmail)) {
      model.addAttribute("error",
          "The email address you entered doesn't match our records for this reset link.");
      model.addAttribute("emailVerificationForm", verificationForm);
      model.addAttribute("step", "verification");
      return "reset/reset-password";
    }

    // Email verified! Show password reset form
    ResetPasswordForm resetForm = new ResetPasswordForm(
        verificationForm.getToken(), verificationForm.getEmail());

    model.addAttribute("resetPasswordForm", resetForm);
    model.addAttribute("username", tokenUser.getUsername());
    model.addAttribute("step", "password");
    model.addAttribute("success", "Email verified! Please enter your new password.");

    return "reset/reset-password";
  }

  /**
   * Handle password reset step
   */
  private String handlePasswordReset(String token, String email, String password,
      String confirmPassword, RedirectAttributes redirectAttributes) {
    if (password == null || password.trim().isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Password cannot be empty.");
      return "redirect:/reset-password?token=" + token;
    }

    if (password.length() < 6) {
      redirectAttributes.addFlashAttribute("error",
          "Password must be at least 6 characters long.");
      return "redirect:/reset-password?token=" + token;
    }

    if (!password.equals(confirmPassword)) {
      redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
      return "redirect:/reset-password?token=" + token;
    }

    resetPasswordService.resetPassword(token, password);

    redirectAttributes.addFlashAttribute("success",
        "Your password has been successfully reset. You can now log in with your new password.");

    return "redirect:/login";
  }

  /**
   * Simple email validation
   */
  private boolean isValidEmail(String email) {
    return email.contains("@") && email.contains(".") && email.length() > 5;
  }
}
package ch.oceandive.exceptionHandler;

import ch.oceandive.exceptionHandler.DuplicateResourceException;
import ch.oceandive.exceptionHandler.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global exception handler for web controllers
 * Handles exceptions for @Controller (web pages) and returns appropriate error views
 */
@ControllerAdvice(annotations = Controller.class)
public class WebExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(WebExceptionHandler.class);
  /**
   * Handle 403 Forbidden - Access Denied
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String handleAccessDenied(AccessDeniedException ex, Model model) {
    logger.warn("Access denied: {}", ex.getMessage());
    model.addAttribute("error", "Access Denied");
    model.addAttribute("message", "You don't have permission to access this resource.");
    model.addAttribute("statusCode", 403);
    return "error/403";
  }
  /**
   * Handle authentication failures
   */
  @ExceptionHandler(AuthenticationException.class)
  public String handleAuthenticationException(AuthenticationException ex, RedirectAttributes redirectAttributes) {
    logger.warn("Authentication failed: {}", ex.getMessage());
    redirectAttributes.addFlashAttribute("errorMessage", "Authentication failed. Please try again.");
    return "redirect:/login?error=auth";
  }
  /**
   * Handle bad credentials
   */
  @ExceptionHandler(BadCredentialsException.class)
  public String handleBadCredentials(BadCredentialsException ex, RedirectAttributes redirectAttributes) {
    logger.warn("Bad credentials: {}", ex.getMessage());
    redirectAttributes.addFlashAttribute("errorMessage", "Invalid username or password.");
    return "redirect:/login?error=credentials";
  }

  /**
   * Handle duplicate resource (e.g., username/email already exists)
   */
  @ExceptionHandler(DuplicateResourceException.class)
  public String handleDuplicateResource(DuplicateResourceException ex, Model model, RedirectAttributes redirectAttributes) {
    logger.warn("Duplicate resource: {}", ex.getMessage());

    // If it's a registration-related duplicate, redirect back to register
    if (ex.getMessage().toLowerCase().contains("username") || ex.getMessage().toLowerCase().contains("email")) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
      return "redirect:/register?error=duplicate";
    }
    // Otherwise show error page
    model.addAttribute("error", "Resource Already Exists");
    model.addAttribute("message", ex.getMessage());
    model.addAttribute("statusCode", 409);
    return "error/409";
  }
  /**
   * Handle validation errors
   */
  @ExceptionHandler(ValidationException.class)
  public String handleValidationException(ValidationException ex, Model model, RedirectAttributes redirectAttributes) {
    logger.warn("Validation error: {}", ex.getMessage());

    // If there are specific field errors, add them to the model
    if (ex.getErrors() != null && !ex.getErrors().isEmpty()) {
      model.addAttribute("validationErrors", ex.getErrors());
    }
    model.addAttribute("error", "Validation Error");
    model.addAttribute("message", ex.getMessage());
    model.addAttribute("statusCode", 400);
    return "error/400";
  }
  /**
   * Handle 404 - Page Not Found
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNoHandlerFound(NoHandlerFoundException ex, Model model) {
    logger.warn("Page not found: {}", ex.getRequestURL());
    model.addAttribute("error", "Page Not Found");
    model.addAttribute("message", "The page you're looking for doesn't exist.");
    model.addAttribute("requestedUrl", ex.getRequestURL());
    model.addAttribute("statusCode", 404);
    return "error/404";
  }
  /**
   * Handle all other exceptions, just an extra layer but the database exceptions are handled in the service layer
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleGeneralError(Exception ex, Model model) {
    logger.error("Unexpected error occurred", ex);
    model.addAttribute("error", "Internal Server Error");
    model.addAttribute("message", "An unexpected error occurred. Please try again later.");
    model.addAttribute("statusCode", 500);

    return "error/500";
  }

  /**
   * Handle IllegalArgumentException
   */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
    logger.warn("Invalid argument: {}", ex.getMessage());
    model.addAttribute("error", "Invalid Request");
    model.addAttribute("message", "The request contains invalid data.");
    model.addAttribute("statusCode", 400);
    return "error/400";
  }

  /**
   * Handle NullPointerException specifically
   */
  @ExceptionHandler(NullPointerException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleNullPointer(NullPointerException ex, Model model) {
    logger.error("Null pointer exception occurred", ex);
    model.addAttribute("error", "Internal Server Error");
    model.addAttribute("message", "A system error occurred. Please contact support if this persists.");
    model.addAttribute("statusCode", 500);
    return "error/500";
  }
}
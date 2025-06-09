package ch.oceandive.controller.web;

import ch.oceandive.dto.ApiResponse;
import ch.oceandive.dto.RegistrationRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


 // MVC Controller for handling user registration form

@Controller
public class RegistrationController {

  private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

  @Value("${server.port:8080}")
  private String serverPort;

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  private final RestTemplate restTemplate;


  public RegistrationController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


   //Show the registration form
  @GetMapping("/register")
  public String showRegistrationForm(Model model) {
    model.addAttribute("registrationRequest", new RegistrationRequest());
    return "register";
  }


   //Handle registration form submission

  @PostMapping("/register")
  public String processRegistration(@Valid @ModelAttribute("registrationRequest") RegistrationRequest registrationRequest,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes) {

    // If there are validation errors, return to the form
    if (bindingResult.hasErrors()) {
      logger.warn("Registration form has validation errors for user: {}", registrationRequest.getUsername());
      return "register";
    }

    try {
      // Call the REST API internally
      String apiUrl = buildApiUrl("/api/auth/user/register");

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<RegistrationRequest> request = new HttpEntity<>(registrationRequest, headers);

      ResponseEntity<ApiResponse> response = restTemplate.exchange(
          apiUrl,
          HttpMethod.POST,
          request,
          ApiResponse.class
      );
      if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null && response.getBody().isSuccess()) {
        logger.info("Registration successful for username: {}", registrationRequest.getUsername());

        // Add success message and redirect to login
        redirectAttributes.addFlashAttribute("successMessage",
            "Registration successful! Please log in with your credentials.");
        return "redirect:/login";
      } else {
        logger.warn("Registration API returned unexpected response for user: {}", registrationRequest.getUsername());
        model.addAttribute("errorMessage", "Registration failed. Please try again.");
        return "register";
      }

    } catch (HttpClientErrorException e) {
      logger.error("Registration failed for user: {} with status: {}",
          registrationRequest.getUsername(), e.getStatusCode());

      // Handle specific error cases
      if (e.getStatusCode() == HttpStatus.CONFLICT) {
        model.addAttribute("errorMessage",
            "Username or email already exists. Please choose different credentials.");
      } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
        model.addAttribute("errorMessage",
            "Invalid registration data. Please check your information and try again.");
      } else {
        model.addAttribute("errorMessage",
            "Registration failed. Please try again later.");
      }

      return "register";

    } catch (Exception e) {
      logger.error("Unexpected error during registration for user: {}", registrationRequest.getUsername(), e);
      model.addAttribute("errorMessage",
          "An unexpected error occurred. Please try again later.");
      return "register";
    }
  }


   // Build the API URL for internal REST calls

  private String buildApiUrl(String endpoint) {
    String baseUrl = "http://localhost:" + serverPort;
    if (contextPath != null && !contextPath.isEmpty()) {
      baseUrl += contextPath;
    }
    return baseUrl + endpoint;
  }
}
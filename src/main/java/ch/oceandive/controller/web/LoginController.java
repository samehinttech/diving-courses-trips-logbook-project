package ch.oceandive.controller.web;

import ch.oceandive.dto.ApiResponse;
import ch.oceandive.dto.LoginRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for handling user login functionality.
 * This controller provides methods to show the login form, process login requests,
 * and handle logout operations.
 */

@Controller
public class LoginController {

  private static final String PAGE_TITLE_LOGIN = "Login - OceanDive";
  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

  @Value("${server.port:8080}")
  private String serverPort;

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  private final RestTemplate restTemplate;
  private final JwtDecoder jwtDecoder;

  public LoginController(RestTemplate restTemplate, JwtDecoder jwtDecoder) {
    this.restTemplate = restTemplate;
    this.jwtDecoder = jwtDecoder;
  }


   // Show the login form

  @GetMapping("/login")
  public String showLoginForm(Model model) {
    model.addAttribute("loginRequest", new LoginRequest());
    model.addAttribute("pageTitle", PAGE_TITLE_LOGIN);
    return "login";
  }


   // Handle login form submission

  @PostMapping("/login")
  public String processLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
      BindingResult bindingResult,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    // If there are validation errors, return to the form
    if (bindingResult.hasErrors()) {
      logger.warn("Login form has validation errors for user: {}", loginRequest.getUsername());
      return "login";
    }

    try {
      // Call the REST API internally
      String apiUrl = buildApiUrl("/api/auth/user/login");

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
      ResponseEntity<ApiResponse> response = restTemplate.exchange(
          apiUrl,
          HttpMethod.POST,
          request,
          ApiResponse.class
      );

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().isSuccess()) {
        logger.info("User login successful for username: {}", loginRequest.getUsername());

        // Extract token information from response
        Object data = response.getBody().getData();
        if (data instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> tokenData = (Map<String, Object>) data;
          String accessToken = (String) tokenData.get("accessToken");
          String refreshToken = (String) tokenData.get("refreshToken");

          // Store tokens in session (for demonstration - in production consider more secure storage)
          session.setAttribute("accessToken", accessToken);
          session.setAttribute("refreshToken", refreshToken);
          session.setAttribute("username", loginRequest.getUsername());
          session.setAttribute("isAuthenticated", true);

          // Decode the JWT token to get user roles
          Jwt jwt = jwtDecoder.decode(accessToken);

          // Extract roles from the token using the standard Spring Security method
          List<String> roles = jwt.getClaimAsStringList("roles");

          if (roles != null && !roles.isEmpty()) {
              logger.debug("Extracted roles from token: {}", roles);

              // Convert roles to GrantedAuthority objects
              List<GrantedAuthority> authorities = roles.stream()
                  .map(SimpleGrantedAuthority::new)
                  .collect(Collectors.toList());

              // Create authentication object
              Authentication authentication = new JwtAuthenticationToken(jwt, authorities, loginRequest.getUsername());

              // Set authentication in security context
              SecurityContextHolder.getContext().setAuthentication(authentication);

              // Redirect based on role
              if (roles.contains("ROLE_ADMIN")) {
                  logger.info("Admin user detected, redirecting to admin dashboard");
                  redirectAttributes.addFlashAttribute("successMessage",
                      "Welcome back, Admin! You have been successfully logged in.");
                  return "redirect:/admin/dashboard";
              }
          } else {
              logger.warn("No roles found in JWT token for user: {}", loginRequest.getUsername());
          }
        }
        // Redirect to home page for non-admin users
        redirectAttributes.addFlashAttribute("successMessage",
            "Welcome back! You have been successfully logged in.");
        return "redirect:/";
      } else {
        logger.warn("Login API returned unexpected response for user: {}", loginRequest.getUsername());
        model.addAttribute("errorMessage", "Login failed. Please check your credentials.");
        return "login";
      }
    } catch (HttpClientErrorException e) {
      logger.error("Login failed for user: {} with status: {}",
          loginRequest.getUsername(), e.getStatusCode());

      // Handle specific error cases
      switch (e.getStatusCode()) {
        case HttpStatus.UNAUTHORIZED -> model.addAttribute("errorMessage",
            "Invalid username or password. Please try again.");
        case HttpStatus.FORBIDDEN -> model.addAttribute("errorMessage",
            "Access denied. Please check your account status.");
        case HttpStatus.TOO_MANY_REQUESTS -> model.addAttribute("errorMessage",
            "Too many failed login attempts. Please try again later.");
        default -> model.addAttribute("errorMessage",
            "Login failed. Please try again later.");
      }
      return "login";
    } catch (Exception e) {
      logger.error("Unexpected error during login for user: {}", loginRequest.getUsername(), e);
      model.addAttribute("errorMessage",
          "An unexpected error occurred. Please try again later.");
      return "login";
    }
  }

  /**
   * Handle logout
   */
  @PostMapping("/logout")
  public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
    try {
      String accessToken = (String) session.getAttribute("accessToken");
      if (accessToken != null) {
        // Call logout API to blacklist the token
        String apiUrl = buildApiUrl("/api/auth/logout");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
          restTemplate.exchange(apiUrl, HttpMethod.POST, request, ApiResponse.class);
        } catch (Exception e) {
          logger.warn("Error calling logout API, but continuing with session cleanup", e);
        }
      }
      // Clear session
      session.invalidate();
      redirectAttributes.addFlashAttribute("successMessage",
          "You have been successfully logged out.");
    } catch (Exception e) {
      logger.error("Error during logout process", e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Error during logout. Please close your browser for security.");
    }
    return "redirect:/";
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

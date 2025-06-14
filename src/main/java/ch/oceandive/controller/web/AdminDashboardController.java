package ch.oceandive.controller.web;

import ch.oceandive.dto.AdminDTO;
import ch.oceandive.dto.RegistrationRequest;
import ch.oceandive.exceptionHandler.DuplicateResourceException;
import ch.oceandive.exceptionHandler.ValidationException;
import ch.oceandive.model.Course;
import ch.oceandive.model.Trip;
import ch.oceandive.service.AdminService;
import ch.oceandive.service.CourseService;
import ch.oceandive.service.TripService;
import ch.oceandive.service.PremiumUserService;
import ch.oceandive.service.GuestUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin dashboard controller
 */
@Controller
public class AdminDashboardController {

  private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
  private static final int RECENT_ITEMS_LIMIT = 3;

  private final CourseService courseService;
  private final TripService tripService;
  private final PremiumUserService premiumUserService;
  private final GuestUserService guestUserService;
  private final AdminService adminService;

  public AdminDashboardController(CourseService courseService,
      TripService tripService,
      PremiumUserService premiumUserService,
      GuestUserService guestUserService,
      AdminService adminService) {
    this.courseService = courseService;
    this.tripService = tripService;
    this.premiumUserService = premiumUserService;
    this.guestUserService = guestUserService;
    this.adminService = adminService;
  }

  // Added for admin dashboard(Admin registration)
  @GetMapping("/admin-dashboard")
  @PreAuthorize("hasRole('ADMIN')")
  public String showDashboard(Model model) {
    // Registration form
    model.addAttribute("registrationRequest", new RegistrationRequest());

    // Load dashboard data
    loadDashboardData(model);
    return "admin-dashboard";
  }

  // Show the admin dashboard
  @GetMapping("/admin/dashboard")
  public String showDashboardAdmin(Model model) {
    return showDashboard(model);
  }

  // Process admin registration
  @PostMapping("/admin/register")
  public String processAdminRegistration(
      @Valid @ModelAttribute("registrationRequest") RegistrationRequest registrationRequest,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("adminRegisterError",
          "Please check all required fields and try again.");
      return "redirect:/admin-dashboard";
    }
    try {
      AdminDTO adminDto = registrationRequest.toAdminDTO();
      AdminDTO createdAdmin = adminService.createAdmin(adminDto);
      redirectAttributes.addFlashAttribute("adminRegisterSuccess",
          "Admin '" + createdAdmin.getUsername() + "' registered successfully!");
    } catch (DuplicateResourceException e) {
      redirectAttributes.addFlashAttribute("adminRegisterError",
          "Username or email already exists. Please choose different credentials.");
    } catch (ValidationException e) {
      redirectAttributes.addFlashAttribute("adminRegisterError",
          "Invalid registration data. Please check your information.");
    } catch (Exception e) {
      logger.error("Admin registration failed", e);
      redirectAttributes.addFlashAttribute("adminRegisterError",
          "Registration failed. Please try again later.");
    }
    return "redirect:/admin-dashboard";
  }

  // Load dashboard data
  private void loadDashboardData(Model model) {
    // Course data
    List<Course> allCourses = courseService.getAllCourses();
    List<Course> availableCourses = courseService.getAvailableCourses();
    List<Course> upcomingCourses = courseService.getUpcomingCourses();
    model.addAttribute("totalCourses", allCourses.size());
    model.addAttribute("availableCourses", availableCourses.size());
    model.addAttribute("upcomingCourses", upcomingCourses.size());
    model.addAttribute("recentCourses", getRecentCourses(allCourses));

    // Trip data
    List<Trip> allTrips = tripService.getAllTrips();
    List<Trip> availableTrips = tripService.getAvailableTrips();
    List<Trip> upcomingTrips = tripService.getUpcomingTrips();
    model.addAttribute("totalTrips", allTrips.size());
    model.addAttribute("availableTrips", availableTrips.size());
    model.addAttribute("upcomingTrips", upcomingTrips.size());
    model.addAttribute("recentTrips", getRecentTrips(allTrips));

    // User data
    Pageable userPageable = PageRequest.of(0, 1000);
    int premiumUsersCount = premiumUserService.getAllPremiumUsers(userPageable).getContent().size();
    int guestUsersCount = guestUserService.getAllGuestUsers(userPageable).getContent().size();
    int totalUsers = premiumUsersCount + guestUsersCount;
    model.addAttribute("totalUsers", totalUsers);
    model.addAttribute("premiumUsers", premiumUsersCount);
    model.addAttribute("guestUsers", guestUsersCount);

    // Booking data
    int courseBookings = calculateCourseBookings(allCourses);
    int tripBookings = calculateTripBookings(allTrips);
    int totalBookings = courseBookings + tripBookings;
    model.addAttribute("courseBookings", courseBookings);
    model.addAttribute("tripBookings", tripBookings);
    model.addAttribute("totalBookings", totalBookings);

    // Additional data
    model.addAttribute("totalOfferings", allCourses.size() + allTrips.size());
    model.addAttribute("pageTitle", "Admin Dashboard - OceanDive");
  }

  // Add the data to the homepage
  public void addHomepageData(Model model) {
    List<Course> featuredCourses = courseService.getAllCourses().stream()
        .filter(course -> course.getFeatured() != null && course.getFeatured())
        .limit(3)
        .toList();
    if (featuredCourses.isEmpty()) {
      featuredCourses = getRecentCourses(courseService.getAllCourses());
    }
    List<Trip> featuredTrips = getRecentTrips(tripService.getAllTrips());
    model.addAttribute("totalCourses", courseService.getAllCourses().size());
    model.addAttribute("totalTrips", tripService.getAllTrips().size());
    Pageable userPageable = PageRequest.of(0, 1000);
    int totalUsers = premiumUserService.getAllPremiumUsers(userPageable).getContent().size() +
        guestUserService.getAllGuestUsers(userPageable).getContent().size();
    model.addAttribute("totalUsers", totalUsers);
    model.addAttribute("featuredCourses", featuredCourses);
    model.addAttribute("featuredTrips", featuredTrips);
  }

  // Calculate total bookings for courses and trips
  private int calculateCourseBookings(List<Course> courses) {
    return courses.stream()
        .mapToInt(course -> course.getCurrentBookings() != null ? course.getCurrentBookings() : 0)
        .sum();
  }

  // Calculate total bookings for trips
  private int calculateTripBookings(List<Trip> trips) {
    return trips.stream()
        .mapToInt(trip -> trip.getCurrentBookings() != null ? trip.getCurrentBookings() : 0)
        .sum();
  }

  // Get recent courses to the dashboard
  private List<Course> getRecentCourses(List<Course> allCourses) {
    return allCourses.stream()
        .sorted((c1, c2) -> {
          if (c1.getCreatedAt() != null && c2.getCreatedAt() != null) {
            return c2.getCreatedAt().compareTo(c1.getCreatedAt());
          }
          return Long.compare(c2.getId(), c1.getId());
        })
        .limit(RECENT_ITEMS_LIMIT)
        .toList();
  }

  // Get recent trips to the dashboard
  private List<Trip> getRecentTrips(List<Trip> allTrips) {
    return allTrips.stream()
        .sorted((t1, t2) -> {
          if (t1.getCreatedAt() != null && t2.getCreatedAt() != null) {
            return t2.getCreatedAt().compareTo(t1.getCreatedAt());
          }
          return Long.compare(t2.getId(), t1.getId());
        })
        .limit(RECENT_ITEMS_LIMIT)
        .toList();
  }

  @GetMapping("/admin/dashboard-stats")
  public String getDashboardStats(Model model) {
    return "redirect:/admin-dashboard";
  }
}
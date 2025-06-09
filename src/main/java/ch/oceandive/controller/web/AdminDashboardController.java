package ch.oceandive.controller.web;

import ch.oceandive.model.Course;
import ch.oceandive.model.Trip;
import ch.oceandive.service.CourseService;
import ch.oceandive.service.TripService;
import ch.oceandive.service.PremiumUserService;
import ch.oceandive.service.GuestUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Enhanced admin dashboard controller that provides comprehensive statistics for courses, trips,
 * users, and bookings.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

  private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
  private static final int RECENT_ITEMS_LIMIT = 5;

  private final CourseService courseService;
  private final TripService tripService;
  private final PremiumUserService premiumUserService;
  private final GuestUserService guestUserService;

  public AdminDashboardController(CourseService courseService,
      TripService tripService,
      PremiumUserService premiumUserService,
      GuestUserService guestUserService) {
    this.courseService = courseService;
    this.tripService = tripService;
    this.premiumUserService = premiumUserService;
    this.guestUserService = guestUserService;
  }

  /**
   * Display the main admin dashboard with comprehensive statistics.
   */
  @GetMapping("/dashboard")
  public String showDashboard(Model model) {
    try {
      logger.info("Loading admin dashboard");

      // Course Statistics
      List<Course> allCourses = courseService.getAllCourses();
      List<Course> availableCourses = courseService.getAvailableCourses();
      List<Course> upcomingCourses = courseService.getUpcomingCourses();

      model.addAttribute("totalCourses", allCourses.size());
      model.addAttribute("availableCourses", availableCourses.size());
      model.addAttribute("upcomingCourses", upcomingCourses.size());

      // Trip Statistics
      List<Trip> allTrips = tripService.getAllTrips();
      List<Trip> availableTrips = tripService.getAvailableTrips();
      List<Trip> upcomingTrips = tripService.getUpcomingTrips();

      model.addAttribute("totalTrips", allTrips.size());
      model.addAttribute("availableTrips", availableTrips.size());
      model.addAttribute("upcomingTrips", upcomingTrips.size());

      // User Statistics
      Pageable userPageable = PageRequest.of(0, 1000); // Get a large page to count all users
      int premiumUsersCount = premiumUserService.getAllPremiumUsers(userPageable).getContent()
          .size();
      int guestUsersCount = guestUserService.getAllGuestUsers(userPageable).getContent().size();
      int totalUsers = premiumUsersCount + guestUsersCount;

      model.addAttribute("totalUsers", totalUsers);
      model.addAttribute("premiumUsers", premiumUsersCount);
      model.addAttribute("guestUsers", guestUsersCount);

      // Booking Statistics
      int courseBookings = calculateCourseBookings(allCourses);
      int tripBookings = calculateTripBookings(allTrips);
      int totalBookings = courseBookings + tripBookings;

      model.addAttribute("courseBookings", courseBookings);
      model.addAttribute("tripBookings", tripBookings);
      model.addAttribute("totalBookings", totalBookings);

      // Recent Activity - Get the most recently created items
      List<Course> recentCourses = getRecentCourses(allCourses);
      List<Trip> recentTrips = getRecentTrips(allTrips);

      model.addAttribute("recentCourses", recentCourses);
      model.addAttribute("recentTrips", recentTrips);

      // Additional Insights
      model.addAttribute("totalOfferings", allCourses.size() + allTrips.size());
      model.addAttribute("pageTitle", "Admin Dashboard - OceanDive");

      logger.info("Dashboard loaded successfully - Courses: {}, Trips: {}, Users: {}, Bookings: {}",
          allCourses.size(), allTrips.size(), totalUsers, totalBookings);

      return "admin/dashboard";

    } catch (Exception e) {
      logger.error("Error loading admin dashboard", e);
      model.addAttribute("error", "Error loading dashboard: " + e.getMessage());

      // Add default values to prevent template errors
      addDefaultModelAttributes(model);
      return "admin/dashboard"; // Redirect to a safe page
    }
  }

  /**
   * Calculate total current bookings for all courses.
   */
  private int calculateCourseBookings(List<Course> courses) {
    return courses.stream()
        .mapToInt(course -> course.getCurrentBookings() != null ? course.getCurrentBookings() : 0)
        .sum();
  }

  /**
   * Calculate total current bookings for all trips.
   */
  private int calculateTripBookings(List<Trip> trips) {
    return trips.stream()
        .mapToInt(trip -> trip.getCurrentBookings() != null ? trip.getCurrentBookings() : 0)
        .sum();
  }

  /**
   * Get the most recently created courses.
   */
  private List<Course> getRecentCourses(List<Course> allCourses) {
    return allCourses.stream()
        .filter(course -> course.getCreatedAt() != null)
        .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
        .limit(RECENT_ITEMS_LIMIT)
        .toList();
  }

  /**
   * Get the most recently created trips.
   */
  private List<Trip> getRecentTrips(List<Trip> allTrips) {
    return allTrips.stream()
        .filter(trip -> trip.getCreatedAt() != null)
        .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
        .limit(RECENT_ITEMS_LIMIT)
        .toList();
  }

  /**
   * Add default model attributes to prevent template errors.
   */
  private void addDefaultModelAttributes(Model model) {
    // Course defaults
    model.addAttribute("totalCourses", 0);
    model.addAttribute("availableCourses", 0);
    model.addAttribute("upcomingCourses", 0);

    // Trip defaults
    model.addAttribute("totalTrips", 0);
    model.addAttribute("availableTrips", 0);
    model.addAttribute("upcomingTrips", 0);

    // User defaults
    model.addAttribute("totalUsers", 0);
    model.addAttribute("premiumUsers", 0);
    model.addAttribute("guestUsers", 0);

    // Booking defaults
    model.addAttribute("courseBookings", 0);
    model.addAttribute("tripBookings", 0);
    model.addAttribute("totalBookings", 0);

    // Activity defaults
    model.addAttribute("recentCourses", List.of());
    model.addAttribute("recentTrips", List.of());

    // Other defaults
    model.addAttribute("totalOfferings", 0);
    model.addAttribute("pageTitle", "Admin Dashboard - OceanDive");
  }
  @GetMapping("/dashboard/stats")
  public String getDashboardStats(Model model) {
    try {
      return "redirect:/admin/dashboard";

    } catch (Exception e) {
      logger.error("Error loading dashboard stats", e);
      model.addAttribute("error", "Error loading statistics");
      return "admin/dashboard";
    }
  }
}
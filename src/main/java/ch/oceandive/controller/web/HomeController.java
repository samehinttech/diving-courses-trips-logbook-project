package ch.oceandive.controller.web;

import ch.oceandive.model.Course;
import ch.oceandive.model.Trip;
import ch.oceandive.service.CourseService;
import ch.oceandive.service.TripService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.time.Year;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * MVC Controller for handling web requests related to the home page and other static pages.
 */
@Controller
public class HomeController {

  // Constants for common attributes
  private static final String COMPANY_NAME = "OceanDive";
  private static final String PAGE_TITLE_HOME = "Home - Dive into Adventure";
  private static final String PAGE_TITLE_COURSES = "Dive Courses - OceanDive";
  private static final String PAGE_TITLE_ABOUT = "About - OceanDive";
  private static final String PAGE_TITLE_PRIVACY = "Privacy Policy - OceanDive";
  private static final String PAGE_TITLE_TERMS = "Terms & Conditions - OceanDive";
  private static final String PAGE_TITLE_LOGIN = "Login - OceanDive";
  private static final String PAGE_NOT_AVAILABLE  = "Not Available - OceanDive";

  // Constants for about page
  private static final String FOUNDED_YEAR = "2017";
  private static final String TEAM_SIZE = "25+";
  private static final String DIVERS_TRAINED = "2000+";

  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
  private final CourseService courseService;
  private final TripService tripService;
  private final AdminDashboardController adminDashboardController;

  public HomeController(CourseService courseService,
      TripService tripService,
      AdminDashboardController adminDashboardController) {
    this.courseService = courseService;
    this.tripService = tripService;
    this.adminDashboardController = adminDashboardController;
  }

  // For all pages if needed and mainly for the footer
  @ModelAttribute
  public void addCommonAttributes(Model model) {
    model.addAttribute("currentYear", Year.now().getValue());
    model.addAttribute("companyName", COMPANY_NAME);
  }

  @GetMapping("/")
  public String home(Model model) {
    try {
      logger.info("HomeController.home() - Loading homepage");
      // Initialize with empty lists to prevent null pointer exceptions
      List<Course> featuredCourses;
      List<Trip> featuredTrips;
      List<Trip> upcomingTrips;

      try {
        // Get featured courses for the homepage (limit to 3)
        // Note: The template shows only 3, but we fetch 6 to allow for future expansion
        // Futured means the course is highlighted to fire in the homepage
        featuredCourses = courseService.getFeaturedCourses(3);
        logger.info("HomeController: Retrieved {} featured courses", featuredCourses.size());
      } catch (Exception e) {
        logger.error("Error getting featured courses", e);
        featuredCourses = new ArrayList<>();
      }

      try {
        featuredTrips = tripService.getFeaturedTrips(6);
        logger.info("HomeController: Retrieved {} featured trips", featuredTrips.size());
      } catch (Exception e) {
        logger.error("Error getting featured trips", e);
        featuredTrips = new ArrayList<>();
      }

      try {
        // Get upcoming trips
        upcomingTrips = tripService.getUpcomingTrips();
        logger.info("HomeController: Retrieved {} upcoming trips", upcomingTrips.size());
      } catch (Exception e) {
        logger.error("Error getting upcoming trips", e);
        upcomingTrips = new ArrayList<>();
      }
      model.addAttribute("featuredCourses", featuredCourses);
      model.addAttribute("featuredTrips", featuredTrips);
      model.addAttribute("upcomingTrips", upcomingTrips.stream().limit(4).collect(Collectors.toList()));
      model.addAttribute("pageTitle", "OceanDive - Discover the Depths");

      // Add homepage data using the admin dashboard controller (for additional statistics)
      try {
        adminDashboardController.addHomepageData(model);
        logger.info("Added homepage data successfully");
      } catch (Exception e) {
        logger.error("Error adding homepage data", e);
        // Add safe defaults for stats
        model.addAttribute("totalCourses", 0);
        model.addAttribute("totalTrips", 0);
        model.addAttribute("totalUsers", 0);
      }

      // I used debug level for detailed information about the courses and trips
      logger.info("Featured courses count: {}", featuredCourses.size());
      for (Course course : featuredCourses) {
        logger.info("Course: {} - Featured: {} - ID: {} - Status: {}",
            course.getName(), course.getFeatured(), course.getId(), course.getStatus());
      }
      logger.info("Featured trips count: {}", featuredTrips.size());
      for (Trip trip : featuredTrips) {
        logger.info("Trip: {} - Featured: {} - ID: {}", trip.getLocation(), trip.getFeatured(), trip.getId());
      }
      logger.info("Homepage loaded successfully");
      return "index";

    } catch (Exception e) {
      logger.error("Critical error loading homepage", e);

      // Fallback - ensure all required attributes exist
      model.addAttribute("pageTitle", PAGE_TITLE_HOME);
      model.addAttribute("featuredCourses", new ArrayList<>());
      model.addAttribute("featuredTrips", new ArrayList<>());
      model.addAttribute("upcomingTrips", new ArrayList<>());
      model.addAttribute("totalCourses", 0);
      model.addAttribute("totalTrips", 0);
      model.addAttribute("totalUsers", 0);

      return "index";
    }
  }

  @GetMapping("/courses")
  public String coursesList(Model model) {
    try {
      // Get all available courses for public display
      List<Course> availableCourses = courseService.getAvailableCourses();
      model.addAttribute("courses", availableCourses);
      model.addAttribute("pageTitle", PAGE_TITLE_COURSES);
      model.addAttribute("pageDescription",
          "Browse our diving courses. From beginner Open Water to advanced certifications.");
      return "courses/list";
    } catch (Exception e) {
      logger.error("Error loading courses list", e);
      model.addAttribute("courses", Collections.emptyList());
      model.addAttribute("error", "Error loading courses. Please try again later.");
      return "courses/list";
    }
  }

  // Used slugs for future SEO optimization and better URLs plus IDs security
  @GetMapping("/courses/{slug}")
  public String courseDetail(@PathVariable String slug, Model model) {
    try {
      Optional<Course> courseOpt = courseService.getCourseBySlug(slug);
      if (courseOpt.isEmpty()) {
        logger.warn("Course not found with slug: {}", slug);
        return "redirect:/courses?error=course-not-found";
      }
      Course course = courseOpt.get();
      model.addAttribute("course", course);
      model.addAttribute("pageTitle", course.getName() + " - Diving Course - OceanDive");
      model.addAttribute("pageDescription", course.getShortDescription());
      return "courses-details";
    } catch (Exception e) {
      logger.error("Error loading course detail for slug: {}", slug, e);
      return "redirect:/courses?error=course-unavailable";
    }
  }

  @GetMapping("/about")
  public String about(Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_ABOUT);
    model.addAttribute("foundedYear", FOUNDED_YEAR);
    model.addAttribute("teamSize", TEAM_SIZE);
    model.addAttribute("diversTrained", DIVERS_TRAINED);
    return "about";
  }

  @GetMapping("/privacy")
  public String privacy(Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_PRIVACY);
    return "privacy";
  }

  @GetMapping("/terms")
  public String termsAndConditions(Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_TERMS);
    return "terms";
  }

  @GetMapping("/login")
  public String showLoginForm(Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_LOGIN);
    return "login";
  }
  @GetMapping("/not-available")
  public String notAvailable(Model model) {
    model.addAttribute("pageTitle", PAGE_NOT_AVAILABLE);
    return "not-available";
  }
}
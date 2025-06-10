package ch.oceandive.controller.web;

import ch.oceandive.model.Course;
import ch.oceandive.service.CourseService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
 * This controller provides methods to render the home, courses, trips, about, privacy policy,
 * terms and conditions, and 404 error pages.
 * @NOTE: Simply it handles routing for static pages.
 */
@Controller
public class HomeController {

  // Constants for common attributes
  private static final String COMPANY_NAME = "OceanDive";
  private static final String PAGE_TITLE_HOME = "Home - Dive into Adventure";
  private static final String PAGE_TITLE_COURSES = "Dive Courses - OceanDive";
  private static final String PAGE_TITLE_TRIPS = "Dive Trips - OceanDive";
  private static final String PAGE_TITLE_ABOUT = "About - OceanDive";
  private static final String PAGE_TITLE_PRIVACY = "Privacy Policy - OceanDive";
  private static final String PAGE_TITLE_TERMS = "Terms & Conditions - OceanDive";
  private static final String PAGE_TITLE_LOGIN = "Login - OceanDive";
  private static final String PAGE_TITLE_404 = "Page Not Found - OceanDive";

  // Constants for about page
  private static final String FOUNDED_YEAR = "2017";
  private static final String TEAM_SIZE = "25+";
  private static final String DIVERS_TRAINED = "2000+";

  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
  private final CourseService courseService;

  public HomeController(CourseService courseService) {
    this.courseService = courseService;
  }

  // For all pages if needed and footer
  @ModelAttribute
  public void addCommonAttributes(Model model) {
    model.addAttribute("currentYear", Year.now().getValue());
    model.addAttribute("companyName", COMPANY_NAME);
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_HOME);
    try {
      // Get featured courses for the homepage (limit to 3-4 cards)
      List<Course> featuredCourses = courseService.getFeaturedCourses(4);
      // Get upcoming available courses if no featured courses
      if (featuredCourses.isEmpty()) {
        featuredCourses = courseService.getAvailableCourses()
            .stream()
            .limit(4)
            .toList();
        logger.info("No courses found, showing {} available courses", featuredCourses.size());
      } else {
        logger.info("Displaying {} featured courses on homepage", featuredCourses.size());
      }
      // Add courses
      model.addAttribute("featuredCourses", featuredCourses);
      model.addAttribute("availableSpots", courseService.getAvailableSpotsCount());

      // Page metadata
      model.addAttribute("pageDescription",
          "Experience the underwater world with our professional diving courses and guided adventures. " +
              "From beginners to advanced divers, we have the perfect certification program for you.");
      return "index";

    } catch (Exception e) {
      logger.error("Error loading homepage data", e);
      // Return homepage with empty lists if there's an error
      model.addAttribute("featuredCourses", Collections.emptyList());
      model.addAttribute("totalPublishedCourses", 0L);
      model.addAttribute("availableSpots", 0L);
      return "index";
    }
  }
  @GetMapping("/courses")
  public String coursesList(Model model) {
    try {
      // Get all available courses for public display
      List<Course> availableCourses = courseService.getAvailableCourses();

      model.addAttribute("courses", availableCourses);
      model.addAttribute("pageTitle",PAGE_TITLE_COURSES);
      model.addAttribute("pageDescription",
          "Browse our comprehensive diving certification courses. From beginner Open Water to advanced certifications.");

      return "courses/list";

    } catch (Exception e) {
      logger.error("Error loading courses list", e);
      model.addAttribute("courses", Collections.emptyList());
      model.addAttribute("error", "Error loading courses. Please try again later.");
      return "courses/list";
    }
  }
  // Individual course detail page, serve SEO-friendly URLs
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

  // This method was removed to resolve ambiguous mapping with coursesList method
  @GetMapping("/trips")
  public String trips(Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_TRIPS);
    return "trips";
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

  @GetMapping("/404")
  public String notFound(Model model) {
    model.addAttribute("pageTitle", PAGE_TITLE_404);
    return "404";
  }
}

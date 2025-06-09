package ch.oceandive.controller.web;

import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.CourseStatus;
import ch.fhnw.oceandive.model.DiveCertification;
import ch.fhnw.oceandive.service.CourseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Web-based admin controller for managing courses
 */
@Controller
@RequestMapping("/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

  private static final Logger logger = LoggerFactory.getLogger(AdminCourseController.class);
  private static final int DEFAULT_PAGE_SIZE = 10;

  private final CourseService courseService;

  public AdminCourseController(CourseService courseService) {
    this.courseService = courseService;
  }

  /**
   * Display a list of all courses with search and pagination.
   */
  @GetMapping
  public String listCourses(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE + "") int size,
      @RequestParam(required = false) String search,
      Model model) {

    try {
      Pageable pageable = PageRequest.of(page, size);
      Page<Course> coursePage;

      if (search != null && !search.trim().isEmpty()) {
        coursePage = courseService.searchCoursesForAdmin(search.trim(), pageable);
        model.addAttribute("search", search);
      } else {
        coursePage = courseService.getAllCoursesForAdmin(pageable);
      }

      model.addAttribute("coursePage", coursePage);
      model.addAttribute("courses", coursePage.getContent());
      model.addAttribute("currentPage", page);
      model.addAttribute("totalPages", coursePage.getTotalPages());
      model.addAttribute("totalElements", coursePage.getTotalElements());
      model.addAttribute("pageSize", size);

      // Add summary statistics
      model.addAttribute("publishedCount", courseService.getPublishedCourseCount());
      model.addAttribute("availableSpots", courseService.getAvailableSpotsCount());

      model.addAttribute("pageTitle", "Course Management - Admin Dashboard");

      return "admin/courses/list";

    } catch (Exception e) {
      logger.error("Error loading courses list", e);
      model.addAttribute("error", "Error loading courses: " + e.getMessage());
      return "admin/courses/list";
    }
  }

  /**
   * Show form to create a new course.
   */
  @GetMapping("/create")
  public String showCreateForm(Model model) {
    Course course = new Course();
    // Set default values
    course.setStatus(CourseStatus.DRAFT);
    course.setFeatured(false);
    course.setDisplayOrder(0);
    course.setStartDate(LocalDate.now().plusDays(30)); // Default start date
    course.setEndDate(LocalDate.now().plusDays(33));   // Default 3-day course

    model.addAttribute("course", course);
    model.addAttribute("certifications", DiveCertification.values());
    model.addAttribute("statuses", CourseStatus.values());
    model.addAttribute("pageTitle", "Create New Course - Admin Dashboard");
    model.addAttribute("isEdit", false);

    return "admin/courses/form";
  }

  /**
   * Process course creation.
   */
  @PostMapping("/create")
  public String createCourse(
      @Valid @ModelAttribute("course") Course course,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {

    try {
      if (result.hasErrors()) {
        model.addAttribute("certifications", DiveCertification.values());
        model.addAttribute("statuses", CourseStatus.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Create New Course - Admin Dashboard");
        return "admin/courses/form";
      }

      Course createdCourse = courseService.createCourseEnhanced(course);

      logger.info("Admin created new course: {} (ID: {})",
          createdCourse.getName(), createdCourse.getId());

      redirectAttributes.addFlashAttribute("successMessage",
          "Course '" + createdCourse.getName() + "' created successfully!");

      return "redirect:/admin/courses";

    } catch (Exception e) {
      logger.error("Error creating course", e);
      model.addAttribute("error", "Error creating course: " + e.getMessage());
      model.addAttribute("certifications", DiveCertification.values());
      model.addAttribute("statuses", CourseStatus.values());
      model.addAttribute("isEdit", false);
      model.addAttribute("pageTitle", "Create New Course - Admin Dashboard");
      return "admin/courses/form";
    }
  }

  /**
   * Show form to edit an existing course.
   */
  @GetMapping("/edit/{id}")
  public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    try {
      Course course = courseService.getCourseById(id);

      model.addAttribute("course", course);
      model.addAttribute("certifications", DiveCertification.values());
      model.addAttribute("statuses", CourseStatus.values());
      model.addAttribute("pageTitle", "Edit Course: " + course.getName());
      model.addAttribute("isEdit", true);

      return "admin/courses/form";

    } catch (Exception e) {
      logger.error("Error loading course for edit: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Course not found or error loading course details.");
      return "redirect:/admin/courses";
    }
  }

  /**
   * Process course update.
   */
  @PostMapping("/edit/{id}")
  public String updateCourse(
      @PathVariable Long id,
      @Valid @ModelAttribute("course") Course course,
      BindingResult result,
      RedirectAttributes redirectAttributes,
      Model model) {

    try {
      if (result.hasErrors()) {
        model.addAttribute("certifications", DiveCertification.values());
        model.addAttribute("statuses", CourseStatus.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Edit Course");
        return "admin/courses/form";
      }

      Course updatedCourse = courseService.updateCourseEnhanced(id, course);

      logger.info("Admin updated course: {} (ID: {})",
          updatedCourse.getName(), updatedCourse.getId());

      redirectAttributes.addFlashAttribute("successMessage",
          "Course '" + updatedCourse.getName() + "' updated successfully!");

      return "redirect:/admin/courses";

    } catch (Exception e) {
      logger.error("Error updating course: {}", id, e);
      model.addAttribute("error", "Error updating course: " + e.getMessage());
      model.addAttribute("certifications", DiveCertification.values());
      model.addAttribute("statuses", CourseStatus.values());
      model.addAttribute("isEdit", true);
      model.addAttribute("pageTitle", "Edit Course");
      return "admin/courses/form";
    }
  }

  /**
   * View course details.
   */
  @GetMapping("/view/{id}")
  public String viewCourse(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    try {
      Course course = courseService.getCourseById(id);

      model.addAttribute("course", course);
      model.addAttribute("pageTitle", "Course Details: " + course.getName());

      return "admin/courses/view";

    } catch (Exception e) {
      logger.error("Error loading course details: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Course not found or error loading course details.");
      return "redirect:/admin/courses";
    }
  }

  /**
   * Delete a course (with confirmation).
   */
  @PostMapping("/delete/{id}")
  public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      Course course = courseService.getCourseById(id);
      String courseName = course.getName();

      // Check if course has bookings
      if (course.getCurrentBookings() > 0) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "Cannot delete course '" + courseName + "' because it has " +
                course.getCurrentBookings() + " active bookings.");
        return "redirect:/admin/courses";
      }

      courseService.deleteCourse(id);

      logger.info("Admin deleted course: {} (ID: {})", courseName, id);

      redirectAttributes.addFlashAttribute("successMessage",
          "Course '" + courseName + "' deleted successfully!");

      return "redirect:/admin/courses";

    } catch (Exception e) {
      logger.error("Error deleting course: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Error deleting course: " + e.getMessage());
      return "redirect:/admin/courses";
    }
  }

  /**
   * Toggle featured status via AJAX or redirect.
   */
  @PostMapping("/toggle-featured/{id}")
  public String toggleFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      Course course = courseService.toggleFeaturedStatus(id);

      String message = course.getFeatured()
          ? "Course '" + course.getName() + "' is now featured on homepage."
          : "Course '" + course.getName() + "' removed from featured courses.";

      redirectAttributes.addFlashAttribute("successMessage", message);

      return "redirect:/admin/courses";

    } catch (Exception e) {
      logger.error("Error toggling featured status: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Error updating course status: " + e.getMessage());
      return "redirect:/admin/courses";
    }
  }

  /**
   * Change course status.
   */
  @PostMapping("/change-status/{id}")
  public String changeStatus(
      @PathVariable Long id,
      @RequestParam CourseStatus status,
      RedirectAttributes redirectAttributes) {

    try {
      Course course = courseService.changeStatus(id, status);

      redirectAttributes.addFlashAttribute("successMessage",
          "Course '" + course.getName() + "' status changed to " + status.getDisplayName());

      return "redirect:/admin/courses";

    } catch (Exception e) {
      logger.error("Error changing course status: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Error changing course status: " + e.getMessage());
      return "redirect:/admin/courses";
    }
  }

  /**
   * Bulk operations (future enhancement).
   */
  @PostMapping("/bulk-action")
  public String bulkAction(
      @RequestParam("action") String action,
      @RequestParam("courseIds") Long[] courseIds,
      RedirectAttributes redirectAttributes) {

    try {
      int successCount = 0;

      switch (action) {
        case "publish":
          for (Long id : courseIds) {
            courseService.changeStatus(id, CourseStatus.PUBLISHED);
            successCount++;
          }
          redirectAttributes.addFlashAttribute("successMessage",
              successCount + " courses published successfully!");
          break;

        case "archive":
          for (Long id : courseIds) {
            courseService.changeStatus(id, CourseStatus.ARCHIVED);
            successCount++;
          }
          redirectAttributes.addFlashAttribute("successMessage",
              successCount + " courses archived successfully!");
          break;

        case "feature":
          for (Long id : courseIds) {
            Course course = courseService.getCourseById(id);
            if (!course.getFeatured()) {
              courseService.toggleFeaturedStatus(id);
              successCount++;
            }
          }
          redirectAttributes.addFlashAttribute("successMessage",
              successCount + " courses added to featured list!");
          break;

        default:
          redirectAttributes.addFlashAttribute("errorMessage",
              "Unknown bulk action: " + action);
      }

      return "redirect:/admin/courses";

    } catch (Exception e) {
      logger.error("Error performing bulk action: {}", action, e);
      redirectAttributes.addFlashAttribute("errorMessage",
          "Error performing bulk action: " + e.getMessage());
      return "redirect:/admin/courses";
    }
  }

  /**
   * Course statistics dashboard.
   */
  @GetMapping("/stats")
  public String courseStats(Model model) {
    try {
      // Add statistics data to model
      model.addAttribute("totalCourses", courseService.getAllCourses().size());
      model.addAttribute("publishedCourses", courseService.getPublishedCourseCount());
      model.addAttribute("availableSpots", courseService.getAvailableSpotsCount());
      model.addAttribute("upcomingCourses", courseService.getUpcomingCourses().size());

      model.addAttribute("pageTitle", "Course Statistics - Admin Dashboard");

      return "admin/courses/stats";

    } catch (Exception e) {
      logger.error("Error loading course statistics", e);
      model.addAttribute("error", "Error loading statistics: " + e.getMessage());
      return "admin/courses/stats";
    }
  }
}
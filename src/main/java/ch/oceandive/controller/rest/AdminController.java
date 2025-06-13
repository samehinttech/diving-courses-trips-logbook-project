package ch.oceandive.controller.rest;

import ch.oceandive.dto.Response;
import ch.oceandive.model.Course;
import ch.oceandive.model.Trip;
import ch.oceandive.service.AdminService;
import ch.oceandive.service.CourseService;
import ch.oceandive.service.GuestUserService;
import ch.oceandive.service.PremiumUserService;
import ch.oceandive.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

  private final AdminService adminService;
  private final CourseService courseService;
  private final TripService tripService;
  private final PremiumUserService premiumUserService;
  private final GuestUserService guestUserService;

  public AdminController(AdminService adminService, CourseService courseService,
      TripService tripService, PremiumUserService premiumUserService,
      GuestUserService guestUserService) {
    this.adminService = adminService;
    this.courseService = courseService;
    this.tripService = tripService;
    this.premiumUserService = premiumUserService;
    this.guestUserService = guestUserService;
  }
//====================== Admin operations for managing courses, trips, and users ======================
  // Add dive course
  @PostMapping("/add-course")
  public ResponseEntity<Response> createCourse(@Valid @RequestBody Course course,
      Authentication authentication) {
    try {
      String adminUsername = authentication.getName();
      logger.info("{} created a new course: {}", adminUsername, course.getName());
      Course createdCourse = courseService.createCourse(course);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new Response(true, "Course created successfully", createdCourse));
    } catch (Exception e) {
      logger.error("Error creating course", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new Response(false, "Error creating course: " + e.getMessage(), null));
    }
  }
// Add a dive trip
  @PostMapping("/add-trip")
  public ResponseEntity<Response> createTrip(@Valid @RequestBody Trip trip,
      Authentication authentication) {
    try {
      String adminUsername = authentication.getName();
      logger.info("Admin {} is creating a new trip: {}", adminUsername, trip.getLocation());
      Trip createdTrip = tripService.createTrip(trip);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new Response(true, "Trip created successfully", createdTrip));
    } catch (Exception e) {
      logger.error("Error creating trip", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new Response(false, "Error creating trip: " + e.getMessage(), null));
    }
  }
// View all users with access to premium and guest users
  @GetMapping("/view-users")
  @Operation(summary = "Get all users with pagination")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Response> getAllUsers(
      @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
    try {
      Pageable pageable = PageRequest.of(page, size);
      Map<String, Object> response = new HashMap<>();
      response.put("premiumUsers", premiumUserService.getAllPremiumUsers(pageable));
      response.put("guestUsers", guestUserService.getAllGuestUsers(pageable));
      response.put("currentPage", page);
      response.put("pageSize", size);
      return ResponseEntity.ok(new Response(true, "Users retrieved successfully", response));
    } catch (Exception e) {
      logger.error("Error retrieving users", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new Response(false, "Error retrieving users: " + e.getMessage(), null));
    }
  }
 // View all booking information for courses and trips
  @GetMapping("/view-bookings")
  @Operation(summary = "Get all bookings information")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Booking information retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Response> getBookingInformation(
      @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
    try {
      Pageable pageable = PageRequest.of(page, size);
      Map<String, Object> response = new HashMap<>();
      response.put("courses", courseService.findAll(pageable));
      response.put("trips", tripService.getAllTrips(pageable));
      response.put("currentPage", page);
      response.put("pageSize", size);
      return ResponseEntity.ok(
          new Response(true, "Booking information retrieved successfully", response));
    } catch (Exception e) {
      logger.error("Error retrieving booking information", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new Response(false, "Error retrieving booking information: " + e.getMessage(),
              null));
    }
  }
}
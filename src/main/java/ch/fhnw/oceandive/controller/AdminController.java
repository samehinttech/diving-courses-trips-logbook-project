package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.dto.AdminDTO;
import ch.fhnw.oceandive.dto.ApiResponse;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.service.AdminService;
import ch.fhnw.oceandive.service.CourseService;
import ch.fhnw.oceandive.service.GuestUserService;
import ch.fhnw.oceandive.service.PremiumUserService;
import ch.fhnw.oceandive.service.TripService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for admin operations.
 * All endpoints in this controller require ADMIN role.
 */
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

    /**
     * POST /api/admin/add-course: Create a new course.
     * Requires an ADMIN role.
     *
     * @param course the course to create
     * @return the ResponseEntity with status 201 (Created) and the created course in body
     */
    @PostMapping("/add-course")
    public ResponseEntity<ApiResponse> createCourse(@Valid @RequestBody Course course, Authentication authentication) {
        try {
            String adminUsername = authentication.getName();
            logger.info("Admin {} is creating a new course: {}", adminUsername, course.getName());
            
            Course createdCourse = courseService.createCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Course created successfully", createdCourse));
        } catch (Exception e) {
            logger.error("Error creating course", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error creating course: " + e.getMessage(), null));
        }
    }

    /**
     * POST /api/admin/add-trip: Create a new trip.
     * Requires an ADMIN role.
     * @return the ResponseEntity with status 201 (Created) and the created trip in body
     */
    @PostMapping("/add-trip")
    public ResponseEntity<ApiResponse> createTrip(@Valid @RequestBody Trip trip, Authentication authentication) {
        try {
            String adminUsername = authentication.getName();
            logger.info("Admin {} is creating a new trip: {}", adminUsername, trip.getLocation());
            
            Trip createdTrip = tripService.createTrip(trip);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Trip created successfully", createdTrip));
        } catch (Exception e) {
            logger.error("Error creating trip", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error creating trip: " + e.getMessage(), null));
        }
    }

    /**
     * GET /api/admin/view-users : Get all users (premium and guest) with pagination.
     * Requires ADMIN role.
     * @param page Page number (0-based)
     * @param size Page size
     * @return the ResponseEntity with status 200 (OK) and a map containing lists of premium and guest users
     */
    @GetMapping("/view-users")
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("premiumUsers", premiumUserService.getAllPremiumUsers(pageable));
            response.put("guestUsers", guestUserService.getAllGuestUsers(pageable));
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(new ApiResponse(true, "Users retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error retrieving users: " + e.getMessage(), null));
        }
    }

    /**
     * GET /api/admin/view-bookings: Get all courses and trips with their booking status.
     * Requires an ADMIN role.
     * @param page Page number (0-based)
     * @param size Page size
     * @return the ResponseEntity with status 200 (OK) and information about courses and trips
     */
    @GetMapping("/view-bookings")
    public ResponseEntity<ApiResponse> getBookingInformation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("courses", courseService.findAll(pageable));
            response.put("trips", tripService.getAllTrips(pageable));
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(new ApiResponse(true, "Booking information retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Error retrieving booking information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error retrieving booking information: " + e.getMessage(), null));
        }
    }

    /**
     * POST /api/admin/add-admin : Create a new admin.
     * Requires ADMIN role.
     * @return the ResponseEntity with status 201 (Created) and the created admin in body
     */
    @PostMapping("/add-admin")
    public ResponseEntity<ApiResponse> createAdmin(@Valid @RequestBody AdminDTO adminDTO, Authentication authentication) {
        try {
            String adminUsername = authentication.getName();
            logger.info("Admin {} is creating a new admin: {}", adminUsername, adminDTO.getUsername());
            
            AdminDTO createdAdmin = adminService.createAdmin(adminDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Admin created successfully", createdAdmin));
        } catch (Exception e) {
            logger.error("Error creating admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error creating admin: " + e.getMessage(), null));
        }
    }
}
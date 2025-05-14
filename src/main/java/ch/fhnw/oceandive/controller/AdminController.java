package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.dto.AdminDTO;
import ch.fhnw.oceandive.model.Course;
import ch.fhnw.oceandive.model.Trip;
import ch.fhnw.oceandive.service.AdminService;
import ch.fhnw.oceandive.service.BookingService;
import ch.fhnw.oceandive.service.CourseService;
import ch.fhnw.oceandive.service.GuestUserService;
import ch.fhnw.oceandive.service.PremiumUserService;
import ch.fhnw.oceandive.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private final AdminService adminService;
    private final CourseService courseService;
    private final TripService tripService;
    private final PremiumUserService premiumUserService;
    private final GuestUserService guestUserService;
    private final BookingService bookingService;

    @Autowired
    public AdminController(AdminService adminService, CourseService courseService,
                          TripService tripService, PremiumUserService premiumUserService,
                          GuestUserService guestUserService, BookingService bookingService) {
        this.adminService = adminService;
        this.courseService = courseService;
        this.tripService = tripService;
        this.premiumUserService = premiumUserService;
        this.guestUserService = guestUserService;
        this.bookingService = bookingService;
    }

    /**
     * POST /api/admin/add-course: Create a new course.
     * Requires an ADMIN role.
     *
     * @param course the course to create
     * @return the ResponseEntity with status 201 (Created) and the created course in body
     */
    @PostMapping("/add-course")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course createdCourse = courseService.createCourse(course);
        return ResponseEntity.status(201).body(createdCourse);
    }

    /**
     * POST /api/admin/add-trip: Create a new trip.
     * Requires an ADMIN role.
     * @return the ResponseEntity with status 201 (Created) and the created trip in body
     */
    @PostMapping("/add-trip")
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        Trip createdTrip = tripService.createTrip(trip);
        return ResponseEntity.status(201).body(createdTrip);
    }

    /**
     * GET /api/admin/view-users : Get all users (premium and guest).
     * Requires ADMIN role.
     * @return the ResponseEntity with status 200 (OK) and a map containing lists of premium and guest users
     */
    @GetMapping("/view-users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("premiumUsers", premiumUserService.getAllPremiumUsers());
        response.put("guestUsers", guestUserService.getAllGuestUsers());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/view-bookings: Get all courses and trips with their booking status.
     * Requires an ADMIN role.
     * @return the ResponseEntity with status 200 (OK) and information about courses and trips
     */
    @GetMapping("/view-bookings")
    public ResponseEntity<Map<String, Object>> getBookingInformation() {
        Map<String, Object> response = new HashMap<>();
        response.put("courses", courseService.getAllCourses());
        response.put("trips", tripService.getAllTrips());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/add-admin : Create a new admin.
     * Requires ADMIN role.
     * @return the ResponseEntity with status 201 (Created) and the created admin in body
     */
    @PostMapping("/add-admin")
    public ResponseEntity<AdminDTO> createAdmin(@RequestBody AdminDTO adminDTO) {
        AdminDTO createdAdmin = adminService.createAdmin(adminDTO);
        return ResponseEntity.status(201).body(createdAdmin);
    }
}

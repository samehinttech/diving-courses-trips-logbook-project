# OceanDive System Architecture Documentation

## 1. System Overview

OceanDive is a comprehensive diving management application that allows users to manage dive activities, bookings, dive logs, and userEntity profiles. The system is built using Spring Boot and follows a layered architecture pattern with clear separation of concerns.

## 2. Architecture Layers

The application follows a standard layered architecture:

1. **Presentation Layer** - REST Controllers that expose APIs
2. **Business Logic Layer** - Services that implement business logic
3. **Data Access Layer** - Repositories that interact with the database
4. **Domain Model Layer** - Entities that represent the domain model

## 3. Key Components

### 3.1 Domain Model

The core domain entities in the system are:

- **User** - Represents a userEntity in the system with authentication details and profile information
- **Role** - Represents a role that can be assigned to users (e.g., USER, ADMIN)
- **Activity** - Represents a diving activity that can be booked
- **Booking** - Represents a booking made by a userEntity for an activity
- **DiveLog** - Represents a log of a dive completed by a userEntity
- **RefreshToken** - Used for authentication refresh tokens

### 3.2 Controllers

The application exposes the following REST APIs through controllers:

- **AuthController** - Handles authentication operations (login, register, refresh token, logout)
- **UserController** - Handles userEntity profile operations
- **AdminUserController** - Handles admin-specific userEntity operations
- **UserRoleController** - Handles userEntity role management
- **ActivityController** - Handles activity management
- **BookingController** - Handles booking management
- **DiveLogController** - Handles dive log management

### 3.3 Services

The business logic is implemented in the following services:

- **UserService** - Implements userEntity-related business logic
- **ActivityService** - Implements activity-related business logic
- **BookingService** - Implements booking-related business logic
- **DiveLogService** - Implements dive log-related business logic
- **TokenService** - Implements token-related business logic
- **CustomUserDetailsService** - Implements userEntity details loading for authentication

### 3.4 Repositories

Data access is handled by the following repositories:

- **UserRepository** - Data access for users
- **RoleRepository** - Data access for roles
- **ActivityRepository** - Data access for activities
- **BookingRepository** - Data access for bookings
- **DiveLogRepository** - Data access for dive logs
- **RefreshTokenRepository** - Data access for refresh tokens

### 3.5 Security Components

Security is implemented using Spring Security with JWT authentication:

- **SecurityConfig** - Configures security settings
- **JwtAuthenticationFilter** - Filters requests to authenticate using JWT
- **TokenService** - Handles JWT token generation and validation
- **CustomUserDetails** - Implements UserDetails for Spring Security
- **CustomUserDetailsService** - Loads userEntity details for authentication

## 4. Key Relationships and Flows

### 4.1 Authentication Flow

1. **Registration**:
   - Client sends a registration request to `/api/auth/register`
   - `AuthController` validates the request and creates a new userEntity
   - User is saved in the database with a USER role

2. **Login**:
   - Client sends login credentials to `/api/auth/login`
   - `AuthController` authenticates the userEntity using `AuthenticationManager`
   - If authentication is successful, `TokenService` generates an access token
   - A refresh token is created and stored in the database
   - The access token is returned in the response body
   - The refresh token is set as an HTTP-only cookie

3. **Token Refresh**:
   - When the access token expires, client sends a request to `/api/auth/refresh`
   - `AuthController` extracts the refresh token from the cookie
   - `TokenService` validates the refresh token
   - If valid, a new access token is generated
   - The new access token is returned in the response

4. **Logout**:
   - Client sends a request to `/api/auth/logout`
   - `AuthController` extracts the refresh token from the cookie
   - `TokenService` deletes the refresh token from the database
   - The refresh token cookie is cleared

### 4.2 Activity and Booking Flow

1. **Activity Creation**:
   - Admin creates a new activity through `/api/activities`
   - `ActivityController` validates the request and creates a new activity
   - Activity is saved in the database

2. **Activity Booking**:
   - User views available activities through `/api/activities`
   - User books an activity through `/api/bookings`
   - `BookingController` validates the request and creates a new booking
   - Booking is saved in the database

3. **Dive Log Creation**:
   - After completing a dive, userEntity creates a dive log through `/api/divelogs`
   - `DiveLogController` validates the request and creates a new dive log
   - Dive log is saved in the database

### 4.3 User Management Flow

1. **User Profile Management**:
   - User views their profile through `/api/users/profile`
   - User updates their profile through `/api/users/profile`
   - `UserController` validates the request and updates the userEntity
   - Updated userEntity is saved in the database

2. **User Role Management**:
   - Admin assigns roles to users through `/api/userEntity-roles`
   - `UserRoleController` validates the request and updates the userEntity's roles
   - Updated userEntity is saved in the database

## 5. Security Implementation

### 5.1 Authentication

The application uses JWT (JSON Web Token) for authentication:

1. **JWT Generation**:
   - When a userEntity logs in, a JWT is generated with the userEntity's details and roles
   - The JWT is signed with a secret key
   - The JWT has an expiration time (1 hour by default)

   ```java
   // From TokenService.java
   public String generateAccessToken(Authentication authentication) {
     Instant now = Instant.now();

     String scope = authentication.getAuthorities().stream()
         .map(GrantedAuthority::getAuthority)
         .collect(Collectors.joining(" "));

     JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
         .issuer("oceandive")
         .issuedAt(now)
         .expiresAt(now.plus(jwtExpirationMs, ChronoUnit.MILLIS))
         .subject(authentication.getName())
         .claim("scope", scope);

     // Add created_at if available (for composite key support)
     if (authentication.getPrincipal() instanceof CustomUserDetails) {
       CustomUserDetails userDetails;
       userDetails = (CustomUserDetails) authentication.getPrincipal();
       if (userDetails.getCreatedAt() != null) {
         claimsBuilder.claim("created_at",
             userDetails.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
       }
     }

     JwtClaimsSet claims = claimsBuilder.build();

     return this.encoder.encode(
         JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS512).build(), claims)
     ).getTokenValue();
   }
   ```

2. **JWT Validation**:
   - For protected endpoints, the `JwtAuthenticationFilter` extracts the JWT from the Authorization header
   - The filter validates the JWT signature and expiration
   - If valid, the userEntity is authenticated and the request proceeds

   ```java
   // From JwtAuthenticationFilter.java
   @Override
   protected void doFilterInternal(
       @NonNull HttpServletRequest request,
       @NonNull HttpServletResponse response,
       @NonNull FilterChain filterChain) throws ServletException, IOException {

     // Skip filter for auth endpoints
     final String requestPath = request.getServletPath();
     if (requestPath.startsWith("/api/auth")) {
       filterChain.doFilter(request, response);
       return;
     }

     final String authHeader = request.getHeader("Authorization");
     if (authHeader == null || !authHeader.startsWith("Bearer ")) {
       filterChain.doFilter(request, response);
       return;
     }

     try {
       final String jwt = authHeader.substring(7);
       final String username = tokenService.extractUsername(jwt);

       if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
         LocalDateTime createdAt = tokenService.extractCreatedAt(jwt);

         UserDetails userDetails;
         if (createdAt != null) {
           userDetails = userDetailsService.loadUserByCompositeKey(username, createdAt);
         } else {
           userDetails = userDetailsService.loadUserByUsername(username);
         }

         UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
             userDetails,
             null,
             userDetails.getAuthorities()
         );

         authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
         SecurityContextHolder.getContext().setAuthentication(authToken);
       }
     } catch (Exception e) {
       // Log error but continue - don't throw exception to avoid revealing info
       logger.error("JWT Authentication error", e);
     }

     filterChain.doFilter(request, response);
   }
   ```

3. **Refresh Token**:
   - Refresh tokens are used to obtain new access tokens without re-authentication
   - Refresh tokens are stored in the database and have a longer expiration time (24 hours by default)
   - Refresh tokens are sent as HTTP-only cookies for security

   ```java
   // From TokenService.java - Creating a refresh token
   public RefreshToken createRefreshToken(String username) {
     User userEntity = userRepository.findByIdUsername(username)
         .orElseThrow(() -> new RuntimeException("UserEntity not found"));

     // Delete any existing refresh tokens for this userEntity
     refreshTokenRepository.deleteByUser(userEntity);

     RefreshToken refreshToken = new RefreshToken();
     refreshToken.setUser(userEntity);
     refreshToken.setToken(UUID.randomUUID().toString());
     refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));

     return refreshTokenRepository.save(refreshToken);
   }

   // From AuthController.java - Setting refresh token as HTTP-only cookie
   @PostMapping("/login")
   public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
     try {
       Authentication authentication = authenticationManager.authenticate(
           new UsernamePasswordAuthenticationToken(
               loginRequest.getUsername(),
               loginRequest.getPassword()
           )
       );

       SecurityContextHolder.getContext().setAuthentication(authentication);

       String accessToken = tokenService.generateAccessToken(authentication);
       RefreshToken refreshToken = tokenService.createRefreshToken(loginRequest.getUsername());

       // Create HTTP-only cookie for refresh token
       ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken.getToken())
           .httpOnly(true)
           .secure(true) // Enable for HTTPS
           .path(REFRESH_TOKEN_PATH)
           .maxAge(refreshExpirationMs / 1000) // Convert ms to seconds
           .sameSite("Strict")
           .build();

       // Calculate expiration in seconds
       int expiresInSeconds = jwtExpirationMs / 1000;

       return ResponseEntity.ok()
           .header(HttpHeaders.SET_COOKIE, cookie.toString())
           .body(new AuthResponseDTO(accessToken, expiresInSeconds));

     } catch (BadCredentialsException e) {
       return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
           .body(new MessageResponse("Invalid username or password"));
     }
   }
   ```

### 5.2 Authorization

The application uses role-based access control:

1. **Roles**:
   - Users can have one or more roles (e.g., USER, ADMIN)
   - Roles are used to control access to endpoints

   ```java
   // From SecurityConfig.java - Configuring endpoint access based on roles
   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
     return http
         .csrf(AbstractHttpConfigurer::disable)
         .authorizeHttpRequests(auth -> auth
             // Public endpoints
             .requestMatchers("/api/auth/**").permitAll()
             .requestMatchers("/", "/courses", "/trips", "/booking/**").permitAll()
             .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
             .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
             // Protected endpoints
             .requestMatchers("/api/dive-logs/**").hasAnyRole("USER", "ADMIN")
             .requestMatchers("/api/admin/**").hasRole("ADMIN")
             .requestMatchers("/h2-console**").permitAll()
             .anyRequest().authenticated()
         )
         .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
         .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
         .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
         .build();
   }
   ```

2. **Endpoint Security**:
   - Endpoints are secured using Spring Security's `@PreAuthorize` annotation
   - For example, admin endpoints require the ADMIN role

   ```java
   // From UserController.java - Method level security with @PreAuthorize
   @GetMapping("/profile")
   @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
   public ResponseEntity<User> getUserProfile(Authentication authentication) {
     CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
     return userService.getUserByUsername(userDetails.getUsername())
         .map(ResponseEntity::ok)
         .orElse(ResponseEntity.notFound().build());
   }

   // From ActivityController.java - Admin-only endpoint
   @PostMapping
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<Activity> createActivity(@RequestBody Activity activity) {
     Activity createdActivity = activityService.createActivity(activity);
     return ResponseEntity.ok(createdActivity);
   }

   // From UserRoleController.java - Complex authorization rule
   @GetMapping("/userEntity/{username}")
   @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
   public ResponseEntity<?> getUserRoles(@PathVariable String username) {
     // Implementation
   }
   ```

## 6. Data Model

### 6.1 Entity Relationships

- **User** - Has many Roles, Bookings, and DiveLogs
- **Role** - Belongs to many Users
- **Activity** - Has many Bookings
- **Booking** - Belongs to a User and an Activity
- **DiveLog** - Belongs to a User
- **RefreshToken** - Belongs to a User

### 6.2 Composite Keys

The application uses composite keys for some entities:

- **UserID** - Composite key for User (username, createdAt)
- **ActivityID** - Composite key for Activity
- **BookingID** - Composite key for Booking
- **DiveLogID** - Composite key for DiveLog

## 7. Exception Handling

The application uses a global exception handler to handle exceptions:

- **GlobalException** - Handles all exceptions and returns appropriate responses
- **ResourceNotFoundException** - Thrown when a resource is not found
- **DuplicateResourceException** - Thrown when a duplicate resource is detected
- **ValidationException** - Thrown when validation fails
- **RoleNotFoundException** - Thrown when a role is not found

## 8. Configuration

The application uses the following configuration:

- **DatabaseInitializer** - Initializes the database with default data
- **CorsConfig** - Configures Cross-Origin Resource Sharing
- **SecurityConfig** - Configures security settings

## 9. Conclusion

OceanDive is a well-structured Spring Boot application that follows best practices for layered architecture, security, and exception handling. The application provides a comprehensive set of features for managing diving activities, bookings, and dive logs.

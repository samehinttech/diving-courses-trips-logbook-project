# OceanDive Documentation

This directory contains comprehensive documentation for the OceanDive application.

## System Architecture Documentation

The [OceanDive System Architecture Documentation](OceanDive_System_Architecture.md) provides a detailed overview of the application's architecture, including:

- System overview
- Architecture layers
- Key components (domain model, controllers, services, repositories, security)
- Key relationships and flows (authentication, activity/booking, userEntity management)
- Security implementation
- Data model
- Exception handling
- Configuration

This documentation is intended for developers who need to understand how the application works and how the different components are linked together.

## API Documentation

The [OceanDive Postman Collection](../apis/OceanDive_Postman_Collection.json) provides a comprehensive collection of API endpoints for the OceanDive application. The collection includes:

- Authentication endpoints (register, login, refresh token, logout)
- User management endpoints (profile, update profile, certification request, change password)
- Admin userEntity management endpoints (get all users)
- User role management endpoints (assign role, remove role)
- Activity management endpoints (get all activities, create activity)
- Booking management endpoints (get userEntity bookings, create booking)
- Dive log management endpoints (get userEntity dive logs, create dive log)

### How to Use the Postman Collection

1. Download and install [Postman](https://www.postman.com/downloads/)
2. Import the OceanDive Postman Collection:
   - Click on "Import" in Postman
   - Select the `OceanDive_Postman_Collection.json` file
3. Set up the environment variable:
   - Create a new environment in Postman
   - Add a variable named `baseUrl` with the value `http://localhost:8080/oceandive` (or your server URL)
   - Add a variable named `accessToken` (this will be populated automatically after login)
4. Use the collection:
   - Start with the "Login" request to authenticate
   - The access token will be automatically stored in the `accessToken` variable
   - Use other requests as needed

## Authentication Flow

The application uses JWT (JSON Web Token) for authentication:

1. **Registration**:
   - Send a POST request to `/api/auth/register` with userEntity details
   - The server creates a new userEntity with a USER role

2. **Login**:
   - Send a POST request to `/api/auth/login` with username and password
   - The server returns an access token in the response body
   - The server sets a refresh token as an HTTP-only cookie

3. **Using Protected Endpoints**:
   - Include the access token in the Authorization header: `Bearer <accessToken>`
   - The server validates the token and allows access if valid

4. **Token Refresh**:
   - When the access token expires, send a POST request to `/api/auth/refresh`
   - The server uses the refresh token cookie to generate a new access token
   - The server returns the new access token in the response

5. **Logout**:
   - Send a POST request to `/api/auth/logout`
   - The server invalidates the refresh token and clears the cookie

## Security Considerations

The application implements several security best practices:

- Access tokens have a short lifespan (1 hour)
- Refresh tokens are stored as HTTP-only cookies to prevent JavaScript access
- Sensitive operations require authentication and appropriate roles
- Passwords are stored using BCrypt encryption
- All API endpoints are protected with appropriate authorization checks
# OceanDive API Collection

This directory contains a Postman collection for testing the OceanDive API endpoints.

## Getting Started

1. Download and install [Postman](https://www.postman.com/downloads/)
2. Import the `OceanDive_API_Collection.json` file into Postman
3. Set up an environment in Postman with the following variables:
   - `baseUrl`: The base URL of your OceanDive API (e.g., `http://localhost:8080`)
   - `username`: Your username for authentication
   - `password`: Your password for authentication
   - `authToken`: This will be automatically populated when you authenticate
   - `refreshToken`: This will be used for token refresh operations

## Collection Structure

The collection is organized into the following folders:

### Authentication
- **Get Authentication Token**: Obtain a JWT token using Basic Auth
- **Refresh Token**: Refresh an existing token
- **Logout**: Invalidate the current token

### Content
- **Get About Info**: Get information about the company
- **Get Contact Info**: Get contact information
- **Get Privacy Policy**: Get privacy policy information
- **Get Terms and Conditions**: Get terms and conditions information

### Courses
- **Get All Courses**: Get a list of all courses
- **Get Course by ID**: Get details of a specific course
- **Get Upcoming Courses**: Get courses starting from today
- **Get Available Courses**: Get courses that are not fully booked
- **Get Courses by Location**: Get courses at a specific location
- **Get Courses by Date Range**: Get courses within a date range

### Trips
- **Get All Trips**: Get a list of all trips
- **Get Trip by ID**: Get details of a specific trip
- **Get Upcoming Trips**: Get trips starting from today
- **Get Available Trips**: Get trips that are not fully booked
- **Get Trips by Name**: Get trips with a specific name
- **Get Trips by Date Range**: Get trips within a date range

### Bookings
- **Book Course for Premium User**: Book a course for an authenticated premium user
- **Book Trip for Premium User**: Book a trip for an authenticated premium user
- **Book Course for Guest User**: Book a course for a guest user
- **Book Trip for Guest User**: Book a trip for a guest user

### Dive Logs
- **Get All Dive Logs**: Get all dive logs for the current user
- **Get Dive Log by ID**: Get a specific dive log
- **Get Dive Logs by Location**: Get dive logs at a specific location
- **Get Dive Logs by Date Range**: Get dive logs within a date range
- **Create Dive Log**: Create a new dive log
- **Update Dive Log**: Update an existing dive log
- **Delete Dive Log**: Delete a dive log

### Admin
- **Add Course**: Create a new course (requires ADMIN role)
- **Add Trip**: Create a new trip (requires ADMIN role)
- **View Users**: Get all users (requires ADMIN role)
- **View Bookings**: Get all bookings (requires ADMIN role)
- **Add Admin**: Create a new admin user (requires ADMIN role)

## Authentication

Most endpoints require authentication. To authenticate:

1. Run the "Get Authentication Token" request with your username and password
2. The token will be automatically stored in the `authToken` environment variable
3. Subsequent requests will use this token in the Authorization header

## Testing

The collection includes basic tests that verify:
- Status codes (200 OK or 201 Created)
- Response times
- Valid JSON responses

You can extend these tests as needed for your specific requirements.
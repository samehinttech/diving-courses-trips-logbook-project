
## Is it normal to see sensitive data in Postman JSON responses?

Yes, it is completely normal and expected to see the access token, refresh token, username, email, and roles in the Postman JSON response after authentication. This is the standard behavior of the OceanDive API's authentication system.

### Why this is normal:

1. **Authentication Flow Design**: According to the API documentation, the login endpoint (`/api/auth/login`) is specifically designed to return these credentials as part of the authentication flow:
   ```json
   {
     "accessToken": "string",
     "refreshToken": "string",
     "username": "string",
     "email": "string",
     "roles": ["string"]
   }
   ```

2. **JWT-based Authentication**: The application uses JWT (JSON Web Tokens) for authentication. The access token you're seeing is a JWT that contains encoded information about the userEntity and is signed with a secret key. This is standard practice in modern web applications.

3. **Token Management**: The Postman environment is configured to capture and store these tokens as environment variables (`accessToken` and `refreshToken`), which are then used for subsequent authenticated requests.

4. **Security Considerations**: 
   - The access token expires after 1 hour (as configured in application.properties)
   - The refresh token expires after 24 hours
   - These tokens are meant to be transmitted and stored securely

### How these tokens are used:

1. The access token is included in the Authorization header for subsequent API requests
2. When the access token expires, the refresh token is used to obtain a new access token
3. This approach eliminates the need to send username and password with every request

This is standard practice for RESTful APIs using token-based authentication, and the response you're seeing is working as designed.
# OceanDive

A website that offers dive courses, trips, and digital dive logs, designed to
meet the requirements of the **Internet Technology module** at
**FHNW**.

[![License](https://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
## Table of Contents
<!-- TOC -->
* [OceanDive](#oceandive)
  * [Analysis](#analysis)
    * [Scenario](#scenario)
    * [User Stories](#user-stories)
      * [Admin User Stories](#admin-user-stories)
      * [Guest stories (non-registered users)](#guest-stories-non-registered-users)
      * [Registered User Stories](#registered-user-stories)
    * [Use Case Diagram](#use-case-diagram)
  * [Design](#design)
    * [Wireframe Design](#wireframe-design)
    * [Prototype Design](#prototype-design)
    * [Domain Design](#domain-design)
    * [Business Logic](#business-logic)
  * [Implementation](#implementation)
    * [Backend Technology](#backend-technology)
    * [Frontend Technology](#frontend-technology)
  * [Execution](#execution)
    * [Deployment to a Paas](#deployment-to-a-paas)
  * [Project Management](#project-management)
    * [Roles](#roles)
    * [Milestones](#milestones)
  * [Maintainers](#maintainers)
  * [Contributors](#contributors)
  * [Note](#note)
<!-- /TOC -->
## Analysis
### Scenario

As a team, we designed the website OceanDive that offers dive courses,
dive trips, and additional service “digital dive log.”
A new userEntity visits the website as a guest, browses available dive courses, and
books a course without a mandatory login. Similarly, guests can explore and book
dive trips without needing an account. Users are prompted to choose a valid
option of the dive certifications provided on the web when booking "dive trips
or advanced courses" or being non-divers otherwise, ensuring only qualified
divers can participate.

Users can optionally create an account to access the digital dive log service,
where they securely log dive details (dive number, dive date, dive location, air
temperature,
surface temperature, dive start time & end time, dive max depth and notes). Once
registered, they can view and/or edit their dive logs by adding, updating, or
deleting entries. Additionally, users can delete their accounts.

On the administrative side, admins manage the platform by adding/updating
courses, trips, and general content. They adjust trip availability and maintain
userEntity accounts (deactivating suspicious accounts or resetting passwords).
User autonomy is prioritized: divers retain full control over their data,
including the ability to delete dive logs permanently.

### User Stories

#### Admin User Stories

1. As an admin, I want to add, update, and delete dive courses and trips so that
   the platform stays up to date.
2. As an admin, I want to manage trip availability so that users see only
   currently available trips.
3. As an admin, I want to edit website content (Terms and Conditions,
   Privacy Policies) to keep information accurate and relevant.
4. As an admin, I want to manage userEntity accounts (deactivate suspicious
   accounts, reset passwords) so that I can maintain platform security.
5. As an admin, I want to ensure users retain full control over their dive logs
   so that they can delete or modify their entries at any time.

#### Guest stories (non-registered users)

1. As a guest, I want to browse available dive courses and trips without logging
   in so that I can explore options freely.
2. As a guest, I want to book a dive course or trip without needing an account
   so that I can make quick reservations.

#### Registered User Stories

1. As a userEntity, I want to select a valid dive certification when booking diving
   trips or advanced courses so that I can comply with safety requirements.
2. As a userEntity, I want to select "non-diver" when booking trips or dive courses.
3. As a userEntity, I want to create an account so that I can securely store my dive
   logs.
4. As a userEntity, I want to log in so that I can access my saved dive logs and
   personal details.
5. As a userEntity, I want to view, update, or delete my dive logs so that I can
   manage my records efficiently.
6. As a userEntity, I want to delete my account so that I have full control over my
   data.

### Use Case Diagram

<div style="text-align: center;">
Use Case Diagram illustrating OceanDive Web Application system interactions.

![Use Case Diagram](assets/wireframe/use-case-diagram.png)
</div>

---

1. UC-1 [Create Account] – A userEntity creates an account to access personalized
   features.
2. UC-2 [Login] – A userEntity logs into the system.
3. UC-3 [Manage Dive Log (View, Update, Delete)] – A userEntity can view, update, or
   delete their dive logs.
4. UC-4 [Book Dive Trip] – A guest or userEntity books a dive trip.
5. UC-5 [Select Dive Certification (or No Certification)] – A userEntity must select a
   valid dive certification when booking trips.
6. UC-6 [Book Dive Course] – A guest or userEntity books a dive course.
7. UC-7 [Browse Courses & Trips] – A guest or userEntity can browse available dive
   courses and trips.
8. UC-8 [Delete Account] – A userEntity deletes their account.
9. UC-9 [Manage Courses & Trips (Add, Update, Delete)] – An admin manages
   courses and trips.
10. UC-10 [Manage Trip Availability] – An admin updates trip availability.
11. UC-11 [Manage User Accounts] – An admin manages userEntity accounts.
12. UC-12 [Ensure User Data Control] – Ensures users can manage and control
    their data.

## Design
### Wireframe Designs

- **Home Page**
![Wireframe Design - Welcome Page](assets/wireframe/welcomePage.jpg)


- **About Us**
![Wireframe Design - About Us](assets/wireframe/AboutUs.jpg)


- **Contact Us**
![Wireframe Design - Contact Us](assets/wireframe/ContactUs.jpg)


- **Create Your Account**
![Wireframe Design - Create Your Account](assets/wireframe/CreateYourAccount.jpg)


- **Log In**
![Wireframe Design - Log In](assets/wireframe/LogIn.jpg)


- **Account View**
![Wireframe Design - ActView](assets/wireframe/ActView.jpg)


- **Dive Log Management**
![Wireframe Design - Dive Log](assets/wireframe/DiveLog.jpg)


### Prototype Design
The prototype demonstrates the core user flows including guest browsing, user registration, dive log management, and admin functionality. Interactive prototypes were developed to validate user experience and interface design decisions.


The prototype includes:
- **Home Page**: The landing page with navigation to various sections.
![Prototype Design](assets/prototype/home-page.jpeg)


- **About Us**: Information about the platform and its mission.
![About Us](assets/prototype/about-us.jpeg)


- **Contact Us**: Contact form for inquiries.
![Contact Us](assets/prototype/contact-us.jpeg)


- **Create Your Account**: User registration form.
![Create Your Account](assets/prototype/register.jpeg)


- **Log In**: User login interface.
![Log In](assets/prototype/login-page.jpeg)


- **Account View**: Interface for view / update / delete / and managing dive logs.
![Account View](assets/prototype/account-view.jpeg)


- **Dive-Log Management**: Interface for users log new dives.
![Dive Log Management](assets/prototype/log-adive.jpeg)


### Domain Design
The application follows a layered architecture with clear separation between presentation, business, and data access layers. The domain model includes entities for Users, Courses, Trips, DiveLogs, and Bookings with appropriate relationships and constraints.

![Domain Design](assets/wireframe/relationship-diagram.png)


### Business Logic
- **Dive Log Management**
  - Users can create, view, update, and delete their dive logs.
  - Each dive log entry must include required fields such as dive number, date, start and end time, location, air temperature, surface temperature, and notes.
  - The system validates user input to ensure data integrity.:


```java
Path:/api/dive-logs
Methods: GET, POST, PUT, DELETE
```
Security: Requires user authentication and ownership validation

```
Path:/api/auth/user/register
Methods: POST
```
```
Path:/api/auth/user/login
Methods: POST
```

## Implementation
### Backend Technology
This web application is built using Spring Boot and includes the following key dependencies:
- **Java 21**: The programming language used for backend development.
- **H2 Database**: AS file database for development and production.
- **Spring Boot**: A framework for building Java applications with ease.
- **Spring Boot Starter Web**: For building web applications, including RESTful services.
- **Spring Boot Starter Data JPA**: For database access and ORM support.
- **Spring Boot Starter Security**: For securing the application with authentication and authorization.

### Frontend Technology
- **Thymeleaf**: A modern server-side Java template engine for web and standalone environments.
- **CSS**: For styling the web application.
- **JavaScript**: For client-side scripting and interactivity.

The frontend is designed to be responsive and user-friendly, ensuring a seamless experience across devices.
The following dependencies been part of what we used to develop the application:

```xml
<!-- Spring Boot Starter Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<!-- Thymeleaf for server-side rendering -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
  <!-- Spring Boot Starter for Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<!-- Spring Boot Starter for Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
- **Swagger**: For API documentation and testing.

```xml
<dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```
## Execution
1- Clone the repository:
```bash
git clone https://github.com/samehinttech/diving-courses-trips-logbook-project.git
```
2- Add your own credentials to the `application.properties` file if needed.
3- Navigate to the project directory:
```bash
cd diving-courses-trips-logbook-project
```
4- Build the project using Maven:
```bash
mvn clean install
```
5- Run the application:
```bash
mvn spring-boot:run
```
6- Access the application in your web browser at `http://localhost:8080`.

7- For API documentation, visit `http://localhost:8080/swagger-ui.html`.



### Deployment to a PaaS
The application has been successfully deployed to [Render](https://render.com/), though, with some limitations and challenges encountered during the deployment process.

**Successfully Deployed**:
- Application builds and starts correctly on Render
- Basic web server functionality is working
- Database connections are established

**Challenges**:
- Some features are not working as intended in the production environment
- Potential CORS configuration issues affecting frontend functionality
- Database persistence limitations with an H2 file database
- Static resource serving may have path resolution issues

- **WEBSITE URL**: [OceanDive on Render](https://oceandive.onrender.com/) (Note: May experience cold start delays on free tier)

- **Render service setup**:
    - **Service Type**: Web Service
    - **Environment**: Java
    - **Build Command**: `mvn clean install`
    - **Start Command**: `java -jar target/*.jar`
    - **Port**: 8080 (fixed port)
    - **Health Check Path**: `api/actuator/health` or `api/health`
  
- **Deployment configuration**:

```docker
# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy JAR
COPY --from=build /app/target/*.jar app.jar

# Create upload directory
RUN mkdir -p /app/uploads

# Expose port 8080 (your fixed port)
EXPOSE 8080

# Health check on port 8080 - try actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || curl -f http://localhost:8080/ || exit 1

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Project Management
### Roles
The project is managed by a team of students, each taking on specific roles to ensure smooth development and deployment:
- **Project Manager**: Iulia Mara Udrea, responsible for overall project coordination, timeline management, and communication with stakeholders.
- **Backend Developer**: Sameh Ahmed focused on backend development, including API design, database management, and server-side logic.
- **Frontend Developer**: Iulia Mara Udrea, Mehak Khan, Asim Rasheed, and Sameh Ahmed worked on the frontend, ensuring a responsive and user-friendly interface.
- **Project Representative**: Asim Rasheed.

### Milestones
1. Analysis & Planning (Week 1–2): Requirements gathering, user story definition, and use case analysis
2. Design Phase (Week 3–4): Wireframe creation, domain modeling, and API design
3. Core Implementation (Week 5–7): Backend development, database setup, and basic frontend
4. Feature Development (Week 8–10): Advanced features, security implementation, and user interface polish
5. Testing & Integration (Week 11–12): Quality assurance, performance testing, and bug fixes
6. Deployment & Documentation (Week 13–14): Production deployment and final documentation

## Maintainers
- Iulia Mara Udrea
- Mehak Khan
- Sameh Ahmed
- Asim Rasheed

## Contributors
- Charuta Pande
- Devid Montecchiari

---

## Note

*Built for learning, not profit — but hey, maybe one day? 🌟*

*P.S. Open to feedback! We’re still students, after all.*

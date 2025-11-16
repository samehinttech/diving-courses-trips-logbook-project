# OceanDive

A website that offers dive courses, trips, and digital dive logs, designed to
meet the requirements of the **Internet Technology module** at
**FHNW**.

[![License](https://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

---

## Overview

OceanDive enables users to:

- Browse and book dive courses and trips
- Log dive details securely (for registered users)
- Manage accounts and dive logs
- Admins can manage courses, trips, and user accounts

For **full documentation** (analysis, design, implementation, execution, project management, and more), visit the [Wiki](https://github.com/samehinttech/diving-courses-trips-logbook-project/wiki).

---

## Features

- **Guest users**: Browse courses and trips, book without an account
- **Registered users**: Create accounts, log dives, manage logs
- **Admin users**: Manage courses, trips, content, and user accounts
- Responsive and user-friendly frontend

---

## Technology Stack

- **Backend**: Java 21, Spring Boot, H2 Database, Spring Data JPA, Spring Security
- **Frontend**: Thymeleaf, CSS, JavaScript
- **API Documentation & Testing**: Swagger

---

## Quick Start

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
6- Access the application in your web browser 
```bash
http://localhost:8080
```
7- For API documentation, visit 
```bash
http://localhost:8080/swagger-ui.html
```
## Deployment
The application has been successfully deployed to [Render](https://render.com/) (note: free-tier may have cold start delays).


## Maintainers
- Iulia Mara Udrea
- Mehak Khan
- Sameh Ahmed
- Asim Rasheed

## Contributors
- Devid Montecchiari

## Notes
- Built for learning purposes, not for commercial use.
- Open to feedback from peers or instructors.
- Some features may be limited in production (H2 database, free-tier deployment).
- The project is part of the Internet Technology module at FHNW.

*P.S. We are still students, so improvements and updates may continue! 🌟*
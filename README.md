# OceanDive

A website that offers dive courses, trips, and digital dive logs, designed to
meet the requirements of the **Internet Technology module** at
**FHNW**.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Status](https://img.shields.io/badge/Status-Completed-success.svg)]()
[![FHNW](https://img.shields.io/badge/FHNW-Internet%20Technology-blue.svg)]()

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
2- Add your own credentials to the 
`application.properties` file if needed.

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
- Iulia Mara Udrea - Project Manager & WireFrame Design [IuliaU](https://github.com/IuliaU)
- Mehak Khan - Use Cases Design  [Mehak-Khan9](https://github.com/Mehak-Khan9)
- Sameh Ahmed - Backend & Front Developer, API design, database management, server-side logic [samehinttech](https://github.com/samehinttech)
- Asim Rasheed - Project Representative

## Contributors
- Devid Montecchiari

## License

This project is licensed under the **Apache License 2.0** - see [LICENSE](http://www.apache.org/licenses/LICENSE-2.0.html) for more details.

### Disclaimer
This project is developed for **educational purposes** as part of the Internet Technology module at FHNW.

### Important Notes
- **Not production-ready** without significant modifications
- **Provided without warranty** of any kind
- **Not intended for commercial use**
- **A learning exercise** demonstrating web development concepts
  
### What This Means
**You CAN:**
- Use this project for educational purposes
- Modify and distribute the code
- Use it in your portfolio or resume
- Learn from the implementation

**You CANNOT:**
- Use this for commercial purposes without permission
- Hold the authors liable for any issues
- Remove or modify copyright notices
- Claim this work as your own

## Acknowledgments
- **FHNW—** University of Applied Sciences Northwestern Switzerland
- **Internet Technology Module** - Course Lecturer and peers
- **Spring Framework Team** - For excellent documentation
- **H2 Database** - For a lightweight database solution
- **Render** - For free hosting platform

---

Made with ❤️ by FHNW students | [Report Bug](https://github.com/samehinttech/diving-courses-trips-logbook-project/issues) | [Request Feature](https://github.com/samehinttech/diving-courses-trips-logbook-project/issues)


**Have a feature idea?** [Suggest it here →](https://github.com/samehinttech/diving-courses-trips-logbook-project/issues/new?template=feature_request.md)

*P.S. We are still students, so improvements and updates may continue! 🌟*

**If you find this project helpful, please consider giving it a star!**

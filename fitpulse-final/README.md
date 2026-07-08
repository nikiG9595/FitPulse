# FitPulse

FitPulse is a Spring MVC fitness management system. It is built as a complete educational project for practicing Spring Boot, MVC, Thymeleaf, JPA, validation, security and layered architecture.

## Tech Stack

- Java 17
- Spring Boot 3.4.0
- Maven
- Spring MVC
- Thymeleaf
- Spring Security
- Spring Data JPA
- MySQL
- Bean Validation
- Bootstrap 5

## Main Features

- User registration and login
- Role-based access control: ADMIN and MEMBER
- Membership plan management
- Workout class management
- Workout booking and cancellation
- Profile page with active membership and bookings
- Server-side validation for all forms
- Custom business exceptions
- UUID primary keys for entities

## Domain Entities

- User
- Membership
- GymClass
- WorkoutBooking

## Demo Accounts

- Admin: `admin / admin123`
- Member: `member / member123`

## Database Setup

The project uses MySQL.

Default configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fitpulse?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Root123.
```

Change the password in `src/main/resources/application.properties` if your local MySQL password is different.

## How to Run

1. Open the project in IntelliJ IDEA.
2. Make sure Project SDK is Java 17 or newer.
3. Start MySQL locally.
4. Run `FitPulseApplication`.
5. Open `http://localhost:8080`.

## Suggested Git Commit Plan

- chore: initialize Spring Boot project
- feat: add domain entities
- feat: configure Spring Security
- feat: implement registration and login
- feat: implement membership management
- feat: implement gym class management
- feat: implement workout booking
- fix: improve validation errors
- refactor: clean service layer
- docs: add project README

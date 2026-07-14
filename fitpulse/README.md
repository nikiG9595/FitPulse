## FitPulse

FitPulse is a Spring Boot fitness management application that allows users to register, 
choose a membership plan, browse available training classes, book workouts, and manage their profile.
You can start it when you run FitPulseApplication class and open `http://localhost:8080`.

Administrators (Admin) can create, edit, and delete membership plans and training classes.



## Tech Stack

- Java 17
- Spring Boot 3.4.0
- Maven
- Spring MVC
- Thymeleaf
- Spring Security
- Spring Data JPA
- MySQL
- Bootstrap 5



## Main Features

- User registration
- User login and logout
- Session-based authentication
- Role-based access control
- Membership plan management
- Training class management
- Workout booking
- Booking cancellation
- Membership-level access validation
- Personalized dashboard
- User profile
- Server-side form validation
- Custom exception handling



## Domain Entities

The application contains the following domain entities:

- `User`
- `Membership`
- `GymClass`
- `WorkoutBooking`
  Each entity uses a UUID as its primary key.


## User Roles

 ADMIN:

An administrator can:

- Create membership plans
- Edit membership plans
- Delete membership plans
- Create training classes
- Edit training classes
- Delete training classes
- Access protected administrator functionality

 MEMBER:

A member can:

- Choose a membership plan
- Browse available training classes
- View training class details
- Book eligible training classes
- Cancel their own bookings
- View their current membership
- View their profile


## Business Rules

The application enforces the following business rules:

- A user must choose a membership before booking a class.
- The user's membership level must meet the requirement of the selected class.
- A user cannot book the same class more than once.
- A class cannot exceed its maximum capacity.
- A user can cancel only their own bookings.
- Membership types must be unique.
- A training class with active bookings cannot be deleted.


## Security

The application uses Spring Security.

- Guests can access the home page, login page, and registration page.
- Logged-in users can access the protected application pages.
- Only administrators can create, edit, or delete membership plans and training classes.
- Passwords are stored using BCrypt hashing.


## Validation and Error Handling

All forms include server-side validation.

Examples of validation include:

- Required fields
- Username length
- Valid email format
- Password length
- Positive membership prices
- Valid class capacity
- Future training dates

Business rule violations throw a custom `FitPulseException`.

Validation errors are displayed next to the corresponding form fields.


## Database

The application uses MySQL and Spring Data JPA.



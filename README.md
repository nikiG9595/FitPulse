# EN 

## FitPulse

FitPulse is a Spring Boot fitness management application that allows users to register, 
choose a membership plan, browse available training classes, book workouts, and manage their profile.

Start the application by running the FitPulseApplication class, then open http://localhost:8080 in your browser.

Administrators (Admin) can create, edit, and delete membership plans and training classes.



## Tech Stack

- Java 21
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


# BG

# FitPulse

FitPulse е приложение за управление на фитнес, разработено със Spring Boot. 
То позволява на потребителите да се регистрират, да изберат абонаментен план, да разглеждат наличните тренировки, 
да се записват за тях и да управляват своя профил.

Стартирайте приложението, като изпълните класа `FitPulseApplication`, 
след което отворете `http://localhost:8080` във вашия браузър.

Администраторите (Admin) могат да създават, редактират и изтриват абонаментни планове и тренировки.



## Използвани технологии

- Java 21
- Spring Boot 3.4.0
- Maven
- Spring MVC
- Thymeleaf
- Spring Security
- Spring Data JPA
- MySQL
- Bootstrap 5



## Основни функционалности

- Регистрация на потребители
- Вход и изход от системата(профилите)
- Автентикация чрез сесии
- Контрол на достъпа според ролята на потребителя
- Управление на абонаментни планове
- Управление на тренировки
- Записване за тренировки
- Отказване на записване
- Проверка на необходимото ниво на абонамент
- Персонализирано табло (Dashboard)
- Потребителски профил
- Валидация на формите от страна на сървъра
- Обработка на грешки чрез персонализирани изключения



## Основни обекти (Domain Entities)

Приложението съдържа следните основни обекти:

- `User`
- `Membership`
- `GymClass`
- `WorkoutBooking`

Всеки обект използва UUID като уникален идентификатор (Primary Key).



## Потребителски роли

ADMIN:

Администраторът може да:

- Създава абонаментни планове
- Редактира абонаментни планове
- Изтрива абонаментни планове
- Създава тренировки
- Редактира тренировки
- Изтрива тренировки
- Достъпва административните функционалности



MEMBER:

Потребителят може да:

- Избира абонаментен план
- Разглежда наличните тренировки
- Разглежда подробности за тренировка
- Записва се за тренировки
- Отказва собствените си записвания
- Преглежда текущия си абонамент
- Преглежда своя профил



## Бизнес правила

Приложението прилага следните бизнес правила:

- Потребителят трябва първо да избере абонаментен план, преди да може да се запише за тренировка.
- Нивото на абонамента трябва да отговаря на изискването на избраната тренировка.
- Един потребител не може да се запише два пъти за една и съща тренировка.
- Броят на записаните потребители не може да надвишава капацитета на тренировката.
- Потребителят може да отменя само собствените си записвания.
- Всеки тип абонаментен план трябва да бъде уникален.
- Тренировка с активни записвания не може да бъде изтрита.



## Сигурност

Приложението използва Spring Security.

- Гостите имат достъп само до началната страница, страницата за вход и страницата за регистрация.
- Влезлите потребители имат достъп до защитените страници на приложението.
- Само администраторите могат да създават, редактират и изтриват абонаментни планове и тренировки.
- Паролите се съхраняват криптирани чрез BCrypt.



## Валидация и обработка на грешки

Всички форми включват валидация от страна на сървъра.

Примери за валидация:

- Задължителни полета
- Минимална и максимална дължина на потребителското име
- Валиден имейл адрес
- Минимална дължина на паролата
- Положителна стойност на цената на абонамента
- Валиден капацитет на тренировката
- Бъдеща дата и час на тренировката

При нарушаване на бизнес правилата се хвърля персонализирано изключение `FitPulseException`.

Грешките при валидацията се показват непосредствено до съответното поле във формата.



## База данни

Приложението използва MySQL и Spring Data JPA.



# IT3030 – PAF Assignment 2026
## Group Coursework Final Report

**Group Name:** Smart Campus Group XX  
**Project:** Smart Campus Operations Hub  
**Module Handled:** Module B – Booking Management  

---

## 1. Requirements

### Functional Requirements (Module B)
1. **Resource Catalogue:** The system maintains an active list of resources that can be booked (Lecture Halls, Meeting Rooms, Equipment).
2. **Booking Requests:** Standard Users can request a booking for a specific resource by providing start and end times, purpose, and expected attendees.
3. **Conflict Prevention:** The system must strictly enforce that overlapping time ranges for a specific resource are rejected with a conflict error.
4. **Approval Workflow:** Admin users can review pending requests, approve them, or reject them with a mandatory reason.
5. **Booking Dashboard:** Users can only view and cancel their own pending/approved bookings. Admins have a global view of all bookings.

### Non-Functional Requirements
- **Architecture:** Layered REST RESTful API relying on Spring Boot.
- **Data Persistence:** Relational database setup via JPA / Hibernate (H2 Database).
- **Usability:** The UI must be constructed using React, focusing on a clean and accessible user experience without page reloads.

---

## 2. Architecture Diagrams

### Backend Architecture (Spring Boot)
The API leverages exactly the layers required by the Marking Rubric:
- **Controller Layer (`BookingController`):** Handles all 4 varying HTTP methods (GET, POST, PATCH, DELETE) and Maps endpoints to standard RESTful principles (`/api/bookings/{id}`).
- **Service Layer (`BookingService`):** Orchestrates business logic, strict validation of timestamps, and uses repository queries to prevent resource conflict. Contains Global Exception translation.
- **Repository Layer (`BookingRepository`):** Performs optimized JPQL database interactions.
- **Database:** Application strictly mapped using Entity objects linked via Foreign Keys.

### Frontend Architecture (React)
- **Component Pattern:** Focused loosely coupled UI using `BookingDashboard.jsx`.
- **API Service:** Employs `axios` configurations to decouple network requests from rendering components.
- **State Management:** Uses precise `useEffect` and `useState` hooks to dictate Admin vs. Standard User workflows dynamically.

---

## 3. REST API Endpoints

Following the strict standard constraint guidelines:

| Method   | Endpoint                  | Purpose                                                 | Expected Status Codes     |
|----------|---------------------------|---------------------------------------------------------|---------------------------|
| **GET**  | `/api/bookings/all`         | Retrieve all bookings across the campus (Admin only).   | 200 OK                    |
| **GET**  | `/api/bookings/user/{id}`   | Retrieve bookings belonging to a specific user.         | 200 OK, 404 Not Found     |
| **POST** | `/api/bookings`             | Create a new resource booking request.                  | 201 Created, 400, 409     |
| **PATCH**| `/api/bookings/{id}/status` | Admin updates status (Approve/Reject) with reasons.     | 200 OK, 400, 404          |
| **DELETE**| `/api/bookings/{id}`        | User cancels an existing pending/approved request.      | 204 No Content, 404       |

---

## 4. Testing Evidence & Validation

### Conflict Validation
If a User attempts to book "Lecture Hall A" on 10/10/2026 from 10:00 to 12PM, and another User immediately attempts 11:00 to 1PM, the Spring Boot JPA explicit query:
`SELECT COUNT(b) > 0 FROM Booking b WHERE b.resource.id = :resourceId AND (b.startTime < :endTime AND b.endTime > :startTime)`
Returns a `boolean` conflict.
The server throws a `ResourceConflictException`, which is mapped globally by `@RestControllerAdvice` to emit a `409 CONFLICT` HTTP code. The React UI parses this gracefully and alerts the User.

### Data Layer Checks
Hibernate ORM relies precisely on `@Valid` checks mapping `@NotNull`, `@NotBlank`, and date sequences (`@FutureOrPresent`) guaranteeing database purity.

---

## 5. Team Contribution Summary

- **Member 1:** (Not Documented in this file - Focus is exclusively Member 2's execution)
- **Member 2 (Akshayan):** 
  - Entire logical layer encompassing **Module B - Booking Management**.
  - Built `Booking`, `BookingStatus`, and relationship bindings.
  - Formed absolute REST APIs adhering strictly to Rubric guidelines `/api/bookings`.
  - Constructed `React Dashboard` ensuring Admin workflows function exactly as tested against the assignment.
- **Member 3:** ...
- **Member 4:** ...

---

## 6. Setup Instructions

1. **Running the API Engine:**
   - Navigate to `backend` folder.
   - Run via Maven: `./mvnw spring-boot:run`
   - API listens on `http://localhost:8080` (H2 database automatically seeds testing data).

2. **Starting the Web Client:**
   - Navigate to `frontend` folder.
   - Execute: `npm install`
   - Launch local interface via: `npm run dev`

*(End of Report Document)*

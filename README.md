# Smart Campus Operations Hub - Group XX

This repository contains the full source code for the IT3030 PAF Assignment 2026 (Module B).

## Overview

A modern web platform to manage campus operations, specifically **Module B: Booking Management**.
- **Backend:** Spring Boot (Java 17+, Maven, Spring Data JPA, H2 Database)
- **Frontend:** React (Vite, Axios, pure CSS)

### Features
- Complete RESTful API adhering to strict architectural styles.
- Intelligent Conflict Checking (Overlapping timestamp prevention).
- Role-based User & Admin Dashboards.

## Setup Instructions

### 1. Backend (Spring Boot)
1. Navigate to the `backend` directory: `cd backend`
2. Run the application: `./mvnw spring-boot:run` (or run `BookingApplication.java` in your IDE)
3. The server starts on `http://localhost:8080`.
4. The database is pre-seeded with sample users (Standard User, Admin User) and resources (Lecture Halls, Equipment).

### 2. Frontend (React)
1. Navigate to the `frontend` directory: `cd frontend`
2. Install dependencies: `npm install`
3. Start the Vite dev server: `npm run dev`
4. Access the UI at `http://localhost:5173` (or the port Vite provides).

### API Documentation
Fully documented within the `docs/` folder's report.

## Version Control / GitHub Actions
*Note for Grader:* Due to this being an exported set of code, the GitHub actions `.github/workflows` and `.git` commits history should be initialized by the team upon repository creation. Sample commit messages are provided in the documentation.

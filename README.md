# Nettdetektivene – Team 11

**IDATT2106 System Development 2 with Agile Project – Spring 2026**

Nettdetektivene is a gamified web application that teaches children (3rd–7th grade) how to recognise and handle digital crime, misinformation, and critical incidents online. Teachers can create classrooms, assign tasks, and track student progress through an interactive mystery-investigation interface.

---

## Table of Contents

- [Team Members](#team-members)
- [Links](#links)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Running for Development](#running-for-development)
- [Environment Variables](#environment-variables)
- [Test Users](#test-users)
- [Project Structure](#project-structure)
- [Authentication](#authentication)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Wiki Structure](#wiki-structure)

---

## Team Members

| Name                    | Username                                  |
| ----------------------- | ----------------------------------------- |
| Helena An Haaland Haarr | [@hahhaarr](https://git.ntnu.no/hahhaarr) |
| Fredrik Tolås Borbe     | [@fredritb](https://git.ntnu.no/fredritb) |
| Marius Kiplesund        | [@mariukip](https://git.ntnu.no/mariukip) |
| Atas Lapenas            | [@atasl](https://git.ntnu.no/atasl)       |
| Cecilie Vu              | [@cecilivu](https://git.ntnu.no/cecilivu) |
| Ingve Værnes            | [@ingvev](https://git.ntnu.no/ingvev)     |
| Piranavan Visvalingam   | [@piranavv](https://git.ntnu.no/piranavv) |

---

## Links

- [GitHub Repository](https://git.ntnu.no/fredritb/idatt2106_2026_11)
- [Issue Board](https://git.ntnu.no/fredritb/idatt2106_2026_11/-/boards)

> TODO: Add a link to the deployed/hosted version of the app once it is available

---

## Tech Stack

| Area         | Technology                                 | Version |
| ------------ | ------------------------------------------ | ------- |
| **Frontend** | React                                      | 19.x    |
|              | TypeScript                                 | 5.x     |
|              | Node.js                                    | 24+     |
| **Backend**  | Java                                       | 25      |
|              | Spring Boot                                | 4.x     |
|              | Spring Security                            | 6.x     |
|              | JPA / Hibernate                            | Latest  |
| **Database** | MySQL                                      | 8.x     |
| **Storage**  | AWS S3 (image uploads)                     | —       |
| **Auth**     | JWT access token + HttpOnly refresh cookie | —       |
| **Build**    | Maven                                      | 3.9+    |
| **CI**       | GitHub Actions                             | —       |
| **API Docs** | Swagger / OpenAPI                          | 3.0     |
| **Testing**  | JUnit 5 (backend), Vitest (frontend)       | Latest  |

---

## Prerequisites

- **Java 25** and **Maven 3.9+**
- **Node.js 24+** and **npm**
- **MySQL 8+** running locally or via Docker
- **Git**

---

## Running for Development

### Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.
Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### Frontend (React + Vite)

```bash
cd frontend
npm install   # first time only
npm run dev
```

The app starts at `http://localhost:5173`.

---

## Test Users

Default test data is loaded automatically on startup.

| Email                | Password      | Role    |
| -------------------- | ------------- | ------- |
| `teacher@test.local` | `password123` | TEACHER |
| `student@test.local` | `password123` | STUDENT |

> TODO: Verify and update these credentials once seed data is finalised
> TODO: Add more representative accounts, e.g. a teacher with multiple classrooms or a student mid-progress

---

## Project Structure

```
idatt2106_2026_11/
├── backend/
│   └── src/main/java/org/ntnu/backend/
│       ├── user/          # Auth, registration, login
│       ├── classroom/     # Enrollments, classroom management
│       ├── game/          # Levels, level completions, boss battles
│       ├── level/         # Level and stop definitions
│       ├── image/         # S3 image upload and retrieval
│       ├── avatar/        # User avatar items and starter packs
│       ├── medalion/      # Achievement medals
│       ├── notes/         # Detective notebook entries
│       ├── security/      # JWT, refresh tokens, UserPrincipal
│       ├── config/        # Spring configuration beans
│       ├── exception/     # Global exception handling
│       └── filehandling/  # JSON content serialization utilities
├── frontend/
│   └── src/
│       ├── pages/         # Student and teacher portal pages
│       ├── components/    # Reusable UI components
│       ├── api/           # API client functions
│       ├── hooks/         # Custom React hooks
│       ├── stores/        # Global state (Zustand)
│       ├── routes/        # Route definitions and guards
│       ├── layouts/       # Shared page layout wrappers
│       ├── types/         # TypeScript type definitions
│       ├── utils/         # Helper utilities
│       ├── data/          # Static data / content files
│       ├── assets/        # Images and static assets
│       ├── audio/         # Sound files
│       └── test/          # Frontend test helpers and fixtures
└── docs/                  # Documentation and diagrams
```

---

## Authentication

The platform uses a dual-token strategy and enforces strict portal separation:

- **Access token** — short-lived JWT returned in the response body. Attach as `Authorization: Bearer <token>` on every request.
- **Refresh token** — long-lived token stored in an `HttpOnly` cookie scoped to `/api/auth/refresh`. Sent automatically by the browser.

A `STUDENT` account cannot authenticate on the teacher portal and vice versa — a mismatch returns `403 Forbidden` without leaking role information.

| Endpoint         | Method | Description                           |
| ---------------- | ------ | ------------------------------------- |
| `/auth/register` | POST   | Create a new account                  |
| `/auth/login`    | POST   | Log in and receive tokens             |
| `/auth/refresh`  | POST   | Exchange refresh token for a new JWT  |
| `/auth/logout`   | POST   | Revoke refresh token and clear cookie |

---

## API Documentation

Interactive documentation is available via Swagger UI at
`http://localhost:8080/swagger-ui.html` when the backend is running.

| Feature Area          | Base Path(s)                                    | Key Operations                                    |
| --------------------- | ----------------------------------------------- | ------------------------------------------------- |
| **Authentication**    | `/auth`                                         | register, login, refresh, logout                  |
| **Avatar & Profile**  | `/user/profile`, `/user/inventory`              | get/update profile, equip items                   |
| **Classrooms**        | `/classrooms`, `/teacher/classrooms`            | CRUD classrooms, enroll/remove students           |
| **Students**          | `/teacher/classrooms/{id}/students`             | list, view progress, notebook, medals             |
| **Co-teachers**       | `/teacher/classrooms/{id}/teachers`             | invite, remove co-teachers                        |
| **Censor words**      | `/teacher/classrooms/{id}/censor-words`         | add/remove profanity filter words                 |
| **Game – Core**       | `/game/stops`, `/game/levels`, `/game/questions`| browse stops/levels, submit answers               |
| **Game – Boss**       | `/game/boss`                                    | save/get boss battle results                      |
| **Questions (admin)** | `/teacher/classrooms/{id}/levels/{id}/questions`| CRUD custom questions                             |
| **Leaderboard**       | `/leaderboard`                                  | global classroom ranking, per-classroom ranking   |
| **Notifications**     | `/teacher/notifications`                        | list, mark read, delete                           |
| **Images**            | `/images`, `/teacher/images`                    | upload image, list teacher images                 |
| **Medals**            | `/user/medalions`                               | award, list medals                                |
| **Notebook**          | `/v1/notes/me`                                  | get, create, delete notes                         |

---

## Testing

To run backend tests:

```bash
cd backend
mvn verify
```

To run frontend tests:

```bash
cd frontend
npm run test
```

### End-to-End Tests (Playwright)

Requires the backend to be running on `http://localhost:8080`.

```bash
cd frontend
npm run test:e2e
```

Playwright auto-starts the frontend dev server. Results and failure screenshots/videos are saved to `playwright-report/`.

---

## Wiki Structure

### Requirements

- [User Stories Overview](User-Stories-Overview)
- [Domain Model](Domain-Model)

### User Testing and Models

- [User Tests](User-Tests)

### System

- [Architecture](Architecture)
- [Project Structure](Project-Structure)
- [Class Diagram](Class-Diagram)
- [Server Services](Server-Services)
- [Database Model](Database-Model)
- [Source Code](Source-Code)
- [Installation](Installation)
- [CI and Testing](CI-and-Testing)
- [Security](Security)

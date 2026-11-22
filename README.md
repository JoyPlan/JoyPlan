# JoyPlan – AI-Powered Smart Family Experience System

SWE 401 Software Engineering · Abu Dhabi University · Fall 2026 · Instructor: Dr. Murad Al-Rajab

## Team members

| Name | Student ID |
|------|-----------|
| Shatha | 1098561 |
| Mashghona | 1091650 |
| Amena | 1093604 |

## Problem

Planning a family outing means juggling each child's age, interests, mood and energy together with the family's budget, location, food preferences and available time. Parents switch between many apps (maps, social media, venue sites, delivery apps) and still end up with plans that ignore tired children or go over budget. No existing app turns the children's needs and the family's limits into one complete, timed, personalised day plan.

## Key features

| ID | Feature | Requirement(s) |
|----|---------|----------------|
| F1 | Family profile: children's ages, interests and food preferences (no names or photos) | FR-01 |
| F2 | Outing details: mood, energy, budget, start area, maximum distance, time window, indoor/outdoor, adults, meal | FR-02 |
| F3 | Smart recommendations: activities, restaurants, snacks and breaks scored and ranked, each with a plain-language reason | FR-03, FR-13 |
| F4 | AI day plan: one timed plan built by Gemini (optional) or the built-in engine, then checked against budget, time and opening hours | FR-04, FR-05, NFR-07 |
| F5 | Edit: replace one stop with at least two alternatives, regenerate the plan, or tap **Kids are tired** | FR-06, FR-07, FR-08 |
| F6 | Save, rate, reuse, share with a family member and open Google Maps directions | FR-09, FR-10, FR-11 |
| Admin | Curated venue list: add, edit, verify, hide, delete; stale-data warning; statistics | FR-12, NFR-03 |
| Privacy | Account and data deletion, BCrypt passwords, role-based access, CSRF protection | NFR-04 |
| Offline | Installable web app; plan pages opened once stay available offline | NFR-06 |
| Accessibility | Larger text and high-contrast modes, labelled forms, keyboard focus styles | Phase 1 §1.6 |

## Technologies

- Java 17, Spring Boot 3.3 (Web MVC, Data JPA, Security, Validation)
- Thymeleaf + Bootstrap 5 (responsive, works on phones)
- H2 database (default, file-based, no installation) or MySQL 8 (profile `mysql`)
- Google Gemini API (optional; the app works fully without a key)
- JUnit 5, Spring Boot Test, MockMvc, AssertJ
- Google Maps directions links, GitHub

## Prerequisites

- JDK 17 or newer (`java -version`)
- Maven 3.9+ (`mvn -version`) **or** IntelliJ IDEA (bundled Maven)
- Internet access the first time, to download dependencies and the Bootstrap CDN files
- Optional: MySQL 8 server
- Optional: a free Gemini API key from Google AI Studio

## Installation

```bash
git clone https://github.com/JoyPlan/JoyPlan.git
cd JoyPlan
mvn clean install -DskipTests
```

In IntelliJ IDEA: **File → Open →** select `pom.xml` → **Open as Project**, then wait for Maven to finish importing.

## Configuration

All settings are in `src/main/resources/application.properties`. Environment variables override them.

| Variable | Purpose | Default |
|----------|---------|---------|
| `GEMINI_API_KEY` | Turns on AI plan composition with Gemini | empty (rule engine only) |
| `GEMINI_MODEL` | Gemini model name | `gemini-2.5-flash` |
| `PORT` | Web server port | `8080` |
| `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PASSWORD` | MySQL connection (profile `mysql` only) | `localhost`, `3306`, `root`, empty |

Without a Gemini key, JoyPlan uses its own recommendation and scheduling engine, so the demo never depends on the internet or API limits. With a key, Gemini chooses and explains the stops from JoyPlan's checked candidate list, and JoyPlan re-times and validates the result before showing it; if the AI answer is invalid, the engine's plan is used instead.

## How to run

```bash
mvn spring-boot:run
```

Windows PowerShell with AI:

```powershell
$env:GEMINI_API_KEY="your-key"; mvn spring-boot:run
```

macOS / Linux with AI:

```bash
GEMINI_API_KEY=your-key mvn spring-boot:run
```

With MySQL instead of H2:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

In IntelliJ: open `src/main/java/com/joyplan/JoyPlanApplication.java` and press **Run**.

Open <http://localhost:8080>. Demo accounts are created on first start:

| Role | Email | Password |
|------|-------|----------|
| Parent (2 children, ages 4 and 7) | parent@joyplan.ae | Parent@123 |
| Second parent (for sharing) | partner@joyplan.ae | Partner@123 |
| Administrator | admin@joyplan.ae | Admin@123 |

The database starts with 47 sample Abu Dhabi venues (19 activities, 9 restaurants, 9 snack stops, 10 break spots). Names are real family places, but prices, hours and coordinates are approximate sample data for the demo. To reset everything, stop the app and delete the `data/` folder.

### Suggested demo path

1. Sign in as `parent@joyplan.ae`, open **Family profile** and look at the two children.
2. **Plan a day** → mood *Tired*, energy *Low*, budget 150, *Indoor only*, 16:00–18:00 → **Generate**.
3. Read each stop's reason, cost and running total; click **Directions**.
4. Click **Replace** on a stop and choose one of the alternatives.
5. Mark the first stop **visited**, then tap **Kids are tired**.
6. **Save**, **Rate**, then **Share** with `partner@joyplan.ae`.
7. Sign out, sign in as `admin@joyplan.ae`, open **Admin** and verify a stale venue.

## How to test

```bash
mvn test
```

Reports are written to `target/surefire-reports/`. The suite contains 33 automated tests:

| Test class | What it checks |
|-----------|----------------|
| `PlanMathTest` | Distance, travel time, family cost (children under 3 free), time conversion |
| `PlanValidatorTest` | Budget, time window, overlap and opening-hours checks |
| `RecommendationServiceTest` | Ranking order, distance/budget/age/indoor filters, tired mode, sponsored labels, opening hours |
| `PlanServiceTest` | Plan within budget and time (SPEC-01), proposal example, ≥2 replacements that keep other stops (SPEC-02), Kids are tired, changes under 5 s (SPEC-03), save/rate/reuse/share, access control |
| `WebSecurityTest` | Login, role-based access, plan form, validation message, registration |

## Project structure

```
src/main/java/com/joyplan
├── config        SecurityConfig, DataSeeder, GlobalModelAdvice
├── controller    Home, Family, Plan, Admin controllers
├── dto           Registration, child and outing forms
├── model         JPA entities and enums
├── repository    Spring Data repositories
└── service       RecommendationService, RulePlanner, PlanValidator,
                  AiPlanService, PlanService, UserService, FamilyService, VenueService
src/main/resources
├── templates     Thymeleaf pages
└── static        CSS, JavaScript, service worker, manifest, icon
src/test/java/com/joyplan   JUnit tests
```

# CinemaBooking

A Java REST API for booking movie tickets — browsing shows, reserving seats with
concurrency-safe locking, and taking payment — backed by MySQL for persistence
and Redis for short-lived booking-queue state.

## Table of contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Data model](#data-model)
- [Booking concurrency logic](#booking-concurrency-logic)
- [Payment flow](#payment-flow)
- [Authentication](#authentication)
- [API reference](#api-reference)
- [Project structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Build and run](#build-and-run)
- [Testing](#testing)
- [Configuration](#configuration)
- [Known limitations](#known-limitations)

## Overview

CinemaBooking is a JAX-RS (Jersey) web application that models a simple cinema
booking system: movies, screens, halls and shows, and lets a user book seats
for a show and pay for them.

- **Movies / Screens / Halls / Shows** are plain data models backed by MySQL tables.
- **Booking** reserves seats for a user against a show, using Redis as a
  short-lived "who is trying to book what, right now" queue so that two users
  racing for the same seats are resolved deterministically (first-come,
  first-served) instead of double-booking.
- **Payment** is processed on a background thread with a timeout, and on
  success marks the user as paid and increments the hall's booked-seat count.

## Architecture

```
Client
  │  HTTP + Basic Auth
  ▼
Booking / Payment  (JAX-RS resources, com.cinema.services)
  │                              │
  ▼                              ▼
BookingConcurrency (Thread)   PaymentThread (Thread)
  │                              │
  ▼                              ▼
BookingService (singleton)    PaymentService (singleton)
  │                              │
  ▼                              ▼
Redis (booking queue)         MySQL (via DBOperations singleton)
```

- `DBOperations` (`com.cinema.util`) is a singleton wrapping all JDBC access
  (parameterized inserts/updates/selects) against MySQL.
- `RedisUtil` (`com.cinema.util`) wraps a Jedis client used purely as a
  transient queue for in-flight booking attempts per show.
- `BookingService` and `PaymentService` are singletons; the booking and
  payment REST endpoints each hand off work to a dedicated `Thread`
  (`BookingConcurrency`, `PaymentThread`) so the seat-conflict check and the
  (potentially slow) third-party payment call don't block the request thread
  indefinitely.
- `UserAuth` (`com.cinema.services`) performs HTTP Basic authentication
  against a single configured admin credential (see [Configuration](#configuration)).

## Data model

Five tables (DDL lives in `web/resources/*.sql`, applied in numeric order):

| Table | Columns | Notes |
|---|---|---|
| `Movie` (`000`→`001`) | `MOVIE_ID`, `NAME`, `DURATION`, `GENRE` | `NAME` is unique |
| `Screen` | `SCREEN_ID`, `TYPE`, `TIMING` | e.g. type = "3D", timing = "9:30am" |
| `Hall` | `HALL_ID`, `NAME`, `TOTAL_SEATS`, `SEATS_BOOKED`, `RATE` | `SEATS_BOOKED` is incremented on successful payment |
| `Shows` | `SHOW_ID`, `MOVIE_ID`, `SCREEN_ID`, `HALL_ID` | FKs to the three tables above; unique per (movie, screen, hall) |
| `Users` | `USER_ID`, `NAME`, `MOBILE_NUMBER`, `SEATS_BOOKED`, `SHOW_ID`, `IS_PAID` | one row per (name, mobile) booking a show; FK to `Shows`, cascades on delete |

Each Java model in `com.cinema.model` (`Movie`, `Screen`, `Hall`, `Shows`,
`Users`) mirrors one table and exposes `addXDetails()`, `updateXDetails()`
and `populateXDetails()` methods that go through `DBOperations`.

## Booking concurrency logic

`POST /cinema/booking` does the following:

1. Validates the request (max 6 seats per booking).
2. Pushes the incoming booking attempt (user, seats, timestamp) onto a
   per-show JSON array stored in Redis under the show's ID.
3. Spawns a `BookingConcurrency` thread which sleeps 1 second (to let other
   near-simultaneous requests for the same show land in the same Redis
   queue), then calls the synchronized `BookingService.bookTickets(...)`.
4. `bookTickets` scans the queue for *other* users on the same show and
   rejects the current attempt if:
   - another user's request timestamp is earlier (FIFO ordering), **and**
   - there is seat overlap with that earlier request.
5. Win or lose, `bookTickets` then removes *this attempt's own entry* from
   the show's Redis queue before returning, leaving any other still-pending
   attempts for that show untouched.
6. The calling thread `join()`s the booking thread and returns
   `Success` (proceed to payment) or `Error` (seats already taken, retry).
7. On success, the `Users` row is inserted/updated via
   `addOrUpdateUserDetails()` (looked up by name + mobile + show).

## Payment flow

`POST /cinema/payment`:

1. Loads the `Users` row for `userId`.
2. Runs `PaymentService.makePayment(...)` on a `PaymentThread`, joined with a
   **120-second timeout**. If it doesn't finish in time, the thread is
   interrupted and a `504 Gateway Timeout` is returned.
3. On success: marks the user `IS_PAID = true`, and increments
   `Hall.SEATS_BOOKED` by the number of seats booked.
4. `PaymentService.makePaymentUsingThirdParty()` is a stub that always
   returns `true` — this is the integration point for a real payment
   gateway.

## Authentication

Every endpoint requires **HTTP Basic Authentication**, checked in
`com.cinema.services.UserAuth` against the single admin credential defined
by `auth.username` / `auth.password` (see [Configuration](#configuration)):

- Username: `ticketAdmin` (default)
- Password: `admin@123` (default)

Requests without a valid `Authorization: Basic <base64(username:password)>`
header receive `401 Unauthorized`.

## API reference

Base path: `/cinema` (configured in `web/WEB-INF/web.xml`).

### `GET /cinema/booking?userId={userId}`

Fetch a user's booking details for their show.

- **Headers**: `Authorization: Basic <credentials>`
- **Query params**: `userId` (Long, required)
- **Response `200`**:
  ```json
  {
    "USER_NAME": "Sheldon Cooper",
    "USER_CONTACT": 9876543210,
    "SEAT_DETAILS": "1,3,4",
    "HALL_NAME": "INOX",
    "SHOW_TYPE": "3D",
    "SHOW_TIMINGS": "9:30am",
    "MOVIE_NAME": "Interstellar",
    "MOVIE_DURATION": 2.17,
    "MOVIE_GENRE": "Sci-fi"
  }
  ```
- **Response `401`**: `{"STATUS": "ERROR", "MESSAGE": "UNAUTHORIZED"}`

### `POST /cinema/booking`

Reserve seats for a show.

- **Headers**: `Authorization: Basic <credentials>`, `Content-Type: application/json`
- **Body**:
  ```json
  {
    "showId": "1",
    "seatNumbers": "1,3,4",
    "userName": "Sheldon Cooper",
    "mobileNumber": "9876543210"
  }
  ```
- **Responses**:
  | Status | Body | Meaning |
  |---|---|---|
  | `200` | `{"STATUS":"Success","MESSAGE":"Booking successful. Proceeding to payment!"}` | Seats reserved |
  | `200` | `{"STATUS":"Error","MESSAGE":"Tickets already booked, please try again!"}` | Lost the race to another booking |
  | `406` | `{"STATUS":"Error","MESSAGE":"Maximum of 6 seats can be booked"}` | Too many seats requested |
  | `401` | `{"STATUS":"ERROR","MESSAGE":"UNAUTHORIZED"}` | Missing/invalid credentials |
  | `500` | `{"STATUS":"Error","MESSAGE":"Error occurred while booking tickets"}` | Unexpected failure |

### `POST /cinema/payment`

Pay for an existing booking.

- **Headers**: `Authorization: Basic <credentials>`, `Content-Type: application/json`
- **Body**:
  ```json
  { "userId": "1" }
  ```
- **Responses**:
  | Status | Body | Meaning |
  |---|---|---|
  | `200` | `{"STATUS":"Success","MESSAGE":"Payment done successfully"}` | Payment completed |
  | `504` | `{"STATUS":"Error","MESSAGE":"Payment Timeout"}` | No response from payment step within 120s |
  | `401` | `{"STATUS":"ERROR","MESSAGE":"UNAUTHORIZED"}` | Missing/invalid credentials |
  | `500` | `{"STATUS":"Error","MESSAGE":"Error occurred while making payment"}` | Unexpected failure |

## Project structure

```
CinemaBooking/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/cinema/
│   │   │   ├── model/       Movie, Screen, Hall, Shows, Users (DB-backed entities)
│   │   │   ├── services/    Booking, Payment (REST endpoints); BookingService,
│   │   │   │                PaymentService (singletons); BookingConcurrency,
│   │   │   │                PaymentThread (Runnables); UserAuth
│   │   │   └── util/        DBOperations (MySQL access), RedisUtil (Jedis wrapper),
│   │   │                    AppConfig (config.properties + env var loader)
│   │   └── resources/config.properties   DB/Redis/auth defaults (see Configuration)
│   └── test/java/
│       ├── Main.java                     Manual scratch harness for exercising the models directly
│       └── com/cinema/
│           ├── services/UserAuthTest.java   JUnit tests for Basic Auth logic
│           └── util/AppConfigTest.java      JUnit tests for config loading
└── web/
    ├── WEB-INF/web.xml            Jersey servlet mapping (/cinema/*)
    ├── index.jsp                  Placeholder landing page ("WELCOME TO CINEMAS!!!!")
    └── resources/
        ├── *.sql                  Table DDL, apply in numeric order (000 → 004)
        └── SCHEMA:cinema.uml      IDE-generated ER diagram of the schema (not needed to build/run)
```

## Prerequisites

- JDK 8+ (the `pom.xml` currently targets Java 8 bytecode, but nothing in
  the code depends on JDK 8 specifically — auth uses `java.util.Base64`,
  which is available on every supported JDK)
- Apache Maven
- MySQL 8.x
- Redis (running on `127.0.0.1:6379`)
- A Servlet 3.1+ container, e.g. Apache Tomcat 8/9

## Setup

1. **Create the database and user**, matching the credentials in
   `DBOperations` (`jdbc:mysql://localhost:3306/cinema`, user `movie`,
   password `test`):
   ```sql
   CREATE DATABASE cinema;
   CREATE USER 'movie'@'localhost' IDENTIFIED BY 'test';
   GRANT ALL PRIVILEGES ON cinema.* TO 'movie'@'localhost';
   ```
2. **Apply the schema**, in order:
   ```bash
   mysql -u movie -p cinema < web/resources/000-Hall.sql
   mysql -u movie -p cinema < web/resources/001-Movie.sql
   mysql -u movie -p cinema < web/resources/002-Screen.sql
   mysql -u movie -p cinema < web/resources/003-Shows.sql
   mysql -u movie -p cinema < web/resources/004-Users.sql
   ```
3. **Start Redis** locally on the default port `6379`.
4. **Seed some data** — insert at least one row into `Movie`, `Screen` and
   `Hall`, then a `Shows` row referencing them, so there's something to book
   against. `src/test/java/Main.java` shows how to do this programmatically
   via the model classes if you'd rather not write raw SQL.

## Build and run

Build and deploy as a WAR:

```bash
mvn clean package
# copy the resulting WAR (and the web/ directory's WEB-INF) to your
# Tomcat webapps/ directory, or configure your IDE's Tomcat run
# configuration to use `web/` as the exploded web root.
```

Once deployed (default context root, port 8080), the API is reachable at:

```
http://localhost:8080/<context>/cinema/booking
http://localhost:8080/<context>/cinema/payment
```

Example request with `curl`:

```bash
curl -u ticketAdmin:admin@123 \
     -H "Content-Type: application/json" \
     -d '{"showId":"1","seatNumbers":"1,3,4","userName":"Sheldon Cooper","mobileNumber":"9876543210"}' \
     http://localhost:8080/cinema/booking
```

## Testing

```bash
mvn test
```

There's a JUnit 5 suite under `src/test/java/com/cinema/` covering the parts
of the codebase that don't require a live MySQL/Redis connection:

- **`UserAuthTest`** — Basic Auth header parsing and validation: correct
  credentials, wrong username/password, missing header, non-Basic scheme,
  malformed Base64, and a `Basic` scheme with no token after it.
- **`AppConfigTest`** — config loading: values come through from
  `config.properties`, unknown keys return `null`, numeric properties parse
  correctly. (Assumes no `DB_*`/`REDIS_*`/`AUTH_*` env vars are set locally,
  since those would override the file's defaults.)

`DBOperations`, `RedisUtil`, `BookingService`, `PaymentService` and the
model classes (`Movie`, `Screen`, `Hall`, `Shows`, `Users`) are not
unit-tested — they talk directly to a real MySQL connection and a real
Jedis client rather than through an interface that could be mocked, so
exercising them meaningfully needs a running MySQL + Redis (integration-test
territory, not covered here). `src/test/java/Main.java` remains as a manual
scratch harness for poking at the model classes against a real database by
hand.

## Configuration

DB, Redis and API-auth settings live in
`src/main/resources/config.properties`:

| Key | Default | Used by |
|---|---|---|
| `db.url` | `jdbc:mysql://localhost:3306/cinema` | `com.cinema.util.DBOperations` |
| `db.username` | `movie` | `com.cinema.util.DBOperations` |
| `db.password` | `test` | `com.cinema.util.DBOperations` |
| `redis.host` | `127.0.0.1` | `com.cinema.util.RedisUtil` |
| `redis.port` | `6379` | `com.cinema.util.RedisUtil` |
| `auth.username` | `ticketAdmin` | `com.cinema.services.UserAuth` |
| `auth.password` | `admin@123` | `com.cinema.services.UserAuth` |

Every key can be overridden without touching the file by setting an
environment variable of the same name, uppercased with `.` replaced by `_`
(e.g. `db.password` → `DB_PASSWORD`, `redis.host` → `REDIS_HOST`). Env vars
take precedence over `config.properties`, so a production deploy can leave
the file with its local-dev defaults and inject real secrets via the
environment instead. This lookup is implemented in `com.cinema.util.AppConfig`.

Two more settings are still plain constants in code, since they're
application behaviour rather than environment-specific secrets:

| Setting | Location | Value |
|---|---|---|
| Payment timeout | `com.cinema.services.Payment` | 120,000 ms |
| Max seats per booking | `com.cinema.services.Booking` | 6 |

## Known limitations

- **Automated test coverage is partial** — see [Testing](#testing): only
  the dependency-free logic (`UserAuth`, `AppConfig`) has a JUnit suite;
  the DB- and Redis-backed classes have no automated tests.

# LMSYS - Library Management System API

Spring Boot REST API for a Library Management System. It supports books, members, login, borrowing, returning, borrow history, fine calculation, and in-app due-date notifications.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Spring Boot 3.3.12 |
| Java | Java 21 |
| API | Spring Web REST controllers |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Password Hashing | Spring Security BCrypt |
| Boilerplate | Lombok |
| Build Tool | Maven |

## Base URL

```text
http://localhost:8080/api
```

All API endpoints are prefixed with `/api`.

## Current Security

Spring Security is configured for stateless JWT authentication.

Public endpoints:

- `POST /api/auth/login`
- `POST /api/members/register`

All other endpoints require:

```http
Authorization: Bearer <jwt-token>
```

The single login endpoint accepts both member and admin credentials. Member login uses `AuthenticationManager`, `DaoAuthenticationProvider`, `UserDetailsService`, and BCrypt password verification. Admin login checks the `admins` table and returns an admin JWT. Successful login returns a JWT and stores the token's `jti` as the account's active token ID. Logging in again replaces the active token ID, so the older token is rejected. Logout clears the active token ID.

Book write operations are admin-only:

- `POST /api/books`
- `PUT /api/books/{id}`
- `PATCH /api/books/**`
- `DELETE /api/books/{id}`

## Frontend Integration Notes

- CORS is configured in `CorsConfig.java` for `http://localhost:5173`.
- Allowed methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
- Allowed headers: all headers.
- The API returns JSON request and response bodies.
- Dates are returned as ISO date strings, for example `2026-06-01`.
- Validation and business errors return a consistent error object.
- Login uses email because email is unique for member/admin accounts.
- Passwords are stored only as BCrypt hashes and are never returned in responses.

Example frontend environment variable:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## Project Structure

```text
src/main/java/com/LMS/LMSYS
|-- config
|   |-- CorsConfig.java
|   `-- SecurityConfig.java
|-- controller
|   |-- AuthController.java
|   |-- BookController.java
|   |-- BorrowController.java
|   |-- MemberController.java
|   `-- NotificationController.java
|-- dto
|   |-- request
|   |   |-- BookRequest.java
|   |   |-- BorrowRequest.java
|   |   |-- LoginRequest.java
|   |   |-- MemberRequest.java
|   |   `-- ReturnBookRequest.java
|   `-- response
|       |-- AdminLoginResponse.java
|       |-- ApiErrorResponse.java
|       |-- BookResponse.java
|       |-- BorrowRecordResponse.java
|       |-- MemberLoginResponse.java
|       |-- MemberRegistrationResponse.java
|       |-- MemberResponse.java
|       `-- NotificationResponse.java
|-- entity
|   |-- Admin.java
|   |-- Book.java
|   |-- BorrowRecord.java
|   |-- Member.java
|   |-- Notification.java
|   `-- NotificationType.java
|-- exception
|   |-- BadRequestException.java
|   |-- ConflictException.java
|   |-- GlobalExceptionHandler.java
|   |-- ResourceNotFoundException.java
|   `-- UnauthorizedException.java
|-- mapper
|   |-- BookMapper.java
|   |-- BorrowRecordMapper.java
|   |-- MemberMapper.java
|   `-- NotificationMapper.java
|-- policy
|   `-- LendingPolicy.java
|-- repository
|   |-- AdminRepository.java
|   |-- BookRepository.java
|   |-- BorrowRecordRepository.java
|   |-- MemberRepository.java
|   `-- NotificationRepository.java
|-- service
|   |-- AdminAuthService.java
|   |-- AuthService.java
|   |-- BookService.java
|   |-- BorrowService.java
|   |-- DueSoonNotificationScheduler.java
|   |-- MemberService.java
|   `-- NotificationService.java
|-- security
|   |-- CustomUserDetailsService.java
|   |-- JwtAuthenticationFilter.java
|   `-- JwtService.java
`-- LmsysApplication.java
```

## Architecture

The application follows a layered architecture:

| Layer | Responsibility |
| --- | --- |
| Controller | Receives HTTP requests, validates request DTOs, and returns `ResponseEntity` responses. |
| Service | Contains business rules for books, members, borrowing, returning, history, notifications, and fine calculation. |
| Repository | Uses Spring Data JPA to query and persist PostgreSQL data. |
| Entity | Maps Java classes to database tables. |
| DTO | Keeps API request and response payloads separate from JPA entities. |
| Mapper | Converts entities into response DTOs. |
| Exception | Centralizes API error handling with `@RestControllerAdvice`. |
| Config | Defines CORS, BCrypt encoding, stateless JWT security, and JSON 401/403 handlers. |

## Database Model

### `books`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `BIGINT` | Primary key |
| `title` | `VARCHAR(255)` | Required |
| `author` | `VARCHAR(255)` | Required |
| `isbn` | `VARCHAR(255)` | Required, unique |
| `total_copies` | `INTEGER` | Required |
| `available_copies` | `INTEGER` | Required |

### `members`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `BIGINT` | Primary key |
| `name` | `VARCHAR(255)` | Required |
| `email` | `VARCHAR(255)` | Required, unique |
| `password` | `VARCHAR(255)` | Nullable for legacy rows; new registrations store a BCrypt hash |
| `current_token_id` | `VARCHAR(255)` | Active JWT `jti`; used to enforce one active token per member |
| `member_since` | `DATE` | Required, set during registration |

### `admins`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `BIGINT` | Primary key |
| `name` | `VARCHAR(255)` | Required |
| `email` | `VARCHAR(255)` | Required, unique |
| `password` | `VARCHAR(255)` | BCrypt hash |
| `current_token_id` | `VARCHAR(255)` | Active JWT `jti`; used to enforce one active token per admin |

### `borrow_records`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `BIGINT` | Primary key |
| `book_id` | `BIGINT` | Required, foreign key to `books.id` |
| `member_id` | `BIGINT` | Required, foreign key to `members.id` |
| `borrow_date` | `DATE` | Required |
| `return_date` | `DATE` | Nullable until returned |

There is a unique partial index that prevents a member from actively borrowing the same book more than once before returning it.

### `notifications`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `BIGINT` | Primary key |
| `member_id` | `BIGINT` | Required, foreign key to `members.id` |
| `borrow_record_id` | `BIGINT` | Required, foreign key to `borrow_records.id` |
| `message` | `TEXT` | Required |
| `notification_type` | `VARCHAR(50)` | Required |
| `created_at` | `TIMESTAMP` | Required |
| `is_read` | `BOOLEAN` | Required, defaults to `false` |
| `is_dismissed` | `BOOLEAN` | Required, defaults to `false` |

Core application tables are created by `src/main/resources/schema.sql`. The `admins` table must also exist in the configured database for admin login.

Hibernate DDL generation is disabled with:

```properties
spring.jpa.hibernate.ddl-auto=none
```

## Configuration

Create `src/main/resources/application.properties` or provide equivalent environment-specific configuration:

```properties
spring.application.name=LMSYS
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/libraryfinal
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always

jwt.secret=base64_encoded_32_byte_secret
jwt.expiration-ms=86400000
```

Set the database password before starting the app:

```env
DB_PASSWORD=your_password
```

## Run the Application

From the project root:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Build without running:

```bash
./mvnw clean package
```

Run the packaged jar:

```bash
java -jar target/LMSYS-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Books

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/books` | Get all books |
| `GET` | `/api/books?available=true` | Get books where `availableCopies > 0` |
| `GET` | `/api/books/{id}` | Get a book by ID |
| `POST` | `/api/books` | Add a new book. Requires `ROLE_ADMIN` |
| `PUT` | `/api/books/{id}` | Update a book by ID. Requires `ROLE_ADMIN` |
| `DELETE` | `/api/books/{id}` | Delete a book by ID. Requires `ROLE_ADMIN` |

### Members

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/members` | Get all members |
| `POST` | `/api/members/register` | Register a member |
| `GET` | `/api/members/{id}` | Get a member by ID |
| `GET` | `/api/members/{id}/history` | Get a member's borrow history |

### Authentication

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Log in with member or admin email and password |
| `POST` | `/api/auth/logout` | Log out and invalidate the current token |

### Borrowing

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/borrow` | Borrow a book |
| `POST` | `/api/return` | Return a borrowed book |
| `GET` | `/api/borrow-records` | Get all borrow records |
| `GET` | `/api/borrow-records/active` | Get active, unreturned borrow records |

### Notifications

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/members/{memberId}/notifications` | Get a member's in-app notifications |
| `PATCH` | `/api/notifications/{notificationId}/read` | Mark a notification as read |
| `DELETE` | `/api/notifications/{notificationId}` | Dismiss a notification |

## Request Bodies

### Add Book

`POST /api/books`

Requires an admin token.

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "totalCopies": 5,
  "availableCopies": 5
}
```

Validation:

| Field | Rule |
| --- | --- |
| `title` | Required, not blank |
| `author` | Required, not blank |
| `isbn` | Required, not blank, unique |
| `totalCopies` | Required, minimum `1` |
| `availableCopies` | Required, minimum `0`, cannot exceed `totalCopies` |

### Update Book

`PUT /api/books/{id}`

Requires an admin token. Uses the same request body and validation rules as add book.

### Delete Book

`DELETE /api/books/{id}`

Requires an admin token. A book can be deleted only when:

- `availableCopies == totalCopies`
- no active borrow records exist
- no borrow history exists

Successful delete returns `204 No Content`.

### Register Member

`POST /api/members/register`

```json
{
  "name": "Vijay",
  "email": "vijay@gmail.com",
  "password": "vijay123"
}
```

Validation:

| Field | Rule |
| --- | --- |
| `name` | Required, not blank |
| `email` | Required, valid email format, unique |
| `password` | Required, not blank; stored as a BCrypt hash |

Successful registration response:

```json
{
  "memberId": 7,
  "name": "Vijay",
  "email": "vijay@gmail.com",
  "memberSince": "2026-06-01"
}
```

### Login

`POST /api/auth/login`

```json
{
  "email": "vijay@gmail.com",
  "password": "vijay123"
}
```

Validation:

| Field | Rule |
| --- | --- |
| `email` | Required, valid email format |
| `password` | Required, not blank |

Successful member login response:

```json
{
  "role": "MEMBER",
  "memberId": 7,
  "name": "Vijay",
  "email": "vijay@gmail.com",
  "memberSince": "2026-06-01",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400000
}
```

Successful admin login response:

```json
{
  "role": "ADMIN",
  "adminId": 1,
  "name": "Admin",
  "email": "admin@example.com",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400000
}
```

Use the token as a Bearer token for protected endpoints. `expiresIn` is returned in milliseconds so the frontend can know when to re-authenticate without decoding the JWT.

### Borrow Book

`POST /api/borrow`

```json
{
  "bookId": 1,
  "memberId": 1
}
```

Validation:

| Field | Rule |
| --- | --- |
| `bookId` | Required, must reference an existing book |
| `memberId` | Required, must reference an existing member |

### Return Book

`POST /api/return`

```json
{
  "borrowId": 1,
  "memberId": 1
}
```

Validation:

| Field | Rule |
| --- | --- |
| `borrowId` | Required, must reference an existing borrow record |
| `memberId` | Required, must reference the returning member |

## Response Bodies

### `BookResponse`

```json
{
  "id": 1,
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "totalCopies": 5,
  "availableCopies": 4
}
```

### `MemberResponse`

```json
{
  "id": 1,
  "name": "Vijay",
  "email": "vijay@example.com",
  "memberSince": "2026-06-01"
}
```

### `BorrowRecordResponse`

```json
{
  "borrowId": 1,
  "bookId": 1,
  "bookTitle": "Clean Code",
  "memberId": 1,
  "memberName": "Vijay",
  "borrowDate": "2026-06-01",
  "returnDate": null,
  "status": "BORROWED",
  "fineAmount": 0
}
```

`returnDate` is `null` for active borrow records. `fineAmount` is calculated from the borrow date until today for active records, or until the return date for returned records.

### `NotificationResponse`

```json
{
  "notificationId": 1,
  "borrowId": 1,
  "memberId": 1,
  "bookTitle": "Clean Code",
  "message": "Book is due soon",
  "type": "DUE_SOON",
  "dueDate": "2026-06-15",
  "createdAt": "2026-06-01T09:00:00",
  "read": false
}
```

## Business Rules

| Rule | Behavior |
| --- | --- |
| Book must exist | Borrowing fails if `bookId` does not exist. |
| Member must exist | Borrowing fails if `memberId` does not exist. |
| Copies must be available | Borrowing fails when `availableCopies <= 0`. |
| Borrow updates inventory | Successful borrow decrements `availableCopies` by `1`. |
| Return updates inventory | Successful return increments `availableCopies` by `1` and sets `returnDate`. |
| Duplicate active borrow blocked | A member cannot borrow the same book again until it is returned. |
| Duplicate ISBN blocked | Adding a book with an existing ISBN returns conflict. |
| Book writes are admin-only | Adding, updating, patching, and deleting books require an admin token. |
| Book update uses a write lock | Updating book inventory fields uses a pessimistic write lock. |
| Book delete is restricted | Deleting a book requires all copies to be available and no borrow history. |
| Duplicate email blocked | Registering a member with an existing email returns conflict. |
| Already returned blocked | Returning the same borrow record twice returns bad request. |

## Fine Calculation

The service calculates a fine when a book is borrowed for more than 14 days.

| Condition | Fine |
| --- | --- |
| Returned within 14 days | `0` |
| 1 day late | `50` |
| Each additional late day | `+5` |

Formula:

```text
fine = 50 + ((lateDays - 1) * 5)
```

## Due-Date Notifications

Every day at `09:00 Asia/Kolkata`, the scheduler creates an in-app reminder for each active loan due today or within the next two days.

The due date is calculated from:

```text
borrowDate + 14 days
```

Deleting a notification dismisses it from the member's notification list. The database keeps a hidden dismissal flag so the scheduler does not recreate the same reminder.

## Status Codes

| Status | Meaning | Used For |
| --- | --- | --- |
| `200 OK` | Successful request | Fetching data, returning a book, marking notification read |
| `201 Created` | Resource created | Adding book, registering member, borrowing book |
| `204 No Content` | Successful delete | Deleting a book, dismissing a notification |
| `400 Bad Request` | Invalid input or invalid action | Validation failure, unavailable book, already returned record |
| `401 Unauthorized` | Login rejected | Invalid email or password |
| `401 Unauthorized` | Authentication failed | Missing, invalid, tampered, expired, or inactive JWT |
| `403 Forbidden` | Access denied | Valid JWT but insufficient permissions |
| `404 Not Found` | Missing resource | Book, member, borrow record, or notification not found |
| `409 Conflict` | Business conflict | Duplicate ISBN, duplicate email, duplicate active borrow, blocked book delete |
| `500 Internal Server Error` | Unexpected server error | Unhandled exceptions |

## Error Response

Errors use `ApiErrorResponse`:

```json
{
  "timestamp": "2026-06-01T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with id: 10",
  "path": "/api/books/10"
}
```

Validation errors combine field messages:

```json
{
  "timestamp": "2026-06-01T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "email: Email must be valid",
  "path": "/api/members/register"
}
```

## Quick API Test Commands

Get all books:

```bash
curl http://localhost:8080/api/books \
  -H "Authorization: Bearer <token>"
```

Add a book:

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d "{\"title\":\"Clean Code\",\"author\":\"Robert C. Martin\",\"isbn\":\"9780132350884\",\"totalCopies\":5,\"availableCopies\":5}"
```

Update a book:

```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d "{\"title\":\"Clean Code\",\"author\":\"Robert C. Martin\",\"isbn\":\"9780132350884\",\"totalCopies\":6,\"availableCopies\":6}"
```

Delete a book:

```bash
curl -X DELETE http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer <admin-token>"
```

Register a member:

```bash
curl -X POST http://localhost:8080/api/members/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Vijay\",\"email\":\"vijay@gmail.com\",\"password\":\"vijay123\"}"
```

Login as a member or admin:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"vijay@gmail.com\",\"password\":\"vijay123\"}"
```

Logout:

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <token>"
```

Borrow a book:

```bash
curl -X POST http://localhost:8080/api/borrow \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{\"bookId\":1,\"memberId\":1}"
```

Return a book:

```bash
curl -X POST http://localhost:8080/api/return \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{\"borrowId\":1,\"memberId\":1}"
```

Get member history:

```bash
curl http://localhost:8080/api/members/1/history \
  -H "Authorization: Bearer <token>"
```

Get member notifications:

```bash
curl http://localhost:8080/api/members/1/notifications \
  -H "Authorization: Bearer <token>"
```

Mark notification as read:

```bash
curl -X PATCH http://localhost:8080/api/notifications/1/read \
  -H "Authorization: Bearer <token>"
```

Dismiss notification:

```bash
curl -X DELETE http://localhost:8080/api/notifications/1 \
  -H "Authorization: Bearer <token>"
```

## Frontend Checklist

Use these endpoints and fields directly in your frontend:

| UI Feature | Endpoint |
| --- | --- |
| Book list | `GET /api/books` |
| Available books view | `GET /api/books?available=true` |
| Book details | `GET /api/books/{id}` |
| Add book form | `POST /api/books` |
| Update book form | `PUT /api/books/{id}` |
| Delete book action | `DELETE /api/books/{id}` |
| Member list | `GET /api/members` |
| Member registration form | `POST /api/members/register` |
| Member/admin login form | `POST /api/auth/login` |
| Member/admin logout action | `POST /api/auth/logout` |
| Member profile | `GET /api/members/{id}` |
| Member borrow history | `GET /api/members/{id}/history` |
| Borrow form | `POST /api/borrow` |
| Return action | `POST /api/return` |
| Notification list | `GET /api/members/{memberId}/notifications` |
| Mark notification as read | `PATCH /api/notifications/{notificationId}/read` |
| Dismiss notification | `DELETE /api/notifications/{notificationId}` |
| Admin borrow records view | `GET /api/borrow-records` |
| Active borrow records view | `GET /api/borrow-records/active` |

Recommended frontend handling:

- Show `message` from error responses in form or toast errors.
- After registration, display the member ID from `response.memberId`.
- After login, store `response.token` and send it as `Authorization: Bearer ${token}` for protected requests.
- Use `response.role` to show member or admin UI. Only show book create/update/delete controls for `ADMIN`.
- Use `response.expiresIn` to schedule re-authentication or token cleanup.
- On logout, call `POST /api/auth/logout`, then remove the stored token and member state.
- Treat `400`, `401`, `404`, and `409` as expected user-facing errors.
- Refresh book lists after borrow or return because `availableCopies` changes.
- Use `borrowId` and `memberId` when calling the return endpoint.
- Display active records when `returnDate` is `null` or `status` indicates an active borrow.

## Testing Checklist

- Add a valid book.
- Confirm a member token cannot add, update, or delete a book.
- Confirm an admin token can add, update, and delete a book.
- Update a book by ID.
- Delete a book with no borrow history and all copies available.
- Try deleting a book while copies are borrowed.
- Try deleting a book with borrow history.
- Try adding a duplicate ISBN.
- Get all books.
- Filter available books.
- Get a book by valid and invalid ID.
- Register a valid member.
- Try registering a duplicate email.
- Register two members with the same name and different emails.
- Log in with a valid email and password.
- Try logging in with an invalid email or password.
- Confirm login returns `token` and `expiresIn`.
- Confirm `/api/books` without a token returns `401`.
- Confirm `/api/books` with a valid Bearer token returns `200`.
- Confirm an invalid token returns `401`.
- Confirm a tampered token returns `401`.
- Confirm an expired token returns `401`.
- Log in twice and confirm the first token is rejected with `401`.
- Log out and confirm the logged-out token is rejected with `401`.
- Confirm registration and login responses never include a password.
- Get all members.
- Get a member by valid and invalid ID.
- Borrow an available book.
- Try borrowing a missing book.
- Try borrowing with a missing member.
- Try borrowing a book with `availableCopies = 0`.
- Try borrowing the same book twice before returning it.
- Return a borrowed book.
- Try returning a missing borrow record.
- Try returning an already returned record.
- Confirm `availableCopies` decreases after borrow and increases after return.
- Confirm member history includes active and returned records.
- Confirm `/api/borrow-records/active` only shows unreturned records.
- Confirm due-date notifications can be fetched, marked as read, and dismissed.

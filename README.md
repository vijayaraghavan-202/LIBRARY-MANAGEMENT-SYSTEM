# Mini Library Management System — Spring Boot Project Specification

## 1. Project Overview

Build a fully functional REST API for a **Library Management System** using **Spring Boot**.

This project focuses on combining the following backend concepts into one complete application:

- REST API design
- Relational database mapping
- Layered architecture
- Business logic validation
- Error handling
- API testing using Postman or curl

This specification is based on the given assignment, with the following changes:

- Database changed from **H2 In-Memory** to **PostgreSQL**
- Build tool fixed as **Maven**
- All other requirements remain the same

---

## 2. Tech Stack

| Component    | Technology                  |
| ------------ | --------------------------- |
| Framework    | Spring Boot 3.x             |
| ORM          | Spring Data JPA + Hibernate |
| Database     | PostgreSQL                  |
| Build Tool   | Maven                       |
| Testing Tool | Postman or curl             |
| Java Version | Java 17 or above            |

---

## 3. Data Model

The system has three main entities:

1. `Book`
2. `Member`
3. `BorrowRecord`

---

## 3.1 Book Entity

Represents a book available in the library.

| Field             | Type      | Description                                        |
| ----------------- | --------- | -------------------------------------------------- |
| `id`              | `Long`    | Primary key, auto-generated                        |
| `title`           | `String`  | Title of the book                                  |
| `author`          | `String`  | Author name                                        |
| `isbn`            | `String`  | Unique ISBN identifier                             |
| `totalCopies`     | `Integer` | Total number of copies owned by the library        |
| `availableCopies` | `Integer` | Number of copies currently available for borrowing |

### Important Notes

- `isbn` must be unique.
- `availableCopies` should never be greater than `totalCopies`.
- `availableCopies` should never become negative.

---

## 3.2 Member Entity

Represents a registered library member.

| Field         | Type        | Description                 |
| ------------- | ----------- | --------------------------- |
| `id`          | `Long`      | Primary key, auto-generated |
| `name`        | `String`    | Full name of the member     |
| `email`       | `String`    | Unique email address        |
| `memberSince` | `LocalDate` | Date of registration        |

### Important Notes

- `email` must be unique.
- `memberSince` can be automatically set when a member is registered.

---

## 3.3 BorrowRecord Entity

Represents the borrowing history of books by members.

| Field        | Type        | Description                                                                   |
| ------------ | ----------- | ----------------------------------------------------------------------------- |
| `id`         | `Long`      | Primary key, auto-generated                                                   |
| `book`       | `Book`      | Many-to-one relationship with `Book`                                          |
| `member`     | `Member`    | Many-to-one relationship with `Member`                                        |
| `borrowDate` | `LocalDate` | Date when the book was borrowed                                               |
| `returnDate` | `LocalDate` | Date when the book was returned. It is `null` if the book is not yet returned |

### Relationship Mapping

- Many `BorrowRecord` entries can belong to one `Book`.
- Many `BorrowRecord` entries can belong to one `Member`.
- Use proper JPA relationships such as `@ManyToOne`.

---

## 4. API Endpoints

## 4.1 Book APIs

| Method | Endpoint      | Description                       |
| ------ | ------------- | --------------------------------- |
| `GET`  | `/books`      | Retrieve all books in the library |
| `POST` | `/books`      | Add a new book to the library     |
| `GET`  | `/books/{id}` | Retrieve a specific book by ID    |

### Optional Stretch Endpoint

| Method | Endpoint                | Description                                    |
| ------ | ----------------------- | ---------------------------------------------- |
| `GET`  | `/books?available=true` | Retrieve only books with `availableCopies > 0` |

---

## 4.2 Member APIs

| Method | Endpoint                | Description                                |
| ------ | ----------------------- | ------------------------------------------ |
| `POST` | `/members`              | Register a new library member              |
| `GET`  | `/members/{id}`         | Retrieve member details by ID              |
| `GET`  | `/members/{id}/history` | Get complete borrowing history of a member |

---

## 4.3 Borrowing APIs

| Method | Endpoint             | Description                                    |
| ------ | -------------------- | ---------------------------------------------- |
| `POST` | `/borrow`            | Borrow a book and enforce all borrowing rules  |
| `POST` | `/return/{borrowId}` | Return a borrowed book and update availability |

---

## 5. Business Rules

All business rules must be implemented inside the **Service layer**, not inside the Controller.

| Rule   | Description                                                                   |
| ------ | ----------------------------------------------------------------------------- |
| Rule 1 | Book must exist before a borrow request is accepted                           |
| Rule 2 | `availableCopies` must be greater than `0` before allowing a borrow           |
| Rule 3 | On successful borrow, decrement `availableCopies` by `1`                      |
| Rule 4 | On successful return, increment `availableCopies` by `1` and set `returnDate` |
| Rule 5 | A member cannot borrow the same book twice without returning it first         |

---

## 6. HTTP Status Codes

Return meaningful HTTP status codes for all responses.

| Status Code       | Meaning                       | Example Usage                                         |
| ----------------- | ----------------------------- | ----------------------------------------------------- |
| `200 OK`          | Request successful            | Getting books, returning a book                       |
| `201 Created`     | Resource created successfully | Creating book, registering member, borrowing book     |
| `400 Bad Request` | Invalid request               | Invalid input data                                    |
| `404 Not Found`   | Resource not found            | Book, member, or borrow record not found              |
| `409 Conflict`    | Business rule conflict        | Borrowing unavailable book or duplicate active borrow |

---

## 7. Architecture Constraints

The project must follow a three-layer architecture.

## 7.1 Controller Layer

Use `@RestController`.

Responsibilities:

- Handle HTTP requests
- Return HTTP responses
- Call the Service layer

The Controller must not contain business logic.

---

## 7.2 Service Layer

Use `@Service`.

Responsibilities:

- Apply all business rules
- Validate borrow and return logic
- Handle application-level decisions
- Throw meaningful exceptions when needed

Rules 1 to 5 must be implemented here.

---

## 7.3 Repository Layer

Use `@Repository` or extend `JpaRepository`.

Responsibilities:

- Perform database operations
- Query entities from PostgreSQL

Example repositories:

```java
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
}
```

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
```

```java
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    boolean existsByBookIdAndMemberIdAndReturnDateIsNull(Long bookId, Long memberId);
    List<BorrowRecord> findByMemberId(Long memberId);
}
```

---

## 8. Suggested Package Structure

```text
src/main/java/com/example/library
│
├── controller
│   ├── BookController.java
│   ├── MemberController.java
│   └── BorrowController.java
│
├── service
│   ├── BookService.java
│   ├── MemberService.java
│   └── BorrowService.java
│
├── repository
│   ├── BookRepository.java
│   ├── MemberRepository.java
│   └── BorrowRecordRepository.java
│
├── entity
│   ├── Book.java
│   ├── Member.java
│   └── BorrowRecord.java
│
├── dto
│   ├── BorrowRequest.java
│   └── ApiResponse.java
│
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ConflictException.java
│
└── LibraryApplication.java
```

---

## 9. PostgreSQL Configuration

Add PostgreSQL database configuration in `src/main/resources/application.properties`.

```properties
spring.application.name=library-management-system

spring.datasource.url=jdbc:postgresql://localhost:5432/library_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Database Setup

Create the PostgreSQL database before running the application:

```sql
CREATE DATABASE library_db;
```

Update the username and password according to your local PostgreSQL setup.

---

## 10. Maven Dependencies

Add the following dependencies in `pom.xml`.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 11. Example Request Bodies

## 11.1 Add Book

`POST /books`

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "totalCopies": 5,
  "availableCopies": 5
}
```

---

## 11.2 Register Member

`POST /members`

```json
{
  "name": "Vijay",
  "email": "vijay@example.com"
}
```

---

## 11.3 Borrow Book

`POST /borrow`

```json
{
  "bookId": 1,
  "memberId": 1
}
```

---

## 11.4 Return Book

`POST /return/{borrowId}`

Example:

```text
POST /return/1
```

---

## 12. Error Handling

Use a global exception handler with `@ControllerAdvice`.

Expected error cases:

- Book not found
- Member not found
- Borrow record not found
- No copies available
- Member already borrowed the same book and has not returned it
- Returning an already returned book
- Duplicate ISBN
- Duplicate member email

Example error response:

```json
{
  "message": "Book not found with id: 10",
  "status": 404,
  "timestamp": "2026-05-26T10:30:00"
}
```

---

## 13. Suggested Timeline

| Day(s)  | Milestone  | Deliverable                                                         |
| ------- | ---------- | ------------------------------------------------------------------- |
| Day 1–2 | Foundation | Entity design, PostgreSQL setup, basic CRUD for Book and Member     |
| Day 3–4 | Core Logic | Implement `/borrow` and `/return` with all 5 business rules         |
| Day 5   | Hardening  | Error handling, HTTP status codes, endpoint testing through Postman |
| Day 6–7 | Polish     | Code cleanup, optional stretch goals, prepare for review            |

---

## 14. Stretch Goals

Complete these only after the core assignment works properly.

- Add filter: `GET /books?available=true`
- Add fine calculation if a book is returned more than 14 days after `borrowDate`
- Add Swagger/OpenAPI documentation using `@Operation` and `@ApiResponse`

---

## 15. Testing Checklist

Test the following cases using Postman or curl.

### Book Tests

- Add a valid book
- Get all books
- Get book by valid ID
- Get book by invalid ID
- Try adding a book with duplicate ISBN

### Member Tests

- Register a valid member
- Get member by valid ID
- Get member by invalid ID
- Try registering a member with duplicate email

### Borrow Tests

- Borrow an available book
- Try borrowing a non-existing book
- Try borrowing with a non-existing member
- Try borrowing a book with `availableCopies = 0`
- Try borrowing the same book twice without returning it

### Return Tests

- Return a borrowed book
- Try returning with invalid borrow ID
- Try returning an already returned book
- Check whether `availableCopies` increases after return

### History Tests

- Get borrowing history of a member
- Verify active and returned borrow records are shown correctly

---

## 16. Submission Checklist

Before submitting, make sure you have:

- Working Spring Boot project
- PostgreSQL database configured and working
- Maven build working successfully
- REST endpoints tested using Postman or curl
- Proper entity relationships using JPA
- Business logic only inside Service layer
- Meaningful HTTP status codes and error messages
- Postman collection showing all endpoints and edge cases
- README file explaining how to run the project and use the endpoints

---

## 17. Run Instructions

### Step 1: Create PostgreSQL Database

```sql
CREATE DATABASE library_db;
```

### Step 2: Update Database Credentials

Edit `application.properties`:

```properties
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### Step 3: Run the Project

Using Maven:

```bash
mvn spring-boot:run
```

Or build and run:

```bash
mvn clean install
java -jar target/library-management-system-0.0.1-SNAPSHOT.jar
```

### Step 4: Test APIs

Use Postman or curl to test:

```text
GET http://localhost:8080/books
POST http://localhost:8080/books
POST http://localhost:8080/members
POST http://localhost:8080/borrow
POST http://localhost:8080/return/{borrowId}
GET http://localhost:8080/members/{id}/history
```

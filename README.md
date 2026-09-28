# Book Management REST API

## 1. Project Overview
A Spring Boot REST API to manage a catalogue of books. It supports full CRUD, search, price filtering,
pagination and sorting, request validation, centralized error handling and Swagger documentation.
Data is stored in a **MySQL** database and the code follows a layered architecture:

```
Controller  ->  Service  ->  Repository  ->  MySQL Database
```

## 2. Use Case / Entity Details
**Entity: `Book`** (table `books`)

| Field           | Type       | Constraints (DB)                 | Validation (DTO)                                   |
|-----------------|------------|----------------------------------|----------------------------------------------------|
| `id`            | Long       | Primary key, auto-increment      | -                                                  |
| `title`         | String     | NOT NULL, max 150                | `@NotBlank`, `@Size(max=150)`                      |
| `author`        | String     | NOT NULL, max 100                | `@NotBlank`, `@Size(max=100)`                      |
| `isbn`          | String     | NOT NULL, UNIQUE                 | `@NotBlank`, `@Pattern` (digits and hyphens, 10-17)|
| `price`         | BigDecimal | NOT NULL, (10,2)                 | `@NotNull`, `@DecimalMin(>0)`, `@Digits`           |
| `publishedYear` | Integer    | NOT NULL                         | `@NotNull`, `@Min(1000)`, `@Max(2100)`             |

DTOs: `BookRequest` (input, validated) and `BookResponse` (output).

## 3. Technologies Used
- Java 17, Spring Boot 3.3.4, Gradle
- Spring Web, Spring Data JPA (Hibernate), Spring Validation
- MySQL 8 + MySQL Connector/J
- springdoc-openapi (Swagger UI)
- Docker

## 4. API Endpoints
Base URL: `http://localhost:8080`

| Method | Endpoint                                              | Description                              | Success |
|--------|-------------------------------------------------------|------------------------------------------|---------|
| POST   | `/api/books`                                          | Create a book                            | 201     |
| GET    | `/api/books?page=0&size=5&sortBy=price&direction=desc`| Get all books (paginated, sorted)        | 200     |
| GET    | `/api/books/{id}`                                     | Get a book by ID                         | 200     |
| PUT    | `/api/books/{id}`                                     | Update a book                            | 200     |
| DELETE | `/api/books/{id}`                                     | Delete a book                            | 204     |
| GET    | `/api/books/search/title?keyword=spring`              | Search by title (derived query)          | 200     |
| GET    | `/api/books/search/author?keyword=martin`             | Search by author (derived query)         | 200     |
| GET    | `/api/books/price-range?min=100&max=500`              | Filter by price range (custom `@Query`)  | 200     |

Sample request body (`POST` / `PUT`):
```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "price": 450.00,
  "publishedYear": 2008
}
```

Sample validation error (400):
```json
{
  "timestamp": "2026-09-28T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": { "title": "Title is required", "price": "Price must be greater than 0" }
}
```

## 5. Additional Features
1. **Derived queries** - `findByTitleContainingIgnoreCase`, `findByAuthorContainingIgnoreCase`, `existsByIsbn`
2. **Custom `@Query`** - JPQL price-range query in `BookRepository`
3. **Global exception handling** - `@RestControllerAdvice` returning 404 (not found), 409 (duplicate ISBN), 400 (validation / bad params), 500
4. **Pagination and sorting** - `page`, `size`, `sortBy`, `direction` parameters (max page size 50)
5. **Swagger / OpenAPI** - interactive docs at `/swagger-ui.html`
6. Duplicate ISBN protection on create and update

## 6. MySQL Setup on Ubuntu
```bash
# 1. Install MySQL server
sudo apt update
sudo apt install mysql-server -y

# 2. Start it and enable on boot
sudo systemctl start mysql
sudo systemctl enable mysql
sudo systemctl status mysql        # should show "active (running)"

# 3. Log in as root (uses auth_socket on Ubuntu, so use sudo)
sudo mysql
```
Inside the MySQL shell:
```sql
CREATE DATABASE bookdb;
CREATE USER 'bookuser'@'localhost' IDENTIFIED BY 'BookPass@123';
GRANT ALL PRIVILEGES ON bookdb.* TO 'bookuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```
Verify the new user works:
```bash
mysql -u bookuser -p -e "SHOW DATABASES;"      # enter BookPass@123
```
Tables are created automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`) on first run.
After running the app you can inspect data with:
```bash
mysql -u bookuser -p bookdb -e "SELECT * FROM books;"
```

## 7. How to Run the Application
Prerequisites: JDK 17 (`sudo apt install openjdk-17-jdk -y`) and MySQL configured as above.

```bash
git clone <your-repo-url> && cd book-api
chmod +x gradlew
./gradlew bootRun or gradle bootRun (If gradle is already present)
```
Then open:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API: http://localhost:8080/api/books

Quick test:
```bash
curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","isbn":"978-0132350884","price":450,"publishedYear":2008}'
curl "http://localhost:8080/api/books?page=0&size=5&sortBy=price&direction=desc"
```
Build a jar: `./gradlew bootJar` (output in `build/libs/`).

## 8. Docker Information
A multi-stage `Dockerfile` is in the project root (Gradle + JDK 17 build stage, JRE 17 runtime stage, port 8080).
```bash
docker build -t book-api .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/bookdb?useSSL=false&allowPublicKeyRetrieval=true" \
  -e DB_USERNAME=bookuser -e DB_PASSWORD='BookPass@123' book-api
```

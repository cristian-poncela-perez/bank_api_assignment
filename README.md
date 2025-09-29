# Bank REST API

A Spring Boot 3 REST service for managing users and their bank accounts.

## Features

- User management with unique email validation
- Bank account management with balance controls
- Role-based account access (PRIMARY and AUTHORIZED users)
- Account metrics and balance aggregation
- OpenAPI/Swagger documentation
- Comprehensive test coverage

## Technology Stack

- **Java 17**
- **Spring Boot 3.3.13**
- **Spring Data JPA** with Hibernate
- **H2 Database** (in-memory)
- **MapStruct 1.5.5** for DTO mapping
- **SpringDoc OpenAPI 2.6.0** for API documentation
- **Maven** for build management

## Prerequisites

- JDK 17 or higher
- Maven 3.6+

## Getting Started

### Build the project

```bash
mvn clean install
```

### Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Access Swagger UI

Navigate to: `http://localhost:8080/swagger-ui.html`

### Access H2 Console (Development)

Navigate to: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:bankdb`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Users

| Method | Endpoint              | Description                                  |
| ------ | --------------------- | -------------------------------------------- |
| GET    | `/users`              | List all users                               |
| GET    | `/users/{id}`         | Get user by ID                               |
| POST   | `/users`              | Create a new user                            |
| PUT    | `/users/{id}`         | Update user details                          |
| DELETE | `/users/{id}`         | Delete user (only if no accounts)            |
| GET    | `/users/{id}/balance` | Get user's total balance across all accounts |

### Accounts

| Method | Endpoint                                   | Description                              |
| ------ | ------------------------------------------ | ---------------------------------------- |
| GET    | `/accounts`                                | List all accounts                        |
| GET    | `/accounts/{id}`                           | Get account by ID                        |
| POST   | `/accounts`                                | Create a new account                     |
| PUT    | `/accounts/{id}`                           | Update account details                   |
| PATCH  | `/accounts/{id}/balance`                   | Update account balance                   |
| DELETE | `/accounts/{id}`                           | Delete account (only if balance is zero) |
| POST   | `/accounts/{id}/authorized-users`          | Add authorized user to account           |
| DELETE | `/accounts/{id}/authorized-users/{userId}` | Remove authorized user from account      |

### Metrics

| Method | Endpoint            | Description                                  |
| ------ | ------------------- | -------------------------------------------- |
| GET    | `/metrics/accounts` | Get account statistics by balance conditions |

**Query Parameters for Metrics:**

- `greaterThan` - Count accounts with balance > value
- `lessThan` - Count accounts with balance < value
- Both parameters can be combined for range queries

## Data Model

### User

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "accounts": [...]
}
```

### Account

```json
{
  "id": 1,
  "accountNumber": "ACC-001",
  "balance": 1000.00,
  "users": [...]
}
```

### Account-User Relationship

- **PRIMARY**: The account owner (exactly one per account)
- **AUTHORIZED**: Users with access rights (zero or more per account)

## Business Rules

1. **Email Uniqueness**: User emails must be unique (case-insensitive)
2. **Account Numbers**: Account numbers must be unique
3. **Balance Validation**: Account balance must be non-negative
4. **User Deletion**: Users cannot be deleted if they have associated accounts
5. **Account Deletion**: Accounts can only be deleted if balance is zero
6. **Primary User**: Each account must have exactly one PRIMARY user

## Example Requests

### Create User

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }'
```

**Response (201 Created):**

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "accounts": []
}
```

### Create Account

```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "ACC-001",
    "balance": 1000.00,
    "primaryUserId": 1
  }'
```

**Response (201 Created):**

```json
{
  "id": 1,
  "accountNumber": "ACC-001",
  "balance": 1000.0,
  "users": [
    {
      "userId": 1,
      "userName": "John Doe",
      "userEmail": "john.doe@example.com",
      "role": "PRIMARY"
    }
  ]
}
```

### Update Account Balance

```bash
curl -X PATCH http://localhost:8080/accounts/1/balance \
  -H "Content-Type: application/json" \
  -d '{
    "balance": 1500.00
  }'
```

### Add Authorized User to Account

```bash
curl -X POST http://localhost:8080/accounts/1/authorized-users \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2
  }'
```

**Response (200 OK):**

```json
{
  "id": 1,
  "accountNumber": "ACC-001",
  "balance": 1500.0,
  "users": [
    {
      "userId": 1,
      "userName": "John Doe",
      "userEmail": "john.doe@example.com",
      "role": "PRIMARY"
    },
    {
      "userId": 2,
      "userName": "Jane Smith",
      "userEmail": "jane.smith@example.com",
      "role": "AUTHORIZED"
    }
  ]
}
```

### Get User Balance

```bash
curl http://localhost:8080/users/1/balance
```

**Response (200 OK):**

```json
{
  "userId": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "totalBalance": 2500.0,
  "accounts": [
    {
      "accountId": 1,
      "accountNumber": "ACC-001",
      "balance": 1500.0,
      "role": "PRIMARY"
    },
    {
      "accountId": 2,
      "accountNumber": "ACC-002",
      "balance": 1000.0,
      "role": "AUTHORIZED"
    }
  ]
}
```

### Get Account Metrics

```bash
# Accounts with balance > 1000
curl "http://localhost:8080/metrics/accounts?greaterThan=1000"
```

**Response (200 OK):**

```json
{
  "count": 5,
  "condition": "balance > 1000"
}
```

```bash
# Accounts with balance between 500 and 2000
curl "http://localhost:8080/metrics/accounts?greaterThan=500&lessThan=2000"
```

**Response (200 OK):**

```json
{
  "count": 3,
  "condition": "balance > 500 AND balance < 2000"
}
```

### Update User

```bash
curl -X PUT http://localhost:8080/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "email": "john.updated@example.com"
  }'
```

### Delete Account (must have zero balance)

```bash
# First, set balance to zero
curl -X PATCH http://localhost:8080/accounts/1/balance \
  -H "Content-Type: application/json" \
  -d '{"balance": 0}'

# Then delete
curl -X DELETE http://localhost:8080/accounts/1
```

**Response (200 OK):**

```json
{
  "message": "Account deleted successfully",
  "timestamp": 1234567890
}
```

### Delete User (must have no accounts)

```bash
curl -X DELETE http://localhost:8080/users/1
```

**Response (200 OK):**

```json
{
  "message": "User deleted successfully",
  "timestamp": 1234567890
}
```

### List All Users

```bash
curl http://localhost:8080/users
```

### List All Accounts

```bash
curl http://localhost:8080/accounts
```

## Testing

### Run all tests

```bash
mvn test
```

### Test Coverage

- **Integration Tests**: 76 tests covering all controllers
- **Unit Tests**: Service and repository layer tests
- **Mapper Tests**: MapStruct transformation tests

The project includes:

- `UserControllerIntegrationTest` - 28 tests
- `AccountControllerIntegrationTest` - 34 tests
- `MetricsControllerIntegrationTest` - 14 tests
- `UserServiceTest` - Unit tests with Mockito
- `AccountRepositoryTest` - Custom query tests
- `UserMapperTest` - DTO mapping tests

## OpenAPI Documentation

### Generate OpenAPI Specification

The OpenAPI specification can be generated in three ways:

**Option 1: Using the shell script (Recommended)**

```bash
chmod +x generate-openapi.sh  # Make executable (first time only)
./generate-openapi.sh
```

- Starts the Spring Boot application automatically
- Waits for the service to be ready
- Fetches the OpenAPI spec
- **Output location**: `docs/openapi.json`
- Stops the application when done

**Option 2: Using the Maven test**

```bash
mvn test -Dtest=OpenApiGenerationTest
```

- Runs a specific test that generates the spec
- Uses the `openapi` profile
- **Output location**: `target/openapi.json`
- Faster than Option 1 if dependencies are already downloaded

**Option 3: Manual download (requires running application)**

```bash
# First, start the application
mvn spring-boot:run

# Then, in another terminal:
curl http://localhost:8080/v3/api-docs -o openapi.json
```

- Requires the application to be running
- **Output location**: Current directory as `openapi.json`
- Most flexible for custom output locations

## Project Structure

```
src/
├── main/
│   ├── java/com/bank/
│   │   ├── advice/           # Global exception handler
│   │   ├── controller/       # REST controllers
│   │   ├── domain/          # JPA entities
│   │   ├── dto/
│   │   │   ├── request/     # Request DTOs
│   │   │   └── response/    # Response DTOs
│   │   ├── exception/       # Custom exceptions
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── repository/      # Spring Data repositories
│   │   └── service/         # Business logic layer
│   └── resources/
│       ├── application.yml
│       └── data.sql
└── test/
    ├── java/com/bank/
    │   ├── controller/      # Integration tests
    │   ├── documentation/   # OpenAPI generation
    │   ├── integration/     # Cross-layer integration tests
    │   ├── mapper/         # Mapper tests
    │   ├── repository/     # Repository tests
    │   └── service/        # Service unit tests
    └── resources/
        ├── application-test.yml
        └── application-openapi.yml
```

## Error Handling

The API returns consistent error responses:

### Validation Error (400)

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": 1234567890,
  "errors": {
    "email": "Email should be valid",
    "balance": "Balance must be positive or zero"
  }
}
```

### Resource Not Found (404)

```json
{
  "status": 404,
  "message": "User not found with ID: 999",
  "timestamp": 1234567890
}
```

### Conflict (409)

```json
{
  "status": 409,
  "message": "User already exists with email: john@example.com",
  "timestamp": 1234567890
}
```

## Configuration Profiles

- **default**: Main application configuration
- **test**: Test environment with separate H2 database
- **openapi**: Configuration for OpenAPI spec generation

## 📌 Notes

- **Default H2 database** is in-memory and auto-initialized with `data.sql` (though currently using `ddl-auto: create-drop`)
- **Error messages** are centralized in `exception/ErrorMessages.java` for consistency and easier maintenance
- **DTOs and mappers** ensure a clean separation between entities and API responses using MapStruct
- **OpenAPI documentation** is generated dynamically at runtime from annotations and can be exported to a file
- **Email normalization** happens automatically - all emails are stored in lowercase and trimmed
- **Ordered responses** - PRIMARY users/accounts always appear before AUTHORIZED in response arrays

## Future Improvements

### Security

- Add Spring Security with JWT authentication
- Implement role-based access control (RBAC)
- Add API rate limiting
- Implement audit logging for sensitive operations

### Features

- Transaction history tracking (deposits, withdrawals, transfers)
- Account-to-account transfers
- Pagination and sorting for list endpoints
- Advanced search and filtering capabilities
- Email notifications for account activities
- Multi-currency support

### Technical Enhancements

- Migrate to PostgreSQL for production
- Add Redis caching for frequently accessed data
- Implement async processing for heavy operations
- Add metrics and monitoring (Actuator, Prometheus, Grafana)
- Containerization with Docker
- CI/CD pipeline setup
- API versioning strategy

### Code Quality

- Increase test coverage to >90%
- Add mutation testing with PITest
- Implement ArchUnit for architecture validation
- Add performance testing with JMeter
- Code quality gates with SonarQube

### Database

- Add database migration management (Flyway or Liquibase)
- Implement soft delete for audit trail
- Add database connection pooling optimization
- Create database indexes for query optimization

### Documentation

- Add architecture decision records (ADRs)
- Create entity relationship diagrams (ERD)
- Add Postman collection for API testing
- Include API usage examples in multiple languages

## License

This project is created as a technical exercise for educational purposes.

## Contact

cristian.poncela.perez@gmail.com

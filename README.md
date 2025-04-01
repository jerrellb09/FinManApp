# FinManApp - Financial Management Application

A comprehensive personal finance management system that helps users track finances, manage budgets, monitor bills, and gain financial insights. This Java Spring Boot application provides a robust backend API for financial data management.

## Features

- **Account Management**: Connect and manage financial accounts
- **Transaction Tracking**: Monitor spending across all accounts
- **Budget Management**: Create and track spending against custom budgets
- **Bill Management**: Track recurring and one-time bills with due date reminders
- **Spending Insights**: AI-powered analysis of spending patterns and habits
- **Budget Recommendations**: Smart budget suggestions based on spending history
- **Secure Authentication**: JWT-based authentication and authorization
- **Multi-platform**: RESTful API enabling connection from web and mobile clients

## System Architecture

### Backend Components

- **Core Application**: Spring Boot application with RESTful API endpoints
- **Data Layer**: Spring Data JPA for database operations
- **Security**: JWT-based authentication with Spring Security
- **AI Integration**: LLaMA 3 integration for financial insights and recommendations
- **External Services**: Plaid API integration for connecting to financial institutions

### Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Database**: PostgreSQL (production) / H2 (development)
- **Security**: JSON Web Tokens (JWT)
- **Build Tool**: Maven
- **Containerization**: Docker and Docker Compose
- **Testing**: Spring Test, JUnit

## Prerequisites

- Java 21 or higher
- Docker and Docker Compose (for PostgreSQL database)
- Maven 3.8+
- Git

## Getting Started

You can run the application with either PostgreSQL (persistent data) or H2 (in-memory database).

### Quick Start (Auto-detects best option)

Run the startup script which will use PostgreSQL if Docker is available, or fallback to H2:

```bash
./start.sh
```

### Option 1: PostgreSQL Database (Persistent)

This option requires Docker to be installed.

1. Set up PostgreSQL database using our helper script:

```bash
./fix-postgres.sh
```

This creates a Docker container with:
- Database: tradingbotv2
- Username: postgres
- Password: postgres
- Port: 5432

2. Build and run the application:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Troubleshooting PostgreSQL Connection Issues

If you see the error "database 'tradingbotv2' does not exist", run the fix script:

```bash
./fix-postgres.sh
```

To verify the database connection is working properly:

```bash
./check-db.sh
```

### Option 2: H2 In-Memory Database (Non-Persistent)

This option doesn't require Docker, but data will be lost when the application stops.

```bash
./mvnw clean install
./mvnw spring-boot:run -Dspring.profiles.active=h2
```

You can access the H2 console at: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: password

### What Happens on Startup

The application will:
1. Connect to the selected database
2. Create all necessary tables
3. Seed the database with sample data (if empty)
4. Start the web server on port 8080

## Sample Data

When started for the first time, the application will seed the database with:

- Categories (Housing, Food, Transportation, etc.)
- Two users:
  - Email: test@example.com / Password: password
  - Email: jane@example.com / Password: password
- Sample accounts for each user
- Sample budgets
- Sample transactions
- Sample bills

## API Endpoints

### Authentication

- `POST /api/auth/login` - Login with email and password
- `POST /api/auth/register` - Register a new user

### Accounts

- `GET /api/accounts` - Get all accounts for the current user
- `POST /api/accounts` - Add a new account
- `GET /api/accounts/{id}` - Get a specific account
- `PUT /api/accounts/{id}` - Update an account
- `DELETE /api/accounts/{id}` - Delete an account

### Transactions

- `GET /api/transactions` - Get all transactions
- `POST /api/transactions` - Add a new transaction
- `GET /api/transactions/{id}` - Get a specific transaction
- `PUT /api/transactions/{id}` - Update a transaction
- `DELETE /api/transactions/{id}` - Delete a transaction

### Budgets

- `GET /api/budgets` - Get all budgets
- `POST /api/budgets` - Create a new budget
- `GET /api/budgets/{id}` - Get a specific budget
- `PUT /api/budgets/{id}` - Update a budget
- `DELETE /api/budgets/{id}` - Delete a budget

### Bills

- `GET /api/bills/user/{userId}` - Get all bills for a user
- `GET /api/bills/due/{userId}` - Get bills due for a user
- `POST /api/bills?userId={userId}` - Create a new bill
- `PUT /api/bills/{billId}` - Update a bill
- `DELETE /api/bills/{billId}` - Delete a bill
- `PATCH /api/bills/{billId}/pay` - Mark a bill as paid
- `GET /api/bills/remaining-income/{userId}` - Get remaining income after bills
- `POST /api/bills/reset-monthly/{userId}` - Reset monthly bills (mark as unpaid)

### Insights

- `GET /api/insights/financial/{userId}` - Get financial insights for a user
- `GET /api/insights/budget-suggestions/{userId}` - Get budget suggestions
- `GET /api/insights/spending-analysis/{userId}` - Get spending habit analysis

## Security

The application uses JWT (JSON Web Token) for authentication. All protected endpoints require a valid JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

The token is obtained by calling the login endpoint with valid credentials.

## Project Structure

- `src/main/java/com/jay/home/finmanapp/` - Main application code
  - `config/` - Application configuration
  - `controller/` - API endpoints
  - `dto/` - Data Transfer Objects
  - `mapper/` - Mappers between entities and DTOs
  - `model/` - Entity classes
  - `repository/` - Data access layer
  - `security/` - JWT authentication components
  - `service/` - Business logic
  - `util/` - Utility classes

## Development

### Adding a New Feature

1. Create any necessary model classes
2. Create repository interfaces
3. Implement service layer logic
4. Create controllers for API endpoints
5. Add security configurations if needed
6. Write tests for the new functionality

### Running Tests

```bash
./mvnw test
```

### Building for Production

```bash
./mvnw clean package -Pprod
```

This will create a standalone JAR file in the `target` directory.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/awesome-feature`)
3. Commit your changes (`git commit -m 'Add awesome feature'`)
4. Push to the branch (`git push origin feature/awesome-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Boot team for the excellent framework
- Plaid for financial data integration capabilities
- All contributors who have helped improve this project
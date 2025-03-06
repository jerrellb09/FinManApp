# TradingBotV2

A personal finance tracking and budget management application with features for account integration, transaction tracking, budget management, and bill management.

## Prerequisites

- Java 21
- Docker and Docker Compose (for PostgreSQL database)
- Maven

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

- POST /api/auth/login - Login with email and password
- POST /api/auth/register - Register a new user

### Accounts

- GET /api/accounts - Get all accounts for the current user
- POST /api/accounts - Add a new account
- GET /api/accounts/{id} - Get a specific account
- PUT /api/accounts/{id} - Update an account
- DELETE /api/accounts/{id} - Delete an account

### Transactions

- GET /api/transactions - Get all transactions
- POST /api/transactions - Add a new transaction
- GET /api/transactions/{id} - Get a specific transaction
- PUT /api/transactions/{id} - Update a transaction
- DELETE /api/transactions/{id} - Delete a transaction

### Budgets

- GET /api/budgets - Get all budgets
- POST /api/budgets - Create a new budget
- GET /api/budgets/{id} - Get a specific budget
- PUT /api/budgets/{id} - Update a budget
- DELETE /api/budgets/{id} - Delete a budget

### Bills

- GET /api/bills/user/{userId} - Get all bills for a user
- GET /api/bills/due/{userId} - Get bills due for a user
- POST /api/bills?userId={userId} - Create a new bill
- PUT /api/bills/{billId} - Update a bill
- DELETE /api/bills/{billId} - Delete a bill
- PATCH /api/bills/{billId}/pay - Mark a bill as paid
- GET /api/bills/remaining-income/{userId} - Get remaining income after bills
- POST /api/bills/reset-monthly/{userId} - Reset monthly bills (mark as unpaid)
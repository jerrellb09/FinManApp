# Database Setup for TradingbotV2

This document explains the database options for the TradingbotV2 application.

## Option 1: Using PostgreSQL (Recommended for Production)

PostgreSQL provides a persistent database solution ideal for production use. 

### Setup Instructions:

1. Make sure you have Docker installed and running
2. Run the setup script:
   ```
   ./fix-postgres.sh
   ```
3. Start the application:
   ```
   ./mvnw spring-boot:run
   ```

### Manual Setup (if Docker is not available):

1. Install PostgreSQL on your system
2. Create a database named `tradingbotv2`
3. Update the connection details in `application.properties` if needed

## Option 2: Using H2 Database (For Development Only)

H2 is an in-memory database that's perfect for development and testing. Note that all data will be lost when the application stops.

### Current Issues:

We're experiencing issues with the Flyway migration when running with H2. Until this is resolved, we recommend using PostgreSQL or disabling Flyway completely by modifying the `application-h2.properties` file.

### To Use H2 (once fixed):

```
./mvnw spring-boot:run -Dspring.profiles.active=h2
```

## Troubleshooting

If you encounter the error "database does not exist", try the following:

1. Run the fix script: `./fix-postgres.sh`
2. Manually create the database:
   ```sql
   CREATE DATABASE tradingbotv2;
   ```
3. Fall back to H2 for development (once fixed)

## Database Schema

The application uses the following tables:
- users: User accounts and authentication
- accounts: Financial accounts linked to users
- transactions: Financial transactions
- categories: Transaction categories
- budgets: User budgets by category
- bills: Recurring bills and payments
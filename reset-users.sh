#!/bin/bash

# Ensure postgresql client is installed
if ! command -v psql &> /dev/null; then
    echo "PostgreSQL client is not installed. Please install it first."
    exit 1
fi

# Set the PostgreSQL connection parameters
DB_NAME="tradingbotv2"
DB_USER="postgres"
DB_PASSWORD="postgres"

# Check if PostgreSQL is running
echo "Checking if PostgreSQL is running..."
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "PostgreSQL is not running. Please start it first."
    echo "You can use: brew services start postgresql"
    exit 1
fi

# Check if database exists
echo "Checking if database exists..."
if ! PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USER -lqt | cut -d \| -f 1 | grep -qw $DB_NAME; then
    echo "Database $DB_NAME does not exist! Creating it..."
    PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USER -c "CREATE DATABASE $DB_NAME;"
    echo "Database created."
else
    echo "Database exists."
fi

# Delete and recreate users
echo "Recreating users table..."
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USER -d $DB_NAME -c "DROP TABLE IF EXISTS users CASCADE;"
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USER -d $DB_NAME -c "
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    monthly_income DECIMAL(19,4),
    payday_day INT
);"

# Insert test users with BCrypt password (password is 'password')
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USER -d $DB_NAME -c "
INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day) 
VALUES 
('test@example.com', '\$2a\$10\$vIiV7hLXi9VXgZTQnk0RDucjyPslpd9ZZjuSvXq7Xtfiv3V4R5AYO', 'Test', 'User', 5000.00, 15),
('drdoompaid13@gmail.com', '\$2a\$10\$vIiV7hLXi9VXgZTQnk0RDucjyPslpd9ZZjuSvXq7Xtfiv3V4R5AYO', 'Dr', 'Doom', 7000.00, 15);"

echo "Users table recreated with test users."
echo "Listing users:"
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT id, email, first_name, last_name FROM users;"

echo "You can now restart the application and login with:"
echo "- Email: drdoompaid13@gmail.com"
echo "- Password: password"
echo "OR"
echo "- Email: test@example.com"
echo "- Password: password"
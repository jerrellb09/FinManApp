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

# Password "password" hashed with BCrypt
BCRYPT_HASH='$2a$10$vIiV7hLXi9VXgZTQnk0RDucjyPslpd9ZZjuSvXq7Xtfiv3V4R5AYO'

# SQL query to add a user
SQL_QUERY="INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day) 
            VALUES ('drdoompaid13@gmail.com', '$BCRYPT_HASH', 'Dr', 'Doom', 5000.00, 15);"

# Execute the SQL query
echo "Adding user to the database:"
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USER -d $DB_NAME -c "$SQL_QUERY"

echo "Listing all users in the database:"
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USER -d $DB_NAME -c "SELECT id, email, first_name, last_name FROM users;"
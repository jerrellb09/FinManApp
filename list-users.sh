#!/bin/bash

# Ensure postgresql client is installed
if ! command -v psql &> /dev/null; then
    echo "PostgreSQL client is not installed. Please install it first."
    exit 1
fi

# Set the PostgreSQL connection parameters
DB_NAME="finmanapp"
DB_USER="postgres"
DB_PASSWORD="postgres"

# SQL query to list all users
SQL_QUERY="SELECT id, email, first_name, last_name FROM users;"

# Execute the SQL query
echo "Listing all users in the database:"
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USER -d $DB_NAME -c "$SQL_QUERY"
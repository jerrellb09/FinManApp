-- Create the database if it doesn't exist
CREATE DATABASE tradingbotv2 WITH OWNER postgres;

-- Connect to the database
\c tradingbotv2

-- Create necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
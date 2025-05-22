-- Create the database if it doesn't exist
CREATE DATABASE finmanapp WITH OWNER postgres;

-- Connect to the database
\c finmanapp

-- Create necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
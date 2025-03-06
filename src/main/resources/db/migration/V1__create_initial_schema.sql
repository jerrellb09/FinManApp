-- Initial database schema for TradingBotV2 application

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    monthly_income DECIMAL(19, 4),
    payday_day INTEGER
);

-- Create categories table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    icon_url VARCHAR(255)
);

-- Create accounts table
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    account_id VARCHAR(255) NOT NULL,
    access_token VARCHAR(255) NOT NULL,
    institution_id VARCHAR(255) NOT NULL,
    institution_name VARCHAR(255) NOT NULL,
    last_synced TIMESTAMP NOT NULL
);

-- Create budgets table
CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    period VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    warning_threshold DECIMAL(5, 2) NOT NULL
);

-- Create transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    transaction_id VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    date TIMESTAMP NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    is_manual_entry BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create bills table
CREATE TABLE bills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    due_day INTEGER NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    is_recurring BOOLEAN NOT NULL DEFAULT TRUE,
    category_id BIGINT REFERENCES categories(id)
);

-- Create notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    budget_id BIGINT NOT NULL REFERENCES budgets(id),
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE
);

-- Add indexes for better performance
CREATE INDEX idx_user_id ON accounts(user_id);
CREATE INDEX idx_account_id ON transactions(account_id);
CREATE INDEX idx_category_id ON transactions(category_id);
CREATE INDEX idx_user_id_bills ON bills(user_id);
CREATE INDEX idx_transaction_date ON transactions(date);
CREATE INDEX idx_notification_user_id ON notifications(user_id);
CREATE INDEX idx_budget_user_id ON budgets(user_id);
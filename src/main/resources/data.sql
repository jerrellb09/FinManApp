-- This SQL script will be automatically executed when using H2 database (in-memory)

-- Insert default categories
INSERT INTO categories (name, description, icon_url) VALUES 
('Housing', 'Rent, mortgage, property taxes, utilities, home insurance, repairs', 'house-fill'),
('Transportation', 'Car payments, gas, public transit, vehicle maintenance, insurance', 'car-front-fill'),
('Food', 'Groceries, restaurants, meal delivery', 'basket-fill'),
('Entertainment', 'Streaming services, events, activities', 'film'),
('Healthcare', 'Insurance, medications, doctor visits', 'heart-pulse-fill'),
('Personal', 'Clothing, personal care, haircuts', 'person-fill'),
('Education', 'Tuition, books, courses, student loans', 'book-fill'),
('Savings', 'Emergency fund, investments, retirement', 'piggy-bank-fill'),
('Debt', 'Credit card payments, loans', 'credit-card-fill'),
('Travel', 'Flights, accommodations, vacation expenses', 'airplane-fill'),
('Shopping', 'Retail purchases, household items', 'bag-fill'),
('Utilities', 'Electricity, water, gas, internet, phone', 'phone-fill'),
('Gifts', 'Birthday, holiday, special occasions', 'gift-fill'),
('Taxes', 'Income tax, property tax, other taxes', 'cash-stack'),
('Insurance', 'Life, health, home, auto insurance', 'shield-fill'),
('Subscriptions', 'Recurring subscriptions, memberships', 'calendar-check-fill'),
('Other', 'Miscellaneous expenses that don't fit elsewhere', 'three-dots'),
('Income', 'Salary, side hustle, investments, etc.', 'cash');

-- Create test users with encrypted password (password123)
INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day)
VALUES ('test@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Test', 'User', 5000.00, 15);

INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day)
VALUES ('jane@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Jane', 'Smith', 6500.00, 1);

-- Insert sample bills for first user
INSERT INTO bills (name, amount, due_day, is_paid, is_recurring, user_id, category_id)
VALUES 
('Rent', 1200.00, 1, true, true, 1, 1),
('Electricity', 75.00, 15, false, true, 1, 12),
('Internet', 60.00, 20, false, true, 1, 12),
('Cell Phone', 85.00, 5, true, true, 1, 12),
('Netflix', 13.99, 7, false, true, 1, 16),
('Gym Membership', 45.00, 10, false, true, 1, 16);

-- Insert sample bills for second user
INSERT INTO bills (name, amount, due_day, is_paid, is_recurring, user_id, category_id)
VALUES 
('Mortgage', 1500.00, 5, true, true, 2, 1),
('Car Payment', 350.00, 12, false, true, 2, 2),
('Water & Sewage', 45.00, 18, false, true, 2, 12),
('Internet & Cable', 120.00, 22, false, true, 2, 12),
('Streaming Services', 35.99, 25, false, true, 2, 16);

-- Create sample accounts
INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
VALUES 
(1, 'Checking Account', 'CHECKING', 2500.00, 'acc_123456', 'access_token_checking', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP),
(1, 'Savings Account', 'SAVINGS', 10000.00, 'acc_789012', 'access_token_savings', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP),
(1, 'Credit Card', 'CREDIT', -1500.00, 'acc_345678', 'access_token_cc', 'ins_67890', 'Credit Bank', CURRENT_TIMESTAMP),
(2, 'Primary Checking', 'CHECKING', 3500.00, 'acc_jane123', 'access_token_jane_checking', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP),
(2, 'High-Yield Savings', 'SAVINGS', 15000.00, 'acc_jane456', 'access_token_jane_savings', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP);

-- Insert sample budgets
INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
VALUES 
(1, 'Monthly Rent', 1200.00, 1, 'MONTHLY', CURRENT_DATE, DATEADD('YEAR', 1, CURRENT_DATE), 90.00),
(1, 'Groceries', 500.00, 3, 'MONTHLY', CURRENT_DATE, DATEADD('YEAR', 1, CURRENT_DATE), 80.00),
(1, 'Entertainment', 200.00, 4, 'MONTHLY', CURRENT_DATE, DATEADD('YEAR', 1, CURRENT_DATE), 85.00),
(1, 'Eating Out', 300.00, 3, 'MONTHLY', CURRENT_DATE, DATEADD('YEAR', 1, CURRENT_DATE), 75.00),
(2, 'Housing', 1500.00, 1, 'MONTHLY', CURRENT_DATE, DATEADD('YEAR', 1, CURRENT_DATE), 90.00),
(2, 'Food Budget', 600.00, 3, 'MONTHLY', CURRENT_DATE, DATEADD('YEAR', 1, CURRENT_DATE), 80.00),
(2, 'Transportation', 400.00, 2, 'MONTHLY', CURRENT_DATE, DATEADD('YEAR', 1, CURRENT_DATE), 85.00);

-- Insert sample transactions
INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
VALUES 
(1, 'tx_12345', 'Whole Foods Market', -85.27, DATEADD('DAY', -5, CURRENT_DATE), 3, false),
(1, 'tx_23456', 'Amazon.com', -29.99, DATEADD('DAY', -3, CURRENT_DATE), 11, false),
(1, 'tx_34567', 'Netflix', -13.99, DATEADD('DAY', -7, CURRENT_DATE), 16, false),
(1, 'tx_45678', 'Shell Gas Station', -45.00, DATEADD('DAY', -2, CURRENT_DATE), 2, false),
(1, 'tx_56789', 'Rent Payment', -1200.00, DATEADD('DAY', -15, CURRENT_DATE), 1, false),
(1, 'tx_67890', 'Salary Deposit', 2500.00, DATEADD('DAY', -15, CURRENT_DATE), 18, false),
(2, 'tx_78901', 'Interest Payment', 5.25, DATEADD('DAY', -1, CURRENT_DATE), 18, false),
(3, 'tx_89012', 'Restaurant Payment', -62.47, DATEADD('DAY', -4, CURRENT_DATE), 3, false),
(3, 'tx_90123', 'Online Shopping', -59.99, DATEADD('DAY', -6, CURRENT_DATE), 11, false),
(4, 'tx_jane1', 'Trader Joe''s', -92.45, DATEADD('DAY', -2, CURRENT_DATE), 3, false),
(4, 'tx_jane2', 'Monthly Transit Pass', -120.00, DATEADD('DAY', -10, CURRENT_DATE), 2, false),
(4, 'tx_jane3', 'Paycheck', 3250.00, DATEADD('DAY', -15, CURRENT_DATE), 18, false),
(5, 'tx_jane4', 'Interest Earned', 12.50, DATEADD('DAY', -1, CURRENT_DATE), 18, false);
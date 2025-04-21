-- Seed data for TradingBotV2 application

-- Insert default categories with ON CONFLICT DO NOTHING to avoid duplicates
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
('Other', 'Miscellaneous expenses that don''t fit elsewhere', 'three-dots'),
('Income', 'Salary, side hustle, investments, etc.', 'cash')
ON CONFLICT (name) DO NOTHING;

-- Create a test user with encrypted password (password = "password")
INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day)
VALUES ('test@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Test', 'User', 5000.00, 15)
ON CONFLICT (email) DO NOTHING;

-- Create a second user
INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day)
VALUES ('jane@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Jane', 'Smith', 6500.00, 1)
ON CONFLICT (email) DO NOTHING;

-- Add sample accounts for first user
INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
VALUES 
(1, 'Checking Account', 'CHECKING', 2500.00, 'acc_123456', 'access_token_checking', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP),
(1, 'Savings Account', 'SAVINGS', 10000.00, 'acc_789012', 'access_token_savings', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP),
(1, 'Credit Card', 'CREDIT', -1500.00, 'acc_345678', 'access_token_cc', 'ins_67890', 'Credit Bank', CURRENT_TIMESTAMP)
ON CONFLICT (account_id) DO NOTHING;

-- Add sample accounts for second user
INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
VALUES 
(2, 'Primary Checking', 'CHECKING', 3500.00, 'acc_jane123', 'access_token_jane_checking', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP),
(2, 'High-Yield Savings', 'SAVINGS', 15000.00, 'acc_jane456', 'access_token_jane_savings', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP)
ON CONFLICT (account_id) DO NOTHING;

-- Add sample budgets for first user
INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
VALUES 
(1, 'Monthly Rent', 1200.00, 1, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 90.00),
(1, 'Groceries', 500.00, 3, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 80.00),
(1, 'Entertainment', 200.00, 4, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 85.00),
(1, 'Eating Out', 300.00, 3, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 75.00)
ON CONFLICT (user_id, name, period) DO NOTHING;

-- Add sample budgets for second user
INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
VALUES 
(2, 'Housing', 1500.00, 1, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 90.00),
(2, 'Food Budget', 600.00, 3, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 80.00),
(2, 'Transportation', 400.00, 2, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 85.00)
ON CONFLICT (user_id, name, period) DO NOTHING;

-- Add sample transactions for first user
INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
VALUES 
(1, 'tx_12345', 'Whole Foods Market', -85.27, CURRENT_DATE - INTERVAL '5 days', 3, FALSE),
(1, 'tx_23456', 'Amazon.com', -29.99, CURRENT_DATE - INTERVAL '3 days', 11, FALSE),
(1, 'tx_34567', 'Netflix', -13.99, CURRENT_DATE - INTERVAL '7 days', 16, FALSE),
(1, 'tx_45678', 'Shell Gas Station', -45.00, CURRENT_DATE - INTERVAL '2 days', 2, FALSE),
(1, 'tx_56789', 'Rent Payment', -1200.00, CURRENT_DATE - INTERVAL '15 days', 1, FALSE),
(1, 'tx_67890', 'Salary Deposit', 2500.00, CURRENT_DATE - INTERVAL '15 days', 18, FALSE),
(2, 'tx_78901', 'Interest Payment', 5.25, CURRENT_DATE - INTERVAL '1 day', 18, FALSE),
(3, 'tx_89012', 'Restaurant Payment', -62.47, CURRENT_DATE - INTERVAL '4 days', 3, FALSE),
(3, 'tx_90123', 'Online Shopping', -59.99, CURRENT_DATE - INTERVAL '6 days', 11, FALSE)
ON CONFLICT (transaction_id) DO NOTHING;

-- Add sample transactions for second user
INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
VALUES 
(4, 'tx_jane1', 'Trader Joe''s', -92.45, CURRENT_DATE - INTERVAL '2 days', 3, FALSE),
(4, 'tx_jane2', 'Monthly Transit Pass', -120.00, CURRENT_DATE - INTERVAL '10 days', 2, FALSE),
(4, 'tx_jane3', 'Paycheck', 3250.00, CURRENT_DATE - INTERVAL '15 days', 18, FALSE),
(5, 'tx_jane4', 'Interest Earned', 12.50, CURRENT_DATE - INTERVAL '1 day', 18, FALSE)
ON CONFLICT (transaction_id) DO NOTHING;

-- Add sample bills for first user
INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
VALUES 
(1, 'Rent', 1200.00, 1, TRUE, TRUE, 1),
(1, 'Electricity', 75.00, 15, FALSE, TRUE, 12),
(1, 'Internet', 60.00, 20, FALSE, TRUE, 12),
(1, 'Cell Phone', 85.00, 5, TRUE, TRUE, 12),
(1, 'Netflix', 13.99, 7, FALSE, TRUE, 16),
(1, 'Gym Membership', 45.00, 10, FALSE, TRUE, 16)
ON CONFLICT (user_id, name) DO NOTHING;

-- Add sample bills for second user
INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
VALUES 
(2, 'Mortgage', 1500.00, 5, TRUE, TRUE, 1),
(2, 'Car Payment', 350.00, 12, FALSE, TRUE, 2),
(2, 'Water & Sewage', 45.00, 18, FALSE, TRUE, 12),
(2, 'Internet & Cable', 120.00, 22, FALSE, TRUE, 12),
(2, 'Streaming Services', 35.99, 25, FALSE, TRUE, 16)
ON CONFLICT (user_id, name) DO NOTHING;

-- Add sample notifications
INSERT INTO notifications (user_id, budget_id, message, sent_at, is_read)
VALUES 
(1, 3, 'You have reached 85% of your Entertainment budget for this month', CURRENT_TIMESTAMP - INTERVAL '2 days', TRUE),
(1, 2, 'You have reached 90% of your Groceries budget for this month', CURRENT_TIMESTAMP - INTERVAL '1 day', FALSE),
(2, 6, 'You have reached 75% of your Food Budget for this month', CURRENT_TIMESTAMP - INTERVAL '3 days', FALSE)
ON CONFLICT (user_id, budget_id, sent_at) DO NOTHING;
-- Seed data for TradingBotV2 application

-- Insert default categories with safety check to avoid duplicates
DO $$ 
BEGIN
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Housing', 'Rent, mortgage, property taxes, utilities, home insurance, repairs', 'house-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Housing');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Transportation', 'Car payments, gas, public transit, vehicle maintenance, insurance', 'car-front-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Transportation');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Food', 'Groceries, restaurants, meal delivery', 'basket-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Food');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Entertainment', 'Streaming services, events, activities', 'film'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Entertainment');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Healthcare', 'Insurance, medications, doctor visits', 'heart-pulse-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Healthcare');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Personal', 'Clothing, personal care, haircuts', 'person-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Personal');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Education', 'Tuition, books, courses, student loans', 'book-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Education');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Savings', 'Emergency fund, investments, retirement', 'piggy-bank-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Savings');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Debt', 'Credit card payments, loans', 'credit-card-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Debt');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Travel', 'Flights, accommodations, vacation expenses', 'airplane-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Travel');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Shopping', 'Retail purchases, household items', 'bag-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Shopping');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Utilities', 'Electricity, water, gas, internet, phone', 'phone-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Utilities');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Gifts', 'Birthday, holiday, special occasions', 'gift-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Gifts');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Taxes', 'Income tax, property tax, other taxes', 'cash-stack'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Taxes');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Insurance', 'Life, health, home, auto insurance', 'shield-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Insurance');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Subscriptions', 'Recurring subscriptions, memberships', 'calendar-check-fill'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Subscriptions');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Other', 'Miscellaneous expenses that don''t fit elsewhere', 'three-dots'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Other');
  
  INSERT INTO categories (name, description, icon_url) 
  SELECT 'Income', 'Salary, side hustle, investments, etc.', 'cash'
  WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Income');
END $$;

-- Create a test user with encrypted password (password = "password")
DO $$ 
BEGIN
  INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day)
  SELECT 'test@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Test', 'User', 5000.00, 15
  WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'test@example.com');
  
  -- Create a second user
  INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day)
  SELECT 'jane@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Jane', 'Smith', 6500.00, 1
  WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'jane@example.com');
END $$;

-- Add sample accounts for first user
DO $$ 
BEGIN
  INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
  SELECT 1, 'Checking Account', 'CHECKING', 2500.00, 'acc_123456', 'access_token_checking', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP
  WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE account_id = 'acc_123456');
  
  INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
  SELECT 1, 'Savings Account', 'SAVINGS', 10000.00, 'acc_789012', 'access_token_savings', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP
  WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE account_id = 'acc_789012');
  
  INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
  SELECT 1, 'Credit Card', 'CREDIT', -1500.00, 'acc_345678', 'access_token_cc', 'ins_67890', 'Credit Bank', CURRENT_TIMESTAMP
  WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE account_id = 'acc_345678');
END $$;

-- Add sample accounts for second user
DO $$ 
BEGIN
  INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
  SELECT 2, 'Primary Checking', 'CHECKING', 3500.00, 'acc_jane123', 'access_token_jane_checking', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP
  WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE account_id = 'acc_jane123');
  
  INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
  SELECT 2, 'High-Yield Savings', 'SAVINGS', 15000.00, 'acc_jane456', 'access_token_jane_savings', 'ins_12345', 'Big Bank', CURRENT_TIMESTAMP
  WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE account_id = 'acc_jane456');
END $$;

-- Add sample budgets for first user (using WHERE NOT EXISTS to prevent duplicates)
DO $$ 
BEGIN
  INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
  SELECT 1, 'Monthly Rent', 1200.00, 1, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 90.00
  WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Monthly Rent' AND period = 'MONTHLY');
  
  INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
  SELECT 1, 'Groceries', 500.00, 3, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 80.00
  WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Groceries' AND period = 'MONTHLY');
  
  INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
  SELECT 1, 'Entertainment', 200.00, 4, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 85.00
  WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Entertainment' AND period = 'MONTHLY');
  
  INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
  SELECT 1, 'Eating Out', 300.00, 3, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 75.00
  WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Eating Out' AND period = 'MONTHLY');
END $$;

-- Add sample budgets for second user
DO $$ 
BEGIN
  INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
  SELECT 2, 'Housing', 1500.00, 1, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 90.00
  WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 2 AND name = 'Housing' AND period = 'MONTHLY');
  
  INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
  SELECT 2, 'Food Budget', 600.00, 3, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 80.00
  WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 2 AND name = 'Food Budget' AND period = 'MONTHLY');
  
  INSERT INTO budgets (user_id, name, amount, category_id, period, start_date, end_date, warning_threshold)
  SELECT 2, 'Transportation', 400.00, 2, 'MONTHLY', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 85.00
  WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 2 AND name = 'Transportation' AND period = 'MONTHLY');
END $$;

-- Add sample transactions for first user
DO $$ 
BEGIN
  -- User 1's checking account transactions
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 1, 'tx_12345', 'Whole Foods Market', -85.27, CURRENT_DATE - INTERVAL '5 days', 3, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_12345');
  
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 1, 'tx_23456', 'Amazon.com', -29.99, CURRENT_DATE - INTERVAL '3 days', 11, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_23456');
  
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 1, 'tx_34567', 'Netflix', -13.99, CURRENT_DATE - INTERVAL '7 days', 16, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_34567');
  
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 1, 'tx_45678', 'Shell Gas Station', -45.00, CURRENT_DATE - INTERVAL '2 days', 2, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_45678');
  
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 1, 'tx_56789', 'Rent Payment', -1200.00, CURRENT_DATE - INTERVAL '15 days', 1, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_56789');
  
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 1, 'tx_67890', 'Salary Deposit', 2500.00, CURRENT_DATE - INTERVAL '15 days', 18, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_67890');
  
  -- User 1's savings account transaction
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 2, 'tx_78901', 'Interest Payment', 5.25, CURRENT_DATE - INTERVAL '1 day', 18, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_78901');
  
  -- User 1's credit card transactions
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 3, 'tx_89012', 'Restaurant Payment', -62.47, CURRENT_DATE - INTERVAL '4 days', 3, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_89012');
  
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 3, 'tx_90123', 'Online Shopping', -59.99, CURRENT_DATE - INTERVAL '6 days', 11, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_90123');
END $$;

-- Add sample transactions for second user
DO $$ 
BEGIN
  -- User 2's checking account transactions
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 4, 'tx_jane1', 'Trader Joe''s', -92.45, CURRENT_DATE - INTERVAL '2 days', 3, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_jane1');
  
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 4, 'tx_jane2', 'Monthly Transit Pass', -120.00, CURRENT_DATE - INTERVAL '10 days', 2, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_jane2');
  
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 4, 'tx_jane3', 'Paycheck', 3250.00, CURRENT_DATE - INTERVAL '15 days', 18, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_jane3');
  
  -- User 2's savings account transaction
  INSERT INTO transactions (account_id, transaction_id, description, amount, date, category_id, is_manual_entry)
  SELECT 5, 'tx_jane4', 'Interest Earned', 12.50, CURRENT_DATE - INTERVAL '1 day', 18, FALSE
  WHERE NOT EXISTS (SELECT 1 FROM transactions WHERE transaction_id = 'tx_jane4');
END $$;

-- Add sample bills for first user
DO $$ 
BEGIN
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 1, 'Rent', 1200.00, 1, TRUE, TRUE, 1
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 1 AND name = 'Rent');
  
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 1, 'Electricity', 75.00, 15, FALSE, TRUE, 12
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 1 AND name = 'Electricity');
  
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 1, 'Internet', 60.00, 20, FALSE, TRUE, 12
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 1 AND name = 'Internet');
  
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 1, 'Cell Phone', 85.00, 5, TRUE, TRUE, 12
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 1 AND name = 'Cell Phone');
  
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 1, 'Netflix', 13.99, 7, FALSE, TRUE, 16
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 1 AND name = 'Netflix');
  
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 1, 'Gym Membership', 45.00, 10, FALSE, TRUE, 16
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 1 AND name = 'Gym Membership');
END $$;

-- Add sample bills for second user
DO $$ 
BEGIN
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 2, 'Mortgage', 1500.00, 5, TRUE, TRUE, 1
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 2 AND name = 'Mortgage');
  
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 2, 'Car Payment', 350.00, 12, FALSE, TRUE, 2
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 2 AND name = 'Car Payment');
  
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 2, 'Water & Sewage', 45.00, 18, FALSE, TRUE, 12
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 2 AND name = 'Water & Sewage');
  
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 2, 'Internet & Cable', 120.00, 22, FALSE, TRUE, 12
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 2 AND name = 'Internet & Cable');
  
  INSERT INTO bills (user_id, name, amount, due_day, is_paid, is_recurring, category_id)
  SELECT 2, 'Streaming Services', 35.99, 25, FALSE, TRUE, 16
  WHERE NOT EXISTS (SELECT 1 FROM bills WHERE user_id = 2 AND name = 'Streaming Services');
END $$;

-- Add sample notifications
DO $$ 
BEGIN
  INSERT INTO notifications (user_id, budget_id, message, sent_at, is_read)
  SELECT 1, 3, 'You have reached 85% of your Entertainment budget for this month', CURRENT_TIMESTAMP - INTERVAL '2 days', TRUE
  WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = 1 AND budget_id = 3 AND message = 'You have reached 85% of your Entertainment budget for this month');
  
  INSERT INTO notifications (user_id, budget_id, message, sent_at, is_read)
  SELECT 1, 2, 'You have reached 90% of your Groceries budget for this month', CURRENT_TIMESTAMP - INTERVAL '1 day', FALSE
  WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = 1 AND budget_id = 2 AND message = 'You have reached 90% of your Groceries budget for this month');
  
  INSERT INTO notifications (user_id, budget_id, message, sent_at, is_read)
  SELECT 2, 6, 'You have reached 75% of your Food Budget for this month', CURRENT_TIMESTAMP - INTERVAL '3 days', FALSE
  WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = 2 AND budget_id = 6 AND message = 'You have reached 75% of your Food Budget for this month');
END $$;
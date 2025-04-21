-- Add is_demo column to users table
ALTER TABLE users ADD COLUMN is_demo BOOLEAN NOT NULL DEFAULT FALSE;

-- Create a demo user if it doesn't exist already
INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day, is_demo)
SELECT 'demo@finmanapp.com', '$2a$10$dVzAjmXgZrpx7.GfHs7uAuwb2kNv3IrpF3KFrn4tSynpbQH0.mcBK', 'Demo', 'User', 5000.00, 15, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'demo@finmanapp.com'
);

-- Create demo accounts for the demo user
-- First, get the demo user ID
DO $$
DECLARE
    demo_user_id BIGINT;
BEGIN
    -- Get demo user ID
    SELECT id INTO demo_user_id FROM users WHERE email = 'demo@finmanapp.com';
    
    -- Only proceed if we have a demo user
    IF demo_user_id IS NOT NULL THEN
        -- Create demo accounts if they don't exist
        IF NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = demo_user_id AND name = 'Demo Checking') THEN
            INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
            VALUES (demo_user_id, 'Demo Checking', 'CHECKING', 3200.00, 'acc_demo_checking', 'access_token_demo', 'ins_demo', 'Demo Bank', NOW());
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = demo_user_id AND name = 'Demo Savings') THEN
            INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
            VALUES (demo_user_id, 'Demo Savings', 'SAVINGS', 12500.00, 'acc_demo_savings', 'access_token_demo', 'ins_demo', 'Demo Bank', NOW());
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = demo_user_id AND name = 'Demo Credit Card') THEN
            INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
            VALUES (demo_user_id, 'Demo Credit Card', 'CREDIT', -750.00, 'acc_demo_credit', 'access_token_demo', 'ins_demo', 'Demo Credit Union', NOW());
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = demo_user_id AND name = 'Demo Investment') THEN
            INSERT INTO accounts (user_id, name, type, balance, account_id, access_token, institution_id, institution_name, last_synced)
            VALUES (demo_user_id, 'Demo Investment', 'INVESTMENT', 25000.00, 'acc_demo_invest', 'access_token_demo', 'ins_demo', 'Demo Investments', NOW());
        END IF;
    END IF;
END $$;
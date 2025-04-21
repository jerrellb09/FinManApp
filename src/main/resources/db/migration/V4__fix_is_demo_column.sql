-- First add the column as nullable if it doesn't exist
DO $$
BEGIN
    BEGIN
        IF NOT EXISTS (
            SELECT FROM information_schema.columns 
            WHERE table_name = 'users' AND column_name = 'is_demo'
        ) THEN
            ALTER TABLE users ADD COLUMN is_demo BOOLEAN;
            RAISE NOTICE 'Added is_demo column as nullable';
        ELSE
            RAISE NOTICE 'is_demo column already exists';
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Error adding is_demo column: %', SQLERRM;
    END;
END $$;

-- Update all existing users to have is_demo = false
DO $$
BEGIN
    BEGIN
        UPDATE users SET is_demo = false WHERE is_demo IS NULL;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Error updating users is_demo: %', SQLERRM;
    END;
END $$;

-- Make the is_demo column not null with a default value
DO $$
BEGIN
    BEGIN
        ALTER TABLE users ALTER COLUMN is_demo SET NOT NULL;
        ALTER TABLE users ALTER COLUMN is_demo SET DEFAULT false;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Error setting is_demo constraints: %', SQLERRM;
    END;
END $$;

-- Create or update the demo user account
DO $$
BEGIN
    BEGIN
        INSERT INTO users (email, password, first_name, last_name, monthly_income, payday_day, is_demo)
        VALUES (
            'demo@finmanapp.com', 
            '$2a$10$dVzAjmXgZrpx7.GfHs7uAuwb2kNv3IrpF3KFrn4tSynpbQH0.mcBK', 
            'Demo', 
            'User', 
            5000.00, 
            15, 
            true
        )
        ON CONFLICT (email) DO UPDATE
        SET is_demo = true, monthly_income = 5000.00, payday_day = 15;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Error inserting/updating demo user: %', SQLERRM;
        
        -- Try alternate approach if needed
        BEGIN
            -- If demo user doesn't exist at all, create without relying on the is_demo column
            IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'demo@finmanapp.com') THEN
                INSERT INTO users (email, password, first_name, last_name)
                VALUES (
                    'demo@finmanapp.com',
                    '$2a$10$dVzAjmXgZrpx7.GfHs7uAuwb2kNv3IrpF3KFrn4tSynpbQH0.mcBK',
                    'Demo',
                    'User'
                );
            END IF;
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Error in alternate demo user creation: %', SQLERRM;
        END;
    END;
END $$;
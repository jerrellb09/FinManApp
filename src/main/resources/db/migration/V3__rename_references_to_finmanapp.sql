-- Migration script to handle database name changes from tradingbotv2 to finmanapp
-- This migration is designed to work with existing databases that might have
-- tradingbotv2 references in view definitions, stored procedures, or other objects

-- Placeholder for any renaming operations that might be needed in the future

-- Note: Most of the renaming was done in code and configuration files
-- This script exists to maintain migration version history
-- and allow Flyway validation to succeed with existing databases

-- If needed, we would add commands here to rename database objects
-- For example:
-- ALTER TABLE if_exists tradingbotv2_legacy_table RENAME TO finmanapp_table;
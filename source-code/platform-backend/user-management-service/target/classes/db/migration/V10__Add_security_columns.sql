-- V10__Add_security_columns.sql
-- Add password hash and security columns to users table

ALTER TABLE users
ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100) COMMENT 'BCrypt encrypted password',
ADD COLUMN IF NOT EXISTS account_locked BOOLEAN DEFAULT FALSE COMMENT 'Account lock status',
ADD COLUMN IF NOT EXISTS failed_login_attempts INT DEFAULT 0 COMMENT 'Failed login counter',
ADD COLUMN IF NOT EXISTS first_login BOOLEAN DEFAULT TRUE COMMENT 'First time login flag';

-- Add index for performance
CREATE INDEX IF NOT EXISTS idx_password_hash ON users(password_hash);
CREATE INDEX IF NOT EXISTS idx_account_locked ON users(account_locked);
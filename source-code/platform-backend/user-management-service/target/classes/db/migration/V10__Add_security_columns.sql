-- V10__Add_security_columns.sql
-- Add password hash and security columns to users table
-- MySQL-compatible version without IF NOT EXISTS

-- Check if columns exist before adding them
SET @dbname = DATABASE();
SET @tablename = 'users';

-- Add password_hash if it doesn't exist
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = 'password_hash'
);

SET @query = IF(@col_exists = 0,
    'ALTER TABLE users ADD COLUMN password_hash VARCHAR(100) COMMENT ''BCrypt encrypted password''',
    'SELECT ''password_hash already exists'' AS info'
);

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add account_locked if it doesn't exist
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = 'account_locked'
);

SET @query = IF(@col_exists = 0,
    'ALTER TABLE users ADD COLUMN account_locked BOOLEAN DEFAULT FALSE COMMENT ''Account lock status''',
    'SELECT ''account_locked already exists'' AS info'
);

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add failed_login_attempts if it doesn't exist
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = 'failed_login_attempts'
);

SET @query = IF(@col_exists = 0,
    'ALTER TABLE users ADD COLUMN failed_login_attempts INT DEFAULT 0 COMMENT ''Failed login counter''',
    'SELECT ''failed_login_attempts already exists'' AS info'
);

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add first_login if it doesn't exist
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = @tablename
    AND COLUMN_NAME = 'first_login'
);


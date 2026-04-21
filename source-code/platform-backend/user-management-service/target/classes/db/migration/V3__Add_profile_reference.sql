-- First, ensure we're using the correct database
USE agribind_users;

-- Remove any failed migration record for version 3
DELETE FROM flyway_schema_history WHERE version = '3';

-- Check if profile_id column exists
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'agribind_users'
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'profile_id'
);

-- Only add column if it doesn't exist
SET @add_col = IF(@col_exists = 0,
    'ALTER TABLE users ADD COLUMN profile_id BIGINT NULL',
    'SELECT "Column already exists" AS info'
);

PREPARE stmt_add_col FROM @add_col;
EXECUTE stmt_add_col;
DEALLOCATE PREPARE stmt_add_col;

-- Check if foreign key exists
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = 'agribind_users'
    AND TABLE_NAME = 'users'
    AND CONSTRAINT_NAME = 'fk_users_profile'
);

-- Only add foreign key if it doesn't exist
SET @add_fk = IF(@fk_exists = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_users_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE SET NULL',
    'SELECT "Foreign key already exists" AS info'
);

PREPARE stmt_add_fk FROM @add_fk;
EXECUTE stmt_add_fk;
DEALLOCATE PREPARE stmt_add_fk;

-- Check if index exists
SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'agribind_users'
    AND TABLE_NAME = 'users'
    AND INDEX_NAME = 'idx_profile_id'
);

-- Only create index if it doesn't exist
SET @add_idx = IF(@idx_exists = 0,
    'CREATE INDEX idx_profile_id ON users(profile_id)',
    'SELECT "Index already exists" AS info'
);

PREPARE stmt_add_idx FROM @add_idx;
EXECUTE stmt_add_idx;
DEALLOCATE PREPARE stmt_add_idx;
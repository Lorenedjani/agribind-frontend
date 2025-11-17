USE agribind_users;

-- Remove failed migration
DELETE FROM flyway_schema_history WHERE version = '3';

-- Check if foreign key exists and drop it if necessary
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = 'agribind_users'
    AND TABLE_NAME = 'users'
    AND CONSTRAINT_NAME = 'fk_users_profile'
);

SET @drop_fk = IF(@fk_exists > 0,
    'ALTER TABLE users DROP FOREIGN KEY fk_users_profile',
    'SELECT "No FK to drop" AS info'
);

PREPARE stmt_drop FROM @drop_fk;
EXECUTE stmt_drop;
DEALLOCATE PREPARE stmt_drop;

-- Check if index exists and drop it if necessary
SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'agribind_users'
    AND TABLE_NAME = 'users'
    AND INDEX_NAME = 'idx_profile_id'
);

SET @drop_idx = IF(@idx_exists > 0,
    'DROP INDEX idx_profile_id ON users',
    'SELECT "No index to drop" AS info'
);

PREPARE stmt_drop_idx FROM @drop_idx;
EXECUTE stmt_drop_idx;
DEALLOCATE PREPARE stmt_drop_idx;
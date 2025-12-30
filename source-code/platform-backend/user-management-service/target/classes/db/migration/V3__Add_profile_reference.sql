-- Remove failed migration (if flyway_schema_history table exists)
DELETE FROM flyway_schema_history WHERE version = '3';

-- Check if foreign key exists and drop it if necessary
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_schema = current_schema()
        AND table_name = 'users'
        AND constraint_name = 'fk_users_profile'
    ) THEN
ALTER TABLE users DROP CONSTRAINT fk_users_profile;
END IF;
END $$;

-- Check if index exists and drop it if necessary
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = current_schema()
        AND tablename = 'users'
        AND indexname = 'idx_profile_id'
    ) THEN
DROP INDEX idx_profile_id;
END IF;
END $$;
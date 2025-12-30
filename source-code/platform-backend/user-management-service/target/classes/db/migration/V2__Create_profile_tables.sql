-- V2__Create_profile_tables.sql
-- Create profiles table with idempotent checks

-- Step 1: Create gender_enum type only if it doesn't exist
CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE', 'OTHER');


-- Step 2: Create profiles table if it doesn't exist
CREATE TABLE IF NOT EXISTS profiles (
                                        id BIGSERIAL PRIMARY KEY,
                                        bio VARCHAR(500),
                                        profile_picture_path VARCHAR(500),
                                        preferred_language VARCHAR(10) DEFAULT 'fr',
                                        receive_sms_notifications BOOLEAN DEFAULT TRUE,
                                        receive_email_notifications BOOLEAN DEFAULT FALSE,
                                        receive_push_notifications BOOLEAN DEFAULT TRUE,
                                        skills VARCHAR(1000),
                                        date_of_birth VARCHAR(20),
                                        gender gender_enum,
                                        marital_status VARCHAR(50),
                                        dependents_count INT DEFAULT 0,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(100),
                                        modified_by VARCHAR(100),
                                        version BIGINT DEFAULT 0
);

-- Step 3: Create index ONLY if it doesn't exist - use IF NOT EXISTS
CREATE INDEX IF NOT EXISTS idx_created_at ON profiles(created_at);

-- Step 4: Create or replace the trigger function
CREATE OR REPLACE FUNCTION update_profiles_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Step 5: Create the trigger (drop if exists first)
DROP TRIGGER IF EXISTS update_profiles_updated_at ON profiles;
CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON profiles
    FOR EACH ROW
EXECUTE FUNCTION update_profiles_updated_at();
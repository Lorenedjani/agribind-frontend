-- File: V2__Fix_refresh_token_constraints.sql
-- Place this in: auth-service/src/main/resources/db/migration/

-- Step 1: Remove the problematic unique constraint on token
-- (The token column is too long for the unique key)
ALTER TABLE refresh_tokens
DROP INDEX UK_ghpmfn23vmxfu3spu3lfg4r2d;

-- Step 2: Create a composite unique constraint on userId + deviceId
-- This prevents duplicate tokens for the same user/device combination
ALTER TABLE refresh_tokens
ADD CONSTRAINT uk_user_device UNIQUE (user_id, device_id);

-- Step 3: Add an index on the token column for faster lookups
-- (but not unique, since we'll handle uniqueness via userId + deviceId)
CREATE INDEX idx_refresh_token ON refresh_tokens(token(255));

-- Step 4: Add index on userId for faster queries
CREATE INDEX idx_user_id ON refresh_tokens(user_id);

-- Step 5: Add index on expiresAt for cleanup queries
CREATE INDEX idx_expires_at ON refresh_tokens(expires_at);

-- Optional: Clean up any existing duplicate data
DELETE t1 FROM refresh_tokens t1
INNER JOIN refresh_tokens t2
WHERE
    t1.id > t2.id
    AND t1.user_id = t2.user_id
    AND t1.device_id = t2.device_id;
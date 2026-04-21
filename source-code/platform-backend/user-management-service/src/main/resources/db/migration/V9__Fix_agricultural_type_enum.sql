-- V9__Fix_agricultural_type_enum.sql
-- Fix the agricultural_type enum to match Java enum values

USE agribind_users;  -- FIXED: Changed from 'railway' to 'agribind_users'

-- Step 1: Add a temporary column
ALTER TABLE farmers ADD COLUMN agricultural_type_temp VARCHAR(50);

-- Step 2: Copy data with transformation (CROPS -> CROP)
UPDATE farmers
SET agricultural_type_temp = CASE
                                 WHEN agricultural_type = 'CROPS' THEN 'CROP'
                                 WHEN agricultural_type = 'LIVESTOCK' THEN 'LIVESTOCK'
                                 WHEN agricultural_type = 'MIXED' THEN 'MIXED'
                                 WHEN agricultural_type = 'AQUACULTURE' THEN 'AQUACULTURE'
                                 WHEN agricultural_type = 'FORESTRY' THEN 'FORESTRY'
                                 ELSE agricultural_type
    END;

-- Step 3: Drop the old column
ALTER TABLE farmers DROP COLUMN agricultural_type;

-- Step 4: Recreate the column with correct enum values (matching Java enum)
ALTER TABLE farmers ADD COLUMN agricultural_type
    ENUM('CROP', 'LIVESTOCK', 'MIXED') NOT NULL DEFAULT 'CROP'
AFTER user_id;

-- Step 5: Copy data back from temp column
UPDATE farmers
SET agricultural_type = agricultural_type_temp;

-- Step 6: Drop the temporary column
ALTER TABLE farmers DROP COLUMN agricultural_type_temp;

-- Verify the change
SELECT COLUMN_NAME, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'agribind_users'  -- FIXED: Changed from 'railway'
  AND TABLE_NAME = 'farmers'
  AND COLUMN_NAME = 'agricultural_type';
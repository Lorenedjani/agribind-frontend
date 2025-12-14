-- V9__Fix_agricultural_type_enum.sql
-- Fix the agricultural_type enum to match Java enum values

-- First, update the agricultural_type_enum type
ALTER TYPE agricultural_type_enum RENAME TO agricultural_type_enum_old;

-- Create new enum type with correct values
CREATE TYPE agricultural_type_enum AS ENUM ('CROP', 'LIVESTOCK', 'MIXED');

-- Step 1: Add a temporary column
ALTER TABLE farmers ADD COLUMN agricultural_type_temp VARCHAR(50);

-- Step 2: Copy data with transformation (CROPS -> CROP, drop AQUACULTURE and FORESTRY)
UPDATE farmers
SET agricultural_type_temp = CASE
                                 WHEN agricultural_type::text = 'CROPS' THEN 'CROP'
                                 WHEN agricultural_type::text = 'LIVESTOCK' THEN 'LIVESTOCK'
                                 WHEN agricultural_type::text = 'MIXED' THEN 'MIXED'
                                 WHEN agricultural_type::text = 'AQUACULTURE' THEN 'MIXED'  -- Map to MIXED
                                 WHEN agricultural_type::text = 'FORESTRY' THEN 'CROP'      -- Map to CROP
                                 ELSE agricultural_type::text
    END;

-- Step 3: Drop the old column
ALTER TABLE farmers DROP COLUMN agricultural_type;

-- Step 4: Add the column with new enum type
ALTER TABLE farmers ADD COLUMN agricultural_type agricultural_type_enum NOT NULL DEFAULT 'CROP';

-- Step 5: Copy data back from temp column
UPDATE farmers
SET agricultural_type = agricultural_type_temp::agricultural_type_enum;

-- Step 6: Drop the temporary column
ALTER TABLE farmers DROP COLUMN agricultural_type_temp;

-- Step 7: Drop the old enum type
DROP TYPE agricultural_type_enum_old;

-- Verify the change
SELECT column_name, udt_name, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'farmers'
  AND column_name = 'agricultural_type';
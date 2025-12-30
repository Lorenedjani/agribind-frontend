-- Create ENUM type for agricultural type
CREATE TYPE agricultural_type_enum AS ENUM ('CROPS', 'LIVESTOCK', 'MIXED', 'AQUACULTURE', 'FORESTRY');

CREATE TABLE farmers (
                         user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                         agricultural_type agricultural_type_enum NOT NULL,
                         cooperative_id BIGINT REFERENCES cooperatives(user_id) ON DELETE SET NULL,
                         years_farming INT,
                         education_level VARCHAR(100),
                         has_bank_account BOOLEAN DEFAULT FALSE,
                         has_mobile_money BOOLEAN DEFAULT TRUE,

    -- FarmDetails (Embedded)
                         total_land_area DOUBLE PRECISION,
                         cultivated_area DOUBLE PRECISION,
                         pasture_area DOUBLE PRECISION,
                         soil_type VARCHAR(100),
                         irrigation_type VARCHAR(50),
                         owns_land BOOLEAN DEFAULT TRUE,
                         land_ownership_type VARCHAR(50),
                         number_of_plots INT,
                         average_yield DOUBLE PRECISION,
                         farming_methods VARCHAR(100),
                         uses_fertilizers BOOLEAN DEFAULT FALSE,
                         uses_pesticides BOOLEAN DEFAULT FALSE,
                         uses_organic_methods BOOLEAN DEFAULT TRUE,
                         equipment_owned TEXT
);

-- Create indexes
CREATE INDEX idx_agricultural_type ON farmers(agricultural_type);
CREATE INDEX idx_cooperative_id ON farmers(cooperative_id);
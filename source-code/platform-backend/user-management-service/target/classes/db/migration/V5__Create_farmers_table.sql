CREATE TABLE farmers (
    user_id BIGINT PRIMARY KEY,
    agricultural_type ENUM('CROPS', 'LIVESTOCK', 'MIXED', 'AQUACULTURE', 'FORESTRY') NOT NULL,
    cooperative_id BIGINT,
    years_farming INT,
    education_level VARCHAR(100),
    has_bank_account BOOLEAN DEFAULT FALSE,
    has_mobile_money BOOLEAN DEFAULT TRUE,

    -- FarmDetails (Embedded)
    total_land_area DOUBLE,
    cultivated_area DOUBLE,
    pasture_area DOUBLE,
    soil_type VARCHAR(100),
    irrigation_type VARCHAR(50),
    owns_land BOOLEAN DEFAULT TRUE,
    land_ownership_type VARCHAR(50),
    number_of_plots INT,
    average_yield DOUBLE,
    farming_methods VARCHAR(100),
    uses_fertilizers BOOLEAN DEFAULT FALSE,
    uses_pesticides BOOLEAN DEFAULT FALSE,
    uses_organic_methods BOOLEAN DEFAULT TRUE,
    equipment_owned TEXT,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (cooperative_id) REFERENCES cooperatives(user_id) ON DELETE SET NULL,
    INDEX idx_agricultural_type (agricultural_type),
    INDEX idx_cooperative_id (cooperative_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
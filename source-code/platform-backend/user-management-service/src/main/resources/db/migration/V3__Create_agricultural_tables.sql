-- Farmer specific table
CREATE TABLE farmers (
    user_id BIGINT PRIMARY KEY,
    agricultural_type ENUM('CROP', 'LIVESTOCK', 'MIXED') NOT NULL,
    cooperative_id BIGINT,
    years_farming INT,
    education_level VARCHAR(100),
    has_bank_account BOOLEAN DEFAULT FALSE,
    has_mobile_money BOOLEAN DEFAULT TRUE,

    -- Farm details (embedded)
    total_land_area DECIMAL(10,2),
    cultivated_area DECIMAL(10,2),
    pasture_area DECIMAL(10,2),
    soil_type VARCHAR(100),
    irrigation_type VARCHAR(100),
    owns_land BOOLEAN DEFAULT TRUE,
    land_ownership_type VARCHAR(50),
    number_of_plots INT,
    average_yield DECIMAL(10,2),
    farming_methods VARCHAR(200),
    uses_fertilizers BOOLEAN DEFAULT FALSE,
    uses_pesticides BOOLEAN DEFAULT FALSE,
    uses_organic_methods BOOLEAN DEFAULT TRUE,
    equipment_owned TEXT,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_agricultural_type (agricultural_type),
    INDEX idx_cooperative (cooperative_id),
    INDEX idx_land_area (total_land_area)
);

-- Crop types for farmers
CREATE TABLE farmer_crops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    crop_type ENUM(
        'MAIZE', 'RICE', 'MILLET', 'SORGHUM', 'BEANS', 'POTATOES', 'CASSAVA', 'YAMS', 'PLANTAINS',
        'COCOA', 'COFFEE', 'COTTON', 'BANANAS', 'PALM_OIL', 'RUBBER', 'TEA', 'SUGARCANE',
        'TOMATOES', 'ONIONS', 'CARROTS', 'CABBAGE', 'PEPPERS', 'OKRA', 'EGGPLANT',
        'MANGOES', 'ORANGES', 'PINEAPPLES', 'AVOCADOS', 'PAPAYAS',
        'GROUNDNUTS', 'SOYBEANS', 'SESAME'
    ) NOT NULL,

    FOREIGN KEY (farmer_id) REFERENCES farmers(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_farmer_crop (farmer_id, crop_type),
    INDEX idx_crop_type (crop_type)
);

-- Livestock types for farmers
CREATE TABLE farmer_livestock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    livestock_type ENUM(
        'CATTLE', 'GOATS', 'SHEEP', 'PIGS', 'POULTRY', 'RABBITS', 'BEES', 'FISH',
        'GUINEA_FOWLS', 'DUCKS', 'TURKEYS'
    ) NOT NULL,

    FOREIGN KEY (farmer_id) REFERENCES farmers(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_farmer_livestock (farmer_id, livestock_type),
    INDEX idx_livestock_type (livestock_type)
);
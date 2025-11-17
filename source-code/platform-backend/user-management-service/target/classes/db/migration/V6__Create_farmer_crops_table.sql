CREATE TABLE farmer_crops (
    farmer_id BIGINT NOT NULL,
    crop_type ENUM('COCOA', 'COFFEE', 'COTTON', 'PALM_OIL', 'RUBBER', 'BANANA', 'PLANTAIN',
                   'CASSAVA', 'MAIZE', 'RICE', 'BEANS', 'GROUNDNUT', 'SORGHUM', 'MILLET',
                   'YAM', 'VEGETABLES', 'FRUITS', 'SPICES', 'OTHER') NOT NULL,
    PRIMARY KEY (farmer_id, crop_type),
    FOREIGN KEY (farmer_id) REFERENCES farmers(user_id) ON DELETE CASCADE,
    INDEX idx_crop_type (crop_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
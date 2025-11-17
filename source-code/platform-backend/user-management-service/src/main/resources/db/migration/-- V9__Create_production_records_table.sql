CREATE TABLE production_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    production_id VARCHAR(50) UNIQUE NOT NULL,
    delivery_date DATE NOT NULL,
    farmer_id BIGINT NOT NULL,
    crop_type ENUM('COCOA', 'COFFEE', 'COTTON', 'PALM_OIL', 'RUBBER', 'BANANA', 'PLANTAIN',
                   'CASSAVA', 'MAIZE', 'RICE', 'BEANS', 'GROUNDNUT', 'SORGHUM', 'MILLET',
                   'YAM', 'VEGETABLES', 'FRUITS', 'SPICES', 'OTHER') NOT NULL,
    quantity DOUBLE NOT NULL,
    quality_grade ENUM('GRADE_A', 'GRADE_B', 'GRADE_C') NOT NULL,
    warehouse VARCHAR(255) NOT NULL,
    value_xaf DOUBLE NOT NULL,
    status ENUM('PENDING', 'VERIFIED', 'REJECTED', 'PROCESSED', 'SOLD') DEFAULT 'PENDING',
    notes TEXT,
    verified_by VARCHAR(100),
    verification_date DATE,
    rejection_reason TEXT,
    price_per_mt DOUBLE,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    version BIGINT DEFAULT 0,

    FOREIGN KEY (farmer_id) REFERENCES farmers(user_id) ON DELETE CASCADE,

    INDEX idx_production_id (production_id),
    INDEX idx_farmer_id (farmer_id),
    INDEX idx_delivery_date (delivery_date),
    INDEX idx_crop_type (crop_type),
    INDEX idx_quality_grade (quality_grade),
    INDEX idx_status (status),
    INDEX idx_warehouse (warehouse),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample production data for testing
INSERT INTO production_records (production_id, delivery_date, farmer_id, crop_type, quantity, quality_grade, warehouse, value_xaf, status, price_per_mt)
SELECT
    'PROD001',
    '2024-10-04',
    (SELECT user_id FROM farmers LIMIT 1),
    'COCOA',
    2.5,
    'GRADE_A',
    'Douala Warehouse',
    5250000,
    'VERIFIED',
    2100000
WHERE EXISTS (SELECT 1 FROM farmers LIMIT 1);

INSERT INTO production_records (production_id, delivery_date, farmer_id, crop_type, quantity, quality_grade, warehouse, value_xaf, status, price_per_mt)
SELECT
    'PROD002',
    '2024-10-04',
    (SELECT user_id FROM farmers LIMIT 1 OFFSET 1),
    'COFFEE',
    1.8,
    'GRADE_A',
    'Yaoundé Warehouse',
    3960000,
    'VERIFIED',
    2200000
WHERE EXISTS (SELECT 1 FROM farmers LIMIT 1 OFFSET 1);
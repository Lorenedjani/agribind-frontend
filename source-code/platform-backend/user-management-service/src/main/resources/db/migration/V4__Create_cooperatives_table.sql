CREATE TABLE cooperatives (
    user_id BIGINT PRIMARY KEY,
    cooperative_type ENUM('PRODUCTION', 'MARKETING', 'CREDIT', 'CONSUMER', 'MULTIPURPOSE') NOT NULL,
    operating_region ENUM('ADAMAOUA', 'CENTRE', 'EST', 'EXTREME_NORD', 'LITTORAL', 'NORD', 'NORD_OUEST', 'OUEST', 'SUD', 'SUD_OUEST') NOT NULL,
    legal_registration_number VARCHAR(100),
    establishment_year INT,
    contact_person VARCHAR(255),
    contact_person_phone VARCHAR(25),
    total_land_area DOUBLE,
    active_member_count INT,

    -- CooperativeDetails (Embedded)
    total_members INT,
    female_members INT,
    male_members INT,
    annual_production DOUBLE,
    annual_revenue DOUBLE,
    primary_products TEXT,
    certification VARCHAR(255),
    has_storage_facilities BOOLEAN DEFAULT FALSE,
    has_processing_equipment BOOLEAN DEFAULT FALSE,
    has_transport_vehicles BOOLEAN DEFAULT FALSE,
    bank_name VARCHAR(255),
    bank_account_number VARCHAR(100),
    mobile_money_number VARCHAR(25),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_cooperative_type (cooperative_type),
    INDEX idx_operating_region (operating_region),
    INDEX idx_legal_registration (legal_registration_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

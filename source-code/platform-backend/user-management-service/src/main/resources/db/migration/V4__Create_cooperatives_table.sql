-- Create ENUM types
CREATE TYPE cooperative_type_enum AS ENUM ('PRODUCTION', 'MARKETING', 'CREDIT', 'CONSUMER', 'MULTIPURPOSE');

CREATE TABLE cooperatives (
                              user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                              cooperative_type cooperative_type_enum NOT NULL,
                              operating_region region_type NOT NULL,
                              legal_registration_number VARCHAR(100),
                              establishment_year INT,
                              contact_person VARCHAR(255),
                              contact_person_phone VARCHAR(25),
                              total_land_area DOUBLE PRECISION,
                              active_member_count INT,

    -- CooperativeDetails (Embedded)
                              total_members INT,
                              female_members INT,
                              male_members INT,
                              annual_production DOUBLE PRECISION,
                              annual_revenue DOUBLE PRECISION,
                              primary_products TEXT,
                              certification VARCHAR(255),
                              has_storage_facilities BOOLEAN DEFAULT FALSE,
                              has_processing_equipment BOOLEAN DEFAULT FALSE,
                              has_transport_vehicles BOOLEAN DEFAULT FALSE,
                              bank_name VARCHAR(255),
                              bank_account_number VARCHAR(100),
                              mobile_money_number VARCHAR(25)
);

-- Create indexes
CREATE INDEX idx_cooperative_type ON cooperatives(cooperative_type);
CREATE INDEX idx_operating_region ON cooperatives(operating_region);
CREATE INDEX idx_legal_registration ON cooperatives(legal_registration_number);
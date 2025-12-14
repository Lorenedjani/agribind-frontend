-- Create ENUM type for crop types
CREATE TYPE crop_type_enum AS ENUM ('COCOA', 'COFFEE', 'COTTON', 'PALM_OIL', 'RUBBER', 'BANANA', 'PLANTAIN',
                   'CASSAVA', 'MAIZE', 'RICE', 'BEANS', 'GROUNDNUT', 'SORGHUM', 'MILLET',
                   'YAM', 'VEGETABLES', 'FRUITS', 'SPICES', 'OTHER');

CREATE TABLE farmer_crops (
                              farmer_id BIGINT NOT NULL REFERENCES farmers(user_id) ON DELETE CASCADE,
                              crop_type crop_type_enum NOT NULL,
                              PRIMARY KEY (farmer_id, crop_type)
);

CREATE INDEX idx_crop_type ON farmer_crops(crop_type);
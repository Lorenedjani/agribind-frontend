-- Create ENUM type for livestock types
CREATE TYPE livestock_type_enum AS ENUM ('CATTLE', 'GOATS', 'SHEEP', 'PIGS', 'POULTRY', 'RABBITS',
                        'FISH', 'BEES', 'OTHER');

CREATE TABLE farmer_livestock (
                                  farmer_id BIGINT NOT NULL REFERENCES farmers(user_id) ON DELETE CASCADE,
                                  livestock_type livestock_type_enum NOT NULL,
                                  PRIMARY KEY (farmer_id, livestock_type)
);

CREATE INDEX idx_livestock_type ON farmer_livestock(livestock_type);
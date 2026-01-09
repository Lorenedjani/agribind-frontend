CREATE TABLE farmer_livestock (
    farmer_id BIGINT NOT NULL,
    livestock_type ENUM('CATTLE', 'GOATS', 'SHEEP', 'PIGS', 'POULTRY', 'RABBITS',
                        'FISH', 'BEES', 'OTHER') NOT NULL,
    PRIMARY KEY (farmer_id, livestock_type),
    FOREIGN KEY (farmer_id) REFERENCES farmers(user_id) ON DELETE CASCADE,
    INDEX idx_livestock_type (livestock_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

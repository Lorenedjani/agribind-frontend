-- Create custom ENUM types first
CREATE TYPE user_type AS ENUM ('FARMER', 'COOPERATIVE', 'GOVERNMENT');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED', 'DELETED');
CREATE TYPE region_type AS ENUM ('ADAMAOUA', 'CENTRE', 'EST', 'EXTREME_NORD', 'LITTORAL', 'NORD', 'NORD_OUEST', 'OUEST', 'SUD', 'SUD_OUEST');

-- Create users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       user_id VARCHAR(20) UNIQUE NOT NULL,
                       type user_type NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE,
                       phone_number VARCHAR(25) UNIQUE NOT NULL,
                       status user_status DEFAULT 'ACTIVE',
                       registration_number VARCHAR(50) UNIQUE,
                       qr_code_data TEXT,
                       region region_type,
                       department VARCHAR(100),
                       district VARCHAR(100),
                       village VARCHAR(100),
                       gps_coordinates VARCHAR(100),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(100),
                       modified_by VARCHAR(100),
                       version BIGINT DEFAULT 0,
                       notes TEXT
);

-- Create indexes
CREATE INDEX idx_user_id ON users(user_id);
CREATE INDEX idx_phone ON users(phone_number);
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_registration ON users(registration_number);
CREATE INDEX idx_type ON users(type);
CREATE INDEX idx_status ON users(status);
CREATE INDEX idx_region ON users(region);
CREATE INDEX idx_created_at ON users(created_at);

-- Create trigger for auto-updating updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
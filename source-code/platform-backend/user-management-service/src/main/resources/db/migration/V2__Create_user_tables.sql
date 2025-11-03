-- Create profiles table
CREATE TABLE profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bio TEXT,
    profile_picture_path VARCHAR(500),
    preferred_language VARCHAR(10) DEFAULT 'fr',
    receive_sms_notifications BOOLEAN DEFAULT TRUE,
    receive_email_notifications BOOLEAN DEFAULT FALSE,
    receive_push_notifications BOOLEAN DEFAULT TRUE,
    skills TEXT,
    date_of_birth VARCHAR(20),
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    marital_status VARCHAR(50),
    dependents_count INT DEFAULT 0,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- Add profile reference to users
ALTER TABLE users ADD COLUMN profile_id BIGINT;
ALTER TABLE users ADD FOREIGN KEY (profile_id) REFERENCES profiles(id);
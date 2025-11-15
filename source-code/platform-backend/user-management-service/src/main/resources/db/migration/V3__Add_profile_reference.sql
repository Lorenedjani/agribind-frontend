ALTER TABLE users ADD COLUMN profile_id BIGINT NULL;
ALTER TABLE users ADD FOREIGN KEY (profile_id) REFERENCES profiles(id);
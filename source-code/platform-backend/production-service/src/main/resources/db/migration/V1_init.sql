-- announcement-service DB schema
CREATE TABLE announcement (
                              id CHAR(36) PRIMARY KEY,
                              cooperative_id CHAR(36) NOT NULL,
                              title VARCHAR(255),
                              content TEXT,
                              language_code VARCHAR(10),
                              audio_url VARCHAR(1024),
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE coop_need (
                           id CHAR(36) PRIMARY KEY,
                           cooperative_id CHAR(36) NOT NULL,
                           item_name VARCHAR(255),
                           quantity DOUBLE,
                           unit VARCHAR(20),
                           reason TEXT,
                           due_date DATE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

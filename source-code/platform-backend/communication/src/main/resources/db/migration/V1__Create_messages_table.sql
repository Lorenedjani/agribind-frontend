CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    content TEXT,
    audio_file_url VARCHAR(500),
    target_audience VARCHAR(100),
    specific_zone VARCHAR(100),
    scheduled_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'DRAFT',
    total_recipients INTEGER DEFAULT 0,
    delivered_count INTEGER DEFAULT 0,
    failed_count INTEGER DEFAULT 0,
    estimated_cost DECIMAL(10,2),
    actual_cost DECIMAL(10,2),
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,

    CONSTRAINT chk_message_type CHECK (type IN ('SMS', 'AUDIO', 'PUSH')),
    CONSTRAINT chk_priority CHECK (priority IN ('NORMAL', 'HIGH', 'URGENT')),
    CONSTRAINT chk_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'SENT', 'FAILED'))
);

CREATE TABLE message_channels (
    message_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    PRIMARY KEY (message_id, channel),
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_scheduled_at ON messages(scheduled_at);
CREATE INDEX idx_messages_created_at ON messages(created_at);
CREATE INDEX idx_messages_type ON messages(type);
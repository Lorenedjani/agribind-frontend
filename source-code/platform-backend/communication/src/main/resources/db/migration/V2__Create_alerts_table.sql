CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_id VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    target_audience VARCHAR(100),
    specific_zone VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_alert_type CHECK (type IN ('WEATHER', 'PRICE', 'PAYMENT', 'SECURITY', 'SYSTEM')),
    CONSTRAINT chk_alert_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE TABLE alert_channels (
    alert_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    PRIMARY KEY (alert_id, channel),
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE
);

CREATE INDEX idx_alerts_priority ON alerts(priority);
CREATE INDEX idx_alerts_is_active ON alerts(is_active);
CREATE INDEX idx_alerts_type ON alerts(type);
CREATE TABLE message_audit (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

CREATE TABLE alert_audit (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    old_status BOOLEAN,
    new_status BOOLEAN,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

CREATE INDEX idx_message_audit_message_id ON message_audit(message_id);
CREATE INDEX idx_alert_audit_alert_id ON alert_audit(alert_id);
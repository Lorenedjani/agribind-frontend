-- Composite indexes for better query performance
CREATE INDEX idx_messages_type_status ON messages(type, status);
CREATE INDEX idx_messages_audience_zone ON messages(target_audience, specific_zone);
CREATE INDEX idx_alerts_active_dates ON alerts(is_active, start_date, end_date);
CREATE INDEX idx_requests_urgency_status ON resource_requests(urgency, status);

-- Function to update sent_at timestamp
CREATE OR REPLACE FUNCTION update_message_sent_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'SENT' AND OLD.status != 'SENT' THEN
        NEW.sent_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for auto-updating sent_at
CREATE TRIGGER trigger_update_sent_at
    BEFORE UPDATE ON messages
    FOR EACH ROW
    EXECUTE FUNCTION update_message_sent_at();

-- Additional constraints
ALTER TABLE messages ADD CONSTRAINT chk_positive_recipients CHECK (total_recipients >= 0);
ALTER TABLE messages ADD CONSTRAINT chk_positive_delivered CHECK (delivered_count >= 0);
ALTER TABLE messages ADD CONSTRAINT chk_positive_failed CHECK (failed_count >= 0);
ALTER TABLE resource_requests ADD CONSTRAINT chk_positive_quantity CHECK (quantity > 0);
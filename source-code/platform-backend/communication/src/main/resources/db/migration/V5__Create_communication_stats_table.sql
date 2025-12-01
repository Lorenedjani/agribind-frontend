CREATE TABLE communication_stats (
    id BIGSERIAL PRIMARY KEY,
    stat_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_messages_sent INTEGER DEFAULT 0,
    messages_sent_this_week INTEGER DEFAULT 0,
    audio_messages_total INTEGER DEFAULT 0,
    audio_languages_supported INTEGER DEFAULT 0,
    active_alerts INTEGER DEFAULT 0,
    critical_alerts INTEGER DEFAULT 0,
    delivery_rate DECIMAL(5,2) DEFAULT 0.0,
    delivery_rate_change DECIMAL(5,2) DEFAULT 0.0,
    active_members INTEGER DEFAULT 0,
    active_loans_amount DECIMAL(15,2) DEFAULT 0.0,
    low_stock_alerts INTEGER DEFAULT 0
);

-- Insert initial statistics
INSERT INTO communication_stats (
    total_messages_sent,
    messages_sent_this_week,
    audio_messages_total,
    audio_languages_supported,
    active_alerts,
    critical_alerts,
    delivery_rate,
    delivery_rate_change,
    active_members,
    active_loans_amount,
    low_stock_alerts
) VALUES (
    1247,
    156,
    89,
    3,
    12,
    3,
    94.5,
    2.3,
    245,
    1250000.00,
    8
);
-- Sample messages
INSERT INTO messages (type, priority, title, content, target_audience, specific_zone, status, total_recipients, delivered_count, created_by) VALUES
('SMS', 'HIGH', 'Payment Reminder', 'Dear member, your loan payment is due tomorrow. Please ensure sufficient funds.', 'ACTIVE_MEMBERS', 'DOUALA_ZONE', 'SENT', 150, 142, 'system'),
('AUDIO', 'NORMAL', 'Weather Update', 'Heavy rainfall expected in Douala region tomorrow. Take necessary precautions.', 'DOUALA_ZONE', 'DOUALA', 'SENT', 200, 195, 'admin'),
('SMS', 'URGENT', 'Price Alert', 'Maize prices have increased by 15% in local markets.', 'ALL_MEMBERS', NULL, 'DRAFT', 0, 0, 'price_manager');

INSERT INTO message_channels (message_id, channel) VALUES
(1, 'SMS'),
(2, 'AUDIO'),
(3, 'SMS'),
(3, 'PUSH');

-- Sample alerts
INSERT INTO alerts (alert_id, type, priority, title, description, target_audience, is_active, start_date, end_date, created_by) VALUES
('ALT-001', 'WEATHER', 'HIGH', 'Heavy Rainfall Warning', 'Expected heavy rainfall in coastal regions for next 3 days.', 'DOUALA_ZONE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '3 days', 'weather_service'),
('ALT-002', 'PRICE', 'MEDIUM', 'Fertilizer Price Drop', 'Fertilizer prices have dropped by 20% in Yaounde markets.', 'YAOUNDE_ZONE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days', 'price_monitor'),
('ALT-003', 'PAYMENT', 'HIGH', 'Loan Payment Deadline', 'Final reminder for quarterly loan payments due this week.', 'ACTIVE_MEMBERS', false, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 'finance_dept');

INSERT INTO alert_channels (alert_id, channel) VALUES
(1, 'SMS'),
(1, 'PUSH'),
(2, 'SMS'),
(3, 'SMS');

-- Sample resource requests
INSERT INTO resource_requests (request_id, resource_name, quantity, unit, urgency, requested_by, requested_by_zone, status, suppliers_matched) VALUES
('REQ-001', 'Organic Fertilizer', 500, 'bags', 'HIGH', 'Farmers Cooperative', 'DOUALA', 'MATCHED', 3),
('REQ-002', 'Maize Seedlings', 2000, 'units', 'MEDIUM', 'Individual Farmer', 'YAOUNDE', 'PENDING', 1),
('REQ-003', 'Irrigation Equipment', 50, 'sets', 'CRITICAL', 'Agricultural Group', 'OTHER_REGIONS', 'FULFILLED', 2);
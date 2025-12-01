CREATE TABLE message_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE template_variables (
    template_id BIGINT NOT NULL,
    variable VARCHAR(100) NOT NULL,
    PRIMARY KEY (template_id, variable),
    FOREIGN KEY (template_id) REFERENCES message_templates(id) ON DELETE CASCADE
);

-- Insert default templates
INSERT INTO message_templates (name, content) VALUES
('Payment Reminder', 'Dear {name}, your payment of {amount} for {product} is due on {date}. Please make payment to avoid penalties.'),
('Weather Alert', 'Weather Alert: {alert_type} expected in {zone} on {date}. {instructions}'),
('Price Update', 'Price Update: {product} price is now {price} per {unit} in {market}.'),
('Loan Approval', 'Dear {name}, your loan application for {amount} has been approved. Funds will be disbursed on {date}.');

INSERT INTO template_variables (template_id, variable) VALUES
(1, '{name}'), (1, '{amount}'), (1, '{product}'), (1, '{date}'),
(2, '{alert_type}'), (2, '{zone}'), (2, '{date}'), (2, '{instructions}'),
(3, '{product}'), (3, '{price}'), (3, '{unit}'), (3, '{market}'),
(4, '{name}'), (4, '{amount}'), (4, '{date}');
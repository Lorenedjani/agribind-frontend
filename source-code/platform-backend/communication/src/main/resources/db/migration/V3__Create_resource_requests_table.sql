CREATE TABLE resource_requests (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) UNIQUE NOT NULL,
    resource_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit VARCHAR(50),
    urgency VARCHAR(50) NOT NULL,
    requested_by VARCHAR(100),
    requested_by_zone VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING',
    suppliers_matched INTEGER DEFAULT 0,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fulfilled_date TIMESTAMP,

    CONSTRAINT chk_urgency CHECK (urgency IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'MATCHED', 'FULFILLED', 'CANCELLED'))
);

CREATE INDEX idx_resource_requests_status ON resource_requests(status);
CREATE INDEX idx_resource_requests_urgency ON resource_requests(urgency);
CREATE INDEX idx_resource_requests_zone ON resource_requests(requested_by_zone);
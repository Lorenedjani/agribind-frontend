CREATE TABLE market_price (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    commodity_code VARCHAR(64) NOT NULL,
    commodity_name VARCHAR(255) NOT NULL,
    market VARCHAR(255) NOT NULL,
    currency VARCHAR(16) NOT NULL DEFAULT 'XAF',
    price DOUBLE NOT NULL,
    price_source VARCHAR(32) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    checksum VARCHAR(128),
    UNIQUE KEY uk_commodity_market (commodity_code, market)
);

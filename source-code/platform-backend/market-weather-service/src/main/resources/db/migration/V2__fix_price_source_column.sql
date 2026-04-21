-- Migration: fix price_source column to match Hibernate @Enumerated(EnumType.STRING)
-- Root cause: V1 created the column as VARCHAR; Hibernate validate mode expects ENUM type.
-- Solution: convert to plain VARCHAR(20) and add NOT NULL with default, which Hibernate
--           accepts when the entity uses @Column(columnDefinition = "VARCHAR(20)").
--           We deliberately keep VARCHAR (not MySQL ENUM) because @Enumerated(STRING)
--           maps to VARCHAR at the JDBC level — the schema-validation mismatch was caused
--           by a MySQL ENUM type in V1. This migration normalises it.

ALTER TABLE market_price
    MODIFY COLUMN price_source VARCHAR(20) NOT NULL DEFAULT 'SYSTEM';

-- Back-fill any NULLs that slipped in before the NOT NULL constraint
UPDATE market_price SET price_source = 'SYSTEM' WHERE price_source IS NULL OR price_source = '';
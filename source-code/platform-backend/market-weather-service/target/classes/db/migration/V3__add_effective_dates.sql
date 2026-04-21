-- Migration V3: add effective_from / effective_to columns to market_price
-- Root cause: MarketPriceEntity declares @Column("effective_from") and
-- @Column("effective_to") but V1 never created them.
-- Hibernate ddl-auto=validate therefore fails at startup with
-- "Schema-validation: missing column [effective_from] in table [market_price]".
--
-- Both columns are nullable (not all prices have validity windows).

ALTER TABLE market_price
    ADD COLUMN effective_from DATE NULL,
    ADD COLUMN effective_to   DATE NULL;
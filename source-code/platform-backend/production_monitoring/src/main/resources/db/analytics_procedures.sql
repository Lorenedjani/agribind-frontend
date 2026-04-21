-- Reference SQL for production analytics stored procedures (MySQL).
-- Authoritative runtime DDL: AnalyticsProcedureInitializer (runs on ApplicationReadyEvent).
-- Apply manually only if you need to repair DB without restarting the service.
-- Database: same as JPA spring.datasource (production_records table).

-- DELIMITER //  -- use when running in mysql client with custom delimiter

DROP PROCEDURE IF EXISTS GetCropsByRegion;

CREATE PROCEDURE GetCropsByRegion(
    IN p_cooperative_id VARCHAR(255),
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT
        IFNULL(farmer_region, 'Unknown') AS region,
        product_name AS cropName,
        SUM(quantity) AS totalQuantity,
        MAX(unit) AS unit,
        COUNT(DISTINCT farmer_id) AS farmerCount
    FROM production_records
    WHERE
        (p_cooperative_id IS NULL OR cooperative_id = p_cooperative_id)
        AND production_date BETWEEN p_start_date AND p_end_date
    GROUP BY IFNULL(farmer_region, 'Unknown'), product_name
    ORDER BY region ASC, totalQuantity DESC;
END;

DROP PROCEDURE IF EXISTS GetCropsByFarmer;

CREATE PROCEDURE GetCropsByFarmer(
    IN p_cooperative_id VARCHAR(255),
    IN p_region VARCHAR(255),
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT
        farmer_id AS farmer_id,
        MAX(IFNULL(farmer_name, 'Unknown')) AS farmer_name,
        MAX(IFNULL(farmer_region, 'Unknown')) AS farmer_region,
        product_name AS product_name,
        SUM(quantity) AS crop_quantity,
        SUM(value_xaf) AS crop_value_xaf,
        MAX(IFNULL(quality_grade, 'N/A')) AS quality_grade,
        COUNT(id) AS delivery_count_per_crop
    FROM production_records
    WHERE
        (p_cooperative_id IS NULL OR cooperative_id = p_cooperative_id)
        AND (p_region IS NULL OR farmer_region = p_region)
        AND production_date BETWEEN p_start_date AND p_end_date
    GROUP BY farmer_id, product_name
    ORDER BY crop_value_xaf DESC;
END;

-- Jasper / reporting (optional): same filters as ReportService JPA queries.
DROP PROCEDURE IF EXISTS GetFarmerProductionReport;

CREATE PROCEDURE GetFarmerProductionReport(
    IN p_cooperative_id VARCHAR(255),
    IN p_region VARCHAR(255),
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT
        production_date,
        farmer_id,
        farmer_name,
        farmer_region,
        product_name,
        quantity,
        unit,
        value_xaf
    FROM production_records
    WHERE
        (p_cooperative_id IS NULL OR cooperative_id = p_cooperative_id)
        AND (p_region IS NULL OR farmer_region = p_region)
        AND production_date BETWEEN p_start_date AND p_end_date
    ORDER BY production_date DESC;
END;

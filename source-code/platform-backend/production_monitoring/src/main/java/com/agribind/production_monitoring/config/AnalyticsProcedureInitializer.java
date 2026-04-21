package com.agribind.production_monitoring.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsProcedureInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = false)
    public void initializeStoredProcedures() {
        log.info("Initializing MySQL Stored Procedures for Analytics...");

        try {
            // Procedure 1: GetCropsByRegion
            jdbcTemplate.execute("DROP PROCEDURE IF EXISTS GetCropsByRegion");
            
            String createCropsByRegionProc = """
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
                END
                """;
            
            jdbcTemplate.execute(createCropsByRegionProc);
            log.info("Stored Procedure 'GetCropsByRegion' initialized successfully.");

            // Procedure 2: GetCropsByFarmer
            jdbcTemplate.execute("DROP PROCEDURE IF EXISTS GetCropsByFarmer");
            
            String createCropsByFarmerProc = """
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
                END
                """;
                
            jdbcTemplate.execute(createCropsByFarmerProc);
            log.info("Stored Procedure 'GetCropsByFarmer' initialized successfully.");

        } catch (Exception e) {
            log.error("Failed to initialize stored procedures: {}", e.getMessage(), e);
        }
    }
}

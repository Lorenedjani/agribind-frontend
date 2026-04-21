package cm.agribind.marketweather.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_price",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_market_price_code_market",
                columnNames = {"commodity_code", "market"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commodity_code", nullable = false, length = 50)
    private String commodityCode;

    @Column(name = "commodity_name", length = 100)
    private String commodityName;

    @Column(name = "market", nullable = false, length = 100)
    private String market;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "price", nullable = false)
    private Double price;

    /**
     * FIX: use columnDefinition = "VARCHAR(20)" so Hibernate schema-validation
     * matches the column type produced by V2 migration (plain VARCHAR, not MySQL ENUM).
     * Without this, Hibernate generates/expects ENUM('system','government') which
     * conflicts with the VARCHAR column created by the original V1 migration.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "price_source", nullable = false, columnDefinition = "VARCHAR(20)")
    private PriceSource priceSource;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "version")
    private Long version;

    @Column(name = "checksum", length = 64)
    private String checksum;
}
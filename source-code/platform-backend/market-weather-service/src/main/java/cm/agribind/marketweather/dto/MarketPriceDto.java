package cm.agribind.marketweather.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MarketPriceDto(
        String commodityCode,
        String commodityName,
        String market,
        String currency,
        double price,
        LocalDateTime updatedAt,
        long version,
        String checksum,
        String priceSource,
        LocalDate effectiveFrom,   // ← new: returned to Angular/mobile so clients know validity window
        LocalDate effectiveTo      // ← new
) {}
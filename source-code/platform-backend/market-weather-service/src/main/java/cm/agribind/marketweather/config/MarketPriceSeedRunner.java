package cm.agribind.marketweather.config;

import cm.agribind.marketweather.model.MarketPriceEntity;
import cm.agribind.marketweather.model.PriceSource;
import cm.agribind.marketweather.repository.MarketPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Seeds baseline SYSTEM prices when the table is empty (first deploy / empty DB).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketPriceSeedRunner {

    private final MarketPriceRepository marketPriceRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (marketPriceRepository.count() > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        marketPriceRepository.save(entity("MAIZE", "Maize", "Douala", "XAF", 250, now));
        marketPriceRepository.save(entity("COCOA", "Cocoa", "Yaounde", "XAF", 1800, now));
        log.info("Seeded default SYSTEM market prices (MAIZE, COCOA).");
    }

    private static MarketPriceEntity entity(
            String code, String name, String market, String currency, double price, LocalDateTime t) {
        return MarketPriceEntity.builder()
                .commodityCode(code)
                .commodityName(name)
                .market(market)
                .currency(currency)
                .price(price)
                .priceSource(PriceSource.SYSTEM)
                .updatedAt(t)
                .version(1L)
                .checksum("seed-" + code)
                .build();
    }
}

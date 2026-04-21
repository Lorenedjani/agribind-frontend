package cm.agribind.marketweather.service;

import cm.agribind.marketweather.dto.*;
import cm.agribind.marketweather.model.MarketPriceEntity;
import cm.agribind.marketweather.model.PriceSource;
import cm.agribind.marketweather.repository.MarketPriceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MarketWeatherService {

    private final MarketPriceRepository marketPriceRepository;

    private final Map<String, WeatherForecastDto> weatherCache = new ConcurrentHashMap<>();
    private final List<WeatherAlertDto> weatherAlerts = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> weatherSubscriptions = Collections.synchronizedSet(new HashSet<>());

    @PostConstruct
    void initWeatherDemoData() {
        LocalDateTime now = LocalDateTime.now();
        weatherCache.put("north", defaultForecast("north", 1));
        weatherAlerts.add(new WeatherAlertDto("north", "MEDIUM", "Heavy rainfall expected in 24 hours", now));
    }

    public List<MarketPriceDto> getMarketPrices(LocalDateTime updatedSince) {
        return marketPriceRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toDto)
                .filter(p -> updatedSince == null || p.updatedAt().isAfter(updatedSince))
                .toList();
    }

    public List<MarketPriceDto> getAllMarketPricesForAdmin() {
        return marketPriceRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public MarketPriceDto upsertGovernmentPrice(UpsertMarketPriceRequest req) {
        String code   = req.commodityCode().trim().toUpperCase(Locale.ROOT);
        String market = req.market().trim();
        LocalDateTime now = LocalDateTime.now();

        MarketPriceEntity entity = marketPriceRepository
                .findByCommodityCodeAndMarket(code, market)
                .orElse(MarketPriceEntity.builder()
                        .commodityCode(code)
                        .market(market)
                        .version(0L)
                        .build());

        entity.setCommodityName(req.commodityName().trim());
        entity.setCurrency(req.currency().trim());
        entity.setPrice(req.price());
        entity.setPriceSource(PriceSource.GOVERNMENT);
        entity.setUpdatedAt(now);
        entity.setVersion(entity.getVersion() + 1);
        entity.setChecksum(checksum(code + ":" + market + ":" + req.price() + ":" + now));

        // FIX: persist effectiveFrom / effectiveTo sent by the Angular dashboard
        entity.setEffectiveFrom(req.effectiveFrom());
        entity.setEffectiveTo(req.effectiveTo());

        return toDto(marketPriceRepository.save(entity));
    }

    public List<PriceTrendDto> getPriceTrends() {
        return marketPriceRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(e -> new PriceTrendDto(e.getCommodityCode(), e.getCommodityName(), e.getPrice(), 0.0, 0.0))
                .toList();
    }

    public WeatherForecastDto getWeatherForecast(String region) {
        return weatherCache.computeIfAbsent(
                region.toLowerCase(Locale.ROOT), key -> defaultForecast(region, 1L));
    }

    public List<WeatherAlertDto> getWeatherAlerts(String region) {
        return weatherAlerts.stream()
                .filter(a -> a.region().equalsIgnoreCase(region))
                .toList();
    }

    public Map<String, Object> subscribeToWeather(WeatherSubscribeRequest request) {
        weatherSubscriptions.add(
                request.farmerId() + ":" + request.region() + ":" + request.channel());
        return Map.of("status", "subscribed",
                "region", request.region(),
                "channel", request.channel());
    }

    @Scheduled(cron = "${market.refresh.cron:0 0 6 * * *}")
    @Transactional
    public void scheduledRefresh() {
        for (MarketPriceEntity e : marketPriceRepository.findByPriceSource(PriceSource.SYSTEM)) {
            double adjusted = Math.round((e.getPrice() * 1.01) * 100.0) / 100.0;
            LocalDateTime now = LocalDateTime.now();
            e.setPrice(adjusted);
            e.setUpdatedAt(now);
            e.setVersion(e.getVersion() + 1);
            e.setChecksum(checksum(e.getCommodityCode() + ":" + adjusted + ":" + now));
            marketPriceRepository.save(e);
        }
    }

    // ── DTO mapping ──────────────────────────────────────────────────────────────

    private MarketPriceDto toDto(MarketPriceEntity e) {
        return new MarketPriceDto(
                e.getCommodityCode(),
                e.getCommodityName(),
                e.getMarket(),
                e.getCurrency(),
                e.getPrice(),
                e.getUpdatedAt(),
                e.getVersion(),
                e.getChecksum() != null
                        ? e.getChecksum()
                        : checksum(e.getCommodityCode() + e.getPrice()),
                e.getPriceSource() != null ? e.getPriceSource().name() : "SYSTEM",
                e.getEffectiveFrom(),   // ← new
                e.getEffectiveTo()      // ← new
        );
    }

    private WeatherForecastDto defaultForecast(String region, long version) {
        LocalDateTime now = LocalDateTime.now();
        return new WeatherForecastDto(region, 19.0, 30.0, 65.0, 42.0, now, version,
                checksum(region + now));
    }

    private String checksum(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
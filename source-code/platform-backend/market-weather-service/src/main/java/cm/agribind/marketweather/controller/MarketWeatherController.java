package cm.agribind.marketweather.controller;

import cm.agribind.marketweather.dto.*;
import cm.agribind.marketweather.service.MarketWeatherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MarketWeatherController {
    private final MarketWeatherService marketWeatherService;

    @Value("${app.market.admin-key:}")
    private String adminKey;

    @GetMapping("/market/prices")
    public ResponseEntity<List<MarketPriceDto>> marketPrices(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime updatedSince
    ) {
        return ResponseEntity.ok(marketWeatherService.getMarketPrices(updatedSince));
    }

    @GetMapping("/market/prices/trends")
    public ResponseEntity<List<PriceTrendDto>> marketPriceTrends() {
        return ResponseEntity.ok(marketWeatherService.getPriceTrends());
    }

    /** Government / back-office: list all rows (international reference prices). */
    @GetMapping("/market/admin/prices")
    public ResponseEntity<List<MarketPriceDto>> adminMarketPrices(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKeyHeader) {
        if (!adminRequestAllowed(adminKeyHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(marketWeatherService.getAllMarketPricesForAdmin());
    }

    /** Government: upsert a price row (stored as GOVERNMENT; not overwritten by daily SYSTEM simulation). */
    @PostMapping("/market/admin/prices")
    public ResponseEntity<MarketPriceDto> upsertGovernmentPrice(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKeyHeader,
            @Valid @RequestBody UpsertMarketPriceRequest request) {
        if (!adminRequestAllowed(adminKeyHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(marketWeatherService.upsertGovernmentPrice(request));
    }

    private boolean adminRequestAllowed(String headerKey) {
        if (adminKey == null || adminKey.isBlank()) {
            return true;
        }
        return adminKey.equals(headerKey);
    }

    @GetMapping("/weather/forecast/{region}")
    public ResponseEntity<WeatherForecastDto> weatherForecast(@PathVariable String region) {
        return ResponseEntity.ok(marketWeatherService.getWeatherForecast(region));
    }

    @GetMapping("/weather/alerts/{region}")
    public ResponseEntity<List<WeatherAlertDto>> weatherAlerts(@PathVariable String region) {
        return ResponseEntity.ok(marketWeatherService.getWeatherAlerts(region));
    }

    @PostMapping("/weather/subscribe")
    public ResponseEntity<Map<String, Object>> subscribeWeather(@Valid @RequestBody WeatherSubscribeRequest request) {
        return ResponseEntity.ok(marketWeatherService.subscribeToWeather(request));
    }
}

package cm.agribind.marketweather.dto;

import java.time.LocalDateTime;

public record WeatherAlertDto(
        String region,
        String severity,
        String message,
        LocalDateTime createdAt
) {}

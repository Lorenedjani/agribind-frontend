package cm.agribind.marketweather.dto;

import java.time.LocalDateTime;

public record WeatherForecastDto(
        String region,
        double temperatureMin,
        double temperatureMax,
        double humidity,
        double rainProbability,
        LocalDateTime updatedAt,
        long version,
        String checksum
) {}

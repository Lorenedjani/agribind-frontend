package cm.agribind.marketweather.dto;

import jakarta.validation.constraints.NotBlank;

public record WeatherSubscribeRequest(
        @NotBlank String farmerId,
        @NotBlank String region,
        @NotBlank String channel
) {}

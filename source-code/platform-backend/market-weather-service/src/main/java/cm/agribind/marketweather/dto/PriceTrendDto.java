package cm.agribind.marketweather.dto;

public record PriceTrendDto(
        String commodityCode,
        String commodityName,
        double currentPrice,
        double change7dPct,
        double change30dPct
) {}

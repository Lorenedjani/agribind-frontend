package cm.agribind.marketweather.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/**
 * Government (or admin) sets international / reference market prices.
 */
public record UpsertMarketPriceRequest(
        @NotBlank String commodityCode,
        @NotBlank String commodityName,
        @NotBlank String market,
        @NotBlank String currency,
        @NotNull @PositiveOrZero Double price,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status
) {}

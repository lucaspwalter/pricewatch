package com.pricewatch.dto;

import com.pricewatch.model.PriceHistory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PriceHistoryResponse(Long id, BigDecimal price, OffsetDateTime capturedAt) {
    public static PriceHistoryResponse from(PriceHistory history) {
        return new PriceHistoryResponse(history.getId(), history.getPrice(), history.getCapturedAt());
    }
}

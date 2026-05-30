package com.pricewatch.dto;

import java.math.BigDecimal;

public final class ExternalDtos {
    private ExternalDtos() {
    }

    public record FakeStoreProductResponse(
            Integer id,
            String title,
            BigDecimal price,
            String image
    ) {
    }

    public record EvolutionSendTextRequest(String number, String text) {
    }
}

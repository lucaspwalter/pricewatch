package com.pricewatch.dto;

import com.pricewatch.model.Alert;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class AlertDtos {
    private AlertDtos() {
    }

    public record AlertRequest(Long productId, BigDecimal targetPrice) {
    }

    public record AlertPatchRequest(BigDecimal targetPrice, Boolean active) {
    }

    public record AlertResponse(
            Long id,
            Long productId,
            String productTitle,
            BigDecimal targetPrice,
            boolean active,
            OffsetDateTime createdAt
    ) {
        public static AlertResponse from(Alert alert) {
            return new AlertResponse(
                    alert.getId(),
                    alert.getProduct().getId(),
                    alert.getProduct().getTitle(),
                    alert.getTargetPrice(),
                    alert.isActive(),
                    alert.getCreatedAt()
            );
        }
    }
}

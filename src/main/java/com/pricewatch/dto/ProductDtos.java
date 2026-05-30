package com.pricewatch.dto;

import com.pricewatch.model.Product;
import java.time.OffsetDateTime;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record ProductRequest(Integer productId) {
    }

    public record ProductResponse(Long id, String externalId, String title, String url, OffsetDateTime createdAt) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.getId(),
                    product.getExternalId(),
                    product.getTitle(),
                    product.getUrl(),
                    product.getCreatedAt()
            );
        }
    }
}

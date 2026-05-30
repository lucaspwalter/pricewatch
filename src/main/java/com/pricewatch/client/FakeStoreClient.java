package com.pricewatch.client;

import com.pricewatch.dto.ExternalDtos.FakeStoreProductResponse;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FakeStoreClient {

    private static final String PRODUCT_URL = "https://fakestoreapi.com/products/{id}";

    private final RestTemplate restTemplate;

    public FakeStoreClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public FakeStoreProductResponse getProduct(String id) {
        return restTemplate.getForObject(PRODUCT_URL, FakeStoreProductResponse.class, id);
    }

    public BigDecimal getCurrentPrice(String id) {
        FakeStoreProductResponse product = getProduct(id);
        if (product == null || product.price() == null) {
            throw new IllegalStateException("Preco nao encontrado para o produto " + id);
        }
        return product.price();
    }
}

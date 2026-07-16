package com.pricewatch.service;

import com.pricewatch.client.ProductPageClient;
import com.pricewatch.dto.PriceHistoryResponse;
import com.pricewatch.dto.ProductDtos.ProductRequest;
import com.pricewatch.dto.ProductDtos.ProductResponse;
import com.pricewatch.model.Product;
import com.pricewatch.repository.PriceHistoryRepository;
import com.pricewatch.repository.ProductRepository;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductPageClient productPageClient;

    public ProductService(
            ProductRepository productRepository,
            PriceHistoryRepository priceHistoryRepository,
            ProductPageClient productPageClient
    ) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.productPageClient = productPageClient;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (request.url() == null || request.url().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a URL do produto");
        }

        try {
            ProductPageClient.ProductData data = productPageClient.getProduct(request.url());
            String externalId = hash(data.url());
            return productRepository.findByExternalId(externalId)
                    .map(ProductResponse::from)
                    .orElseGet(() -> ProductResponse.from(productRepository.save(buildProduct(externalId, data))));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    public ProductResponse get(Long id) {
        return ProductResponse.from(findById(id));
    }

    public List<PriceHistoryResponse> getHistory(Long productId) {
        Product product = findById(productId);
        return priceHistoryRepository.findByProductOrderByCapturedAtDesc(product)
                .stream()
                .map(PriceHistoryResponse::from)
                .toList();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));
    }

    private Product buildProduct(String externalId, ProductPageClient.ProductData data) {
        Product product = new Product();
        product.setExternalId(externalId);
        product.setTitle(data.title());
        product.setUrl(data.url());
        return product;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel", exception);
        }
    }
}

package com.pricewatch.service;

import com.pricewatch.client.FakeStoreClient;
import com.pricewatch.dto.ExternalDtos.FakeStoreProductResponse;
import com.pricewatch.dto.PriceHistoryResponse;
import com.pricewatch.dto.ProductDtos.ProductRequest;
import com.pricewatch.dto.ProductDtos.ProductResponse;
import com.pricewatch.model.Product;
import com.pricewatch.repository.PriceHistoryRepository;
import com.pricewatch.repository.ProductRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final FakeStoreClient fakeStoreClient;

    public ProductService(
            ProductRepository productRepository,
            PriceHistoryRepository priceHistoryRepository,
            FakeStoreClient fakeStoreClient
    ) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.fakeStoreClient = fakeStoreClient;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (request.productId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um productId valido");
        }

        String externalId = String.valueOf(request.productId());
        return productRepository.findByExternalId(externalId)
                .map(ProductResponse::from)
                .orElseGet(() -> ProductResponse.from(productRepository.save(buildProduct(externalId))));
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

    private Product buildProduct(String externalId) {
        FakeStoreProductResponse fakeStoreProduct = fakeStoreClient.getProduct(externalId);
        if (fakeStoreProduct == null || fakeStoreProduct.id() == null || fakeStoreProduct.title() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um productId valido");
        }

        Product product = new Product();
        product.setExternalId(externalId);
        product.setTitle(fakeStoreProduct.title());
        product.setUrl("https://fakestoreapi.com/products/" + externalId);
        return product;
    }
}

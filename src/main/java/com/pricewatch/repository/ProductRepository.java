package com.pricewatch.repository;

import com.pricewatch.model.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByExternalId(String externalId);
}

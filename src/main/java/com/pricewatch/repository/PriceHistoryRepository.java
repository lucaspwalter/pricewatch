package com.pricewatch.repository;

import com.pricewatch.model.PriceHistory;
import com.pricewatch.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByProductOrderByCapturedAtDesc(Product product);
}

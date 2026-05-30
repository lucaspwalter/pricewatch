package com.pricewatch.repository;

import com.pricewatch.model.Alert;
import com.pricewatch.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    @EntityGraph(attributePaths = {"product", "user"})
    List<Alert> findByActiveTrue();

    @EntityGraph(attributePaths = {"product", "user"})
    List<Alert> findByUserOrderByCreatedAtDesc(User user);
}

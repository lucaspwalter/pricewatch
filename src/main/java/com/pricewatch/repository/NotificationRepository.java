package com.pricewatch.repository;

import com.pricewatch.model.Notification;
import com.pricewatch.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @EntityGraph(attributePaths = {"alert", "alert.product"})
    @Query("select n from Notification n where n.alert.user = :user order by n.sentAt desc")
    List<Notification> findByUser(@Param("user") User user);
}

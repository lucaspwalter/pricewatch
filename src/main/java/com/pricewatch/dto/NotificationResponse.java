package com.pricewatch.dto;

import com.pricewatch.model.Notification;
import com.pricewatch.model.NotificationChannel;
import java.time.OffsetDateTime;

public record NotificationResponse(
        Long id,
        Long alertId,
        String productTitle,
        NotificationChannel channel,
        OffsetDateTime sentAt,
        String status
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getAlert().getId(),
                notification.getAlert().getProduct().getTitle(),
                notification.getChannel(),
                notification.getSentAt(),
                notification.getStatus()
        );
    }
}

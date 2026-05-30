package com.pricewatch.service;

import com.pricewatch.client.WhatsAppClient;
import com.pricewatch.dto.NotificationResponse;
import com.pricewatch.model.Alert;
import com.pricewatch.model.Notification;
import com.pricewatch.model.NotificationChannel;
import com.pricewatch.repository.NotificationRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;
    private final WhatsAppClient whatsAppClient;

    public NotificationService(
            NotificationRepository notificationRepository,
            CurrentUserService currentUserService,
            WhatsAppClient whatsAppClient
    ) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
        this.whatsAppClient = whatsAppClient;
    }

    public List<NotificationResponse> listForCurrentUser() {
        return notificationRepository.findByUser(currentUserService.getCurrentUser())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void notifyPriceReached(Alert alert, BigDecimal currentPrice) {
        String message = "O produto " + alert.getProduct().getTitle()
                + " atingiu R$ " + currentPrice
                + ". Valor alvo: R$ " + alert.getTargetPrice()
                + ". Link: " + alert.getProduct().getUrl();

        sendWhatsApp(alert, message);
    }

    private void sendWhatsApp(Alert alert, String message) {
        try {
            String status = whatsAppClient.sendText(alert.getUser().getPhone(), message);
            save(alert, NotificationChannel.WHATSAPP, status);
        } catch (Exception exception) {
            log.warn("Falha ao enviar WhatsApp para alerta {}: {}", alert.getId(), exception.getMessage());
            save(alert, NotificationChannel.WHATSAPP, "ERROR");
        }
    }

    private void save(Alert alert, NotificationChannel channel, String status) {
        Notification notification = new Notification();
        notification.setAlert(alert);
        notification.setChannel(channel);
        notification.setStatus(status);
        notificationRepository.save(notification);
    }
}

package com.pricewatch.scheduler;

import com.pricewatch.client.ProductPageClient;
import com.pricewatch.model.Alert;
import com.pricewatch.model.PriceHistory;
import com.pricewatch.repository.AlertRepository;
import com.pricewatch.repository.PriceHistoryRepository;
import com.pricewatch.service.NotificationService;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PriceMonitorScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceMonitorScheduler.class);

    private final AlertRepository alertRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductPageClient productPageClient;
    private final NotificationService notificationService;

    public PriceMonitorScheduler(
            AlertRepository alertRepository,
            PriceHistoryRepository priceHistoryRepository,
            ProductPageClient productPageClient,
            NotificationService notificationService
    ) {
        this.alertRepository = alertRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.productPageClient = productPageClient;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void monitorPrices() {
        List<Alert> activeAlerts = alertRepository.findByActiveTrue();
        log.info("Iniciando monitoramento de {} alertas ativos", activeAlerts.size());

        for (Alert alert : activeAlerts) {
            try {
                processAlert(alert);
            } catch (Exception exception) {
                log.warn("Falha ao processar alerta {}: {}", alert.getId(), exception.getMessage());
            }
        }
    }

    @Transactional
    protected void processAlert(Alert alert) {
        BigDecimal currentPrice = productPageClient.getProduct(alert.getProduct().getUrl()).price();

        PriceHistory history = new PriceHistory();
        history.setProduct(alert.getProduct());
        history.setPrice(currentPrice);
        priceHistoryRepository.save(history);

        if (currentPrice.compareTo(alert.getTargetPrice()) <= 0) {
            notificationService.notifyPriceReached(alert, currentPrice);
            alert.setActive(false);
            alertRepository.save(alert);
            log.info("Alerta {} notificado e desativado", alert.getId());
        }
    }
}

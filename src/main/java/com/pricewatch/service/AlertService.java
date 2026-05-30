package com.pricewatch.service;

import com.pricewatch.dto.AlertDtos.AlertPatchRequest;
import com.pricewatch.dto.AlertDtos.AlertRequest;
import com.pricewatch.dto.AlertDtos.AlertResponse;
import com.pricewatch.model.Alert;
import com.pricewatch.model.User;
import com.pricewatch.repository.AlertRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final ProductService productService;
    private final CurrentUserService currentUserService;

    public AlertService(AlertRepository alertRepository, ProductService productService, CurrentUserService currentUserService) {
        this.alertRepository = alertRepository;
        this.productService = productService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AlertResponse create(AlertRequest request) {
        Alert alert = new Alert();
        alert.setUser(currentUserService.getCurrentUser());
        alert.setProduct(productService.findById(request.productId()));
        alert.setTargetPrice(request.targetPrice());
        alert.setActive(true);
        return AlertResponse.from(alertRepository.save(alert));
    }

    public List<AlertResponse> list() {
        return alertRepository.findByUserOrderByCreatedAtDesc(currentUserService.getCurrentUser())
                .stream()
                .map(AlertResponse::from)
                .toList();
    }

    @Transactional
    public AlertResponse update(Long id, AlertPatchRequest request) {
        Alert alert = findOwnedAlert(id);
        if (request.targetPrice() != null) {
            alert.setTargetPrice(request.targetPrice());
        }
        if (request.active() != null) {
            alert.setActive(request.active());
        }
        return AlertResponse.from(alert);
    }

    @Transactional
    public void delete(Long id) {
        alertRepository.delete(findOwnedAlert(id));
    }

    private Alert findOwnedAlert(Long id) {
        User user = currentUserService.getCurrentUser();
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta nao encontrado"));
        if (!alert.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta nao encontrado");
        }
        return alert;
    }
}

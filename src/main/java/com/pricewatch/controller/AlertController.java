package com.pricewatch.controller;

import com.pricewatch.dto.AlertDtos.AlertPatchRequest;
import com.pricewatch.dto.AlertDtos.AlertRequest;
import com.pricewatch.dto.AlertDtos.AlertResponse;
import com.pricewatch.service.AlertService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse create(@RequestBody AlertRequest request) {
        return alertService.create(request);
    }

    @GetMapping
    public List<AlertResponse> list() {
        return alertService.list();
    }

    @PatchMapping("/{id}")
    public AlertResponse update(@PathVariable Long id, @RequestBody AlertPatchRequest request) {
        return alertService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        alertService.delete(id);
    }
}

package com.pricewatch.controller;

import com.pricewatch.scheduler.PriceMonitorScheduler;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final PriceMonitorScheduler priceMonitorScheduler;

    public AdminController(PriceMonitorScheduler priceMonitorScheduler) {
        this.priceMonitorScheduler = priceMonitorScheduler;
    }

    @PostMapping("/run-scheduler")
    public Map<String, String> runScheduler() {
        priceMonitorScheduler.monitorPrices();
        return Map.of("status", "scheduler executed");
    }
}

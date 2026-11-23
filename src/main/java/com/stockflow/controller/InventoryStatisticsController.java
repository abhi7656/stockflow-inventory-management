package com.stockflow.controller;

import com.stockflow.service.InventoryStatisticsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class InventoryStatisticsController {

    private final InventoryStatisticsService inventoryStatisticsService;

    // Constructor injection
    public InventoryStatisticsController(
            InventoryStatisticsService inventoryStatisticsService) {

        this.inventoryStatisticsService = inventoryStatisticsService;
    }

    // REST API endpoint for inventory statistics
    @GetMapping("/statistics")
    public Map<String, Object> getInventoryStatistics() {

        return inventoryStatisticsService.getInventoryStatistics();
    }
}
package com.stockflow.controller;

import com.stockflow.service.InventoryStatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryDashboardController {

    private final InventoryStatisticsService statisticsService;

    public InventoryDashboardController(
            InventoryStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // Display Inventory Statistics Dashboard
    @GetMapping("/inventory/dashboard")
    public String showDashboard(Model model) {

        model.addAttribute(
                "statistics",
                statisticsService.getInventoryStatistics()
        );

        return "inventory-dashboard";
    }
}
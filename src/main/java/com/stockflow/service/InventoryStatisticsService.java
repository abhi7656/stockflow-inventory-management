package com.stockflow.service;

import com.stockflow.model.Product;
import com.stockflow.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class InventoryStatisticsService {

    private final ProductRepository productRepository;

    public InventoryStatisticsService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Calculate inventory statistics
    public Map<String, Object> getInventoryStatistics() {

        Map<String, Object> statistics = new LinkedHashMap<>();

        // Count total products
        long totalProducts = productRepository.count();

        // Count unique brands and categories
        long totalBrands = productRepository.countUniqueBrands();
        long totalCategories = productRepository.countUniqueCategories();

        // Calculate average product price
        Double averagePrice = productRepository.getAverageProductPrice();

        // Calculate total listed prices
        BigDecimal totalPrice = productRepository.getTotalListedPrice();

        // Handle empty database values
        if (averagePrice == null) {
            averagePrice = 0.0;
        }

        if (totalPrice == null) {
            totalPrice = BigDecimal.ZERO;
        }

        // Round average price to two decimal places
        BigDecimal roundedAverage = BigDecimal.valueOf(averagePrice)
                .setScale(2, RoundingMode.HALF_UP);

        // Find most expensive product
        String mostExpensiveProduct = productRepository
                .findFirstByOrderByUnitPriceDesc()
                .map(Product::getName)
                .orElse("No products available");

        // Find cheapest product
        String cheapestProduct = productRepository
                .findFirstByOrderByUnitPriceAsc()
                .map(Product::getName)
                .orElse("No products available");

        // Add results to response
        statistics.put("totalProducts", totalProducts);
        statistics.put("totalBrands", totalBrands);
        statistics.put("totalCategories", totalCategories);
        statistics.put("averageProductPrice", roundedAverage);
        statistics.put("totalListedPrice", totalPrice);
        statistics.put("mostExpensiveProduct", mostExpensiveProduct);
        statistics.put("cheapestProduct", cheapestProduct);

        return statistics;
    }
}
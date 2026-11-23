package com.stockflow.repository;

import com.stockflow.model.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Search products by name
    Page<Product> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    // Search products by SKU
    Page<Product> findBySkuContainingIgnoreCase(
            String sku,
            Pageable pageable
    );

    // Search products by name or SKU
    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String name,
            String sku
    );

    // Filter products by brand
    List<Product> findByBrandId(Long brandId);

    // Filter products by category
    List<Product> findByCategoryId(Long categoryId);

    // Combined search, filtering, sorting and pagination
    @Query("""
        SELECT p FROM Product p
        WHERE (
            :keyword = ''
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:brandId IS NULL OR p.brandId = :brandId)
        AND (:categoryId IS NULL OR p.categoryId = :categoryId)
        """)
    Page<Product> searchAndFilterProducts(
            @Param("keyword") String keyword,
            @Param("brandId") Long brandId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // Count unique brands
    @Query("SELECT COUNT(DISTINCT p.brandId) FROM Product p")
    Long countUniqueBrands();

    // Count unique categories
    @Query("SELECT COUNT(DISTINCT p.categoryId) FROM Product p")
    Long countUniqueCategories();

    // Calculate average product price
    @Query("SELECT AVG(p.unitPrice) FROM Product p")
    Double getAverageProductPrice();

    // Calculate total listed product prices
    @Query("SELECT SUM(p.unitPrice) FROM Product p")
    BigDecimal getTotalListedPrice();

    // Find the most expensive product
    Optional<Product> findFirstByOrderByUnitPriceDesc();

    // Find the cheapest product
    Optional<Product> findFirstByOrderByUnitPriceAsc();
}
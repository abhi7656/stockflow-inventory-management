package com.stockflow.service;

import com.stockflow.model.Product;
import com.stockflow.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Save or update a product
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Find product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Delete product by ID
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Search products by name or SKU
    public List<Product> searchProducts(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll();
        }

        return productRepository
                .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                        keyword, keyword);
    }

    // Filter products by brand ID
    public List<Product> filterByBrand(Long brandId) {
        return productRepository.findByBrandId(brandId);
    }

    // Filter products by category ID
    public List<Product> filterByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // Sort products by selected field
    public List<Product> getSortedProducts(String sortBy) {

        return productRepository.findAll(
                Sort.by(Sort.Direction.ASC, sortBy)
        );
    }

    // Pagination with sorting
    public Page<Product> getPaginatedProducts(
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return productRepository.findAll(pageable);
    }

    // Combined search, filtering, sorting and pagination
    public Page<Product> searchAndFilterProducts(
            String keyword,
            Long brandId,
            Long categoryId,
            int page,
            int size,
            String sortBy,
            String direction) {

        // Prevent invalid page numbers and page sizes
        page = Math.max(page, 0);
        size = Math.max(1, Math.min(size, 100));

        // Restrict sorting to supported fields
        if (!sortBy.equals("id")
                && !sortBy.equals("name")
                && !sortBy.equals("sku")
                && !sortBy.equals("unitPrice")) {
            sortBy = "id";
        }

        // Configure sorting direction
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Configure pagination
        Pageable pageable = PageRequest.of(page, size, sort);

        // Clean up the search keyword
        String searchKeyword = keyword == null
                ? ""
                : keyword.trim();

        // Execute combined database query
        return productRepository.searchAndFilterProducts(
                searchKeyword,
                brandId,
                categoryId,
                pageable
        );
    }
}
package com.stockflow.controller;

import com.stockflow.model.Product;
import com.stockflow.service.ProductService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ProductListingController {

    private final ProductService productService;

    public ProductListingController(ProductService productService) {
        this.productService = productService;
    }

    // Display all products
    @GetMapping("/catalog")
    public String showProductCatalog(Model model) {

        List<Product> products = productService.getAllProducts();

        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "StockFlow Product Catalog");

        return "product-listing";
    }

    // Display selected product details
    @GetMapping("/catalog/{id}")
    public String showProductDetails(
            @PathVariable Long id,
            Model model) {

        Product product = productService.getProductById(id).orElse(null);

        if (product == null) {
            return "redirect:/catalog";
        }

        model.addAttribute("product", product);
        model.addAttribute("pageTitle", "Product Details");

        return "product-details";
    }
}
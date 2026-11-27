package com.stockflow.controller;

import com.stockflow.model.Product;
import com.stockflow.service.ProductService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Display Edit Product form - ADMIN and STAFF only
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/edit/{id}")
    public String showEditProductForm(
            @PathVariable Long id, Model model) {

        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid product ID: " + id));

        model.addAttribute("product", product);

        return "edit-product";
    }

    // Delete product - ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        productService.deleteProduct(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product deleted successfully!"
        );

        return "redirect:/products";
    }

    // Display Add Product form - ADMIN and STAFF only
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/new")
    public String showAddProductForm(Model model) {

        model.addAttribute("product", new Product());

        return "add-product";
    }

    // Save or update product - ADMIN and STAFF only
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping("/save")
    public String saveProduct(
            @ModelAttribute Product product,
            RedirectAttributes redirectAttributes) {

        productService.saveProduct(product);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product saved successfully!"
        );

        return "redirect:/products";
    }

    // Display all products - ADMIN and STAFF only
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public String listProducts(Model model) {

        model.addAttribute(
                "products",
                productService.getAllProducts()
        );

        return "products";
    }
}
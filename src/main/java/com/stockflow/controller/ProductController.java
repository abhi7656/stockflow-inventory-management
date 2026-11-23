package com.stockflow.controller;

import com.stockflow.model.Product;
import com.stockflow.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Display Edit Product form
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {

        Product product = productService.getProductById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid product ID: " + id));

        model.addAttribute("product", product);

        return "edit-product";
    }

    // Delete product by ID
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {

        productService.deleteProduct(id);

        return "redirect:/products";
    }

    // Display Add Product form
    @GetMapping("/new")
    public String showAddProductForm(Model model) {

        model.addAttribute("product", new Product());

        return "add-product";
    }

    // Save product to database
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product) {

        productService.saveProduct(product);

        return "redirect:/products";
    }

    // Display products with search, filtering, sorting and pagination
    @GetMapping
    public String listProducts(

            @RequestParam(defaultValue = "") String keyword,

            @RequestParam(required = false) Long brandId,

            @RequestParam(required = false) Long categoryId,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "5") int size,

            @RequestParam(defaultValue = "id") String sortBy,

            @RequestParam(defaultValue = "asc") String direction,

            Model model) {

        // Validate page number and page size
        page = Math.max(page, 0);
        size = Math.max(1, Math.min(size, 100));

        // Validate sorting field
        if (!sortBy.equals("id")
                && !sortBy.equals("name")
                && !sortBy.equals("sku")
                && !sortBy.equals("unitPrice")) {
            sortBy = "id";
        }

        // Validate sorting direction
        direction = "desc".equalsIgnoreCase(direction)
                ? "desc"
                : "asc";

        // Execute combined search, filtering, sorting and pagination
        Page<Product> productPage =
                productService.searchAndFilterProducts(
                        keyword,
                        brandId,
                        categoryId,
                        page,
                        size,
                        sortBy,
                        direction
                );

        // Handle page numbers beyond available results
        if (productPage.getTotalPages() > 0
                && page >= productPage.getTotalPages()) {

            page = productPage.getTotalPages() - 1;

            productPage = productService.searchAndFilterProducts(
                    keyword,
                    brandId,
                    categoryId,
                    page,
                    size,
                    sortBy,
                    direction
            );
        }

        // Send product data to HTML
        model.addAttribute("products", productPage.getContent());

        // Pagination information
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        // Preserve search and filter values
        model.addAttribute("keyword", keyword);
        model.addAttribute("brandId", brandId);
        model.addAttribute("categoryId", categoryId);

        // Preserve sorting and pagination settings
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("size", size);

        return "products";
    }
}
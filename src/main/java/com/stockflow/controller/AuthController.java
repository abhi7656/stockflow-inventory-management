package com.stockflow.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.stockflow.dto.RegistrationRequest;
import com.stockflow.service.UserService;

import jakarta.validation.Valid;

// User Authentication Controller - Harpreet Kaur

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Display login page
    @GetMapping("/login")
    public String showLoginPage() {

        return "login";
    }

    // Display registration page
    @GetMapping("/register")
    public String showRegistrationPage(Model model) {

        model.addAttribute(
                "registrationRequest",
                new RegistrationRequest()
        );

        return "register";
    }

    // Process user registration
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationRequest")
            RegistrationRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check validation errors
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Check password confirmation
        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Passwords do not match."
            );

            return "register";
        }

        try {

            // Register user in database
            userService.registerUser(request);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Account created successfully! Please log in."
            );

            return "redirect:/login";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "register";

        } catch (DataIntegrityViolationException e) {

            model.addAttribute(
                    "errorMessage",
                    "Username or email is already registered."
            );

            return "register";
        }
    }
}
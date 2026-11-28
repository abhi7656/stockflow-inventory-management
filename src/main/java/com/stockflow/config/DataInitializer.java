package com.stockflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.stockflow.model.User;
import com.stockflow.repository.UserRepository;

// Database Account Initialization - Harpreet Kaur

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,

            @Value("${STOCKFLOW_ADMIN_PASSWORD:}")
            String adminPassword,

            @Value("${STOCKFLOW_STAFF_PASSWORD:}")
            String staffPassword,

            @Value("${STOCKFLOW_VIEWER_PASSWORD:}")
            String viewerPassword) {

        return args -> {

            createUser(
                    userRepository,
                    passwordEncoder,
                    "StockFlow Administrator",
                    "admin",
                    "admin@stockflow.example",
                    adminPassword,
                    "ADMIN"
            );

            createUser(
                    userRepository,
                    passwordEncoder,
                    "StockFlow Staff",
                    "staff",
                    "staff@stockflow.example",
                    staffPassword,
                    "STAFF"
            );

            createUser(
                    userRepository,
                    passwordEncoder,
                    "StockFlow Viewer",
                    "viewer",
                    "viewer@stockflow.example",
                    viewerPassword,
                    "VIEWER"
            );
        };
    }

    private void createUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String fullName,
            String username,
            String email,
            String password,
            String role) {

        // Skip account creation if no password is configured
        if (password == null || password.isBlank()) {
            return;
        }

        // Prevent duplicate accounts
        if (userRepository.existsByUsername(username)
                || userRepository.existsByEmail(email)) {
            return;
        }

        User user = new User();

        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(password));

        user.setRole(role);
        user.setEnabled(true);

        userRepository.save(user);

        System.out.println(
                "StockFlow account initialized: "
                + username + " | Role: " + role
        );
    }
}
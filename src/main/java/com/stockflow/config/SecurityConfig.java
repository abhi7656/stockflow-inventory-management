package com.stockflow.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.stockflow.service.CustomUserDetailsService;

// Role-Based Security - Abhinass Singh
// Database Authentication - Harpreet Kaur

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // BCrypt password hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomUserDetailsService customUserDetailsService)
            throws Exception {

        http

            // Database-backed authentication
            .userDetailsService(customUserDetailsService)

            // Role-based authorization
            .authorizeHttpRequests(auth -> auth

                // Allow custom error page rendering
                .dispatcherTypeMatchers(
                        DispatcherType.ERROR
                ).permitAll()

                // Public pages and static resources
                .requestMatchers(
                        "/",
                        "/about",
                        "/features",
                        "/login",
                        "/register",
                        "/error",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico"
                ).permitAll()

                // Administrator-only pages
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                // Product deletion - administrator only
                .requestMatchers(
                        "/products/delete/**",
                        "/products/*/delete"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/products/**"
                ).hasRole("ADMIN")

                // Product management
                .requestMatchers(
                        "/products",
                        "/products/**"
                ).hasAnyRole("ADMIN", "STAFF")

                // Inventory dashboard and statistics API
                .requestMatchers(
                        "/inventory/**",
                        "/api/products/statistics"
                ).hasAnyRole("ADMIN", "STAFF")

                // Product catalog
                .requestMatchers(
                        "/catalog",
                        "/catalog/**"
                ).hasAnyRole("ADMIN", "STAFF", "VIEWER")

                // Deny unconfigured routes
                .anyRequest().denyAll()
            )

            // Custom login page - Harpreet Kaur
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            // Secure logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
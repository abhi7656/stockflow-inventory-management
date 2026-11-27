package com.stockflow.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // BCrypt password hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Role-based security configuration
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
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
                .requestMatchers("/products", "/products/**")
                .hasAnyRole("ADMIN", "STAFF")

                // Inventory dashboard and statistics API
                .requestMatchers(
                        "/inventory/**",
                        "/api/products/statistics"
                ).hasAnyRole("ADMIN", "STAFF")

                // Product catalog
                .requestMatchers("/catalog", "/catalog/**")
                .hasAnyRole("ADMIN", "STAFF", "VIEWER")

                // Deny unconfigured routes
                .anyRequest().denyAll()
            )

            // Login configuration
            .formLogin(form -> form
                .defaultSuccessUrl("/", true)
                .permitAll()
            )

            // Logout configuration
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

    // Temporary development accounts
    // Passwords are loaded from environment variables.
    // Remove this bean when database authentication is integrated.
    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${STOCKFLOW_ADMIN_PASSWORD}") String adminPassword,
            @Value("${STOCKFLOW_STAFF_PASSWORD}") String staffPassword,
            @Value("${STOCKFLOW_VIEWER_PASSWORD}") String viewerPassword) {

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();

        UserDetails staff = User.builder()
                .username("staff")
                .password(passwordEncoder.encode(staffPassword))
                .roles("STAFF")
                .build();

        UserDetails viewer = User.builder()
                .username("viewer")
                .password(passwordEncoder.encode(viewerPassword))
                .roles("VIEWER")
                .build();

        return new InMemoryUserDetailsManager(
                admin,
                staff,
                viewer
        );
    }
}
package com.stockflow.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.dto.RegistrationRequest;
import com.stockflow.model.User;
import com.stockflow.repository.UserRepository;

// User Authentication and Registration - Harpreet Kaur

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> RESERVED_USERNAMES =
            Set.of("admin", "staff", "viewer");

    private static final Set<String> RESERVED_EMAILS =
            Set.of(
                "admin@stockflow.example",
                "staff@stockflow.example",
                "viewer@stockflow.example"
            );

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegistrationRequest request) {

        // Normalize username and email
        String username = request.getUsername()
                .trim().toLowerCase(Locale.ROOT);

        String email = request.getEmail()
                .trim().toLowerCase(Locale.ROOT);

        // Protect reserved system usernames
        if (RESERVED_USERNAMES.contains(username)) {

            throw new IllegalArgumentException(
                    "This username is reserved. "
                    + "Please choose another username.");
        }

        // Protect reserved system email addresses
        if (RESERVED_EMAILS.contains(email)) {

            throw new IllegalArgumentException(
                    "This email address is reserved.");
        }

        // Check duplicate username
        if (userRepository.existsByUsername(username)) {

            throw new IllegalArgumentException(
                    "Username is already registered.");
        }

        // Check duplicate email
        if (userRepository.existsByEmail(email)) {

            throw new IllegalArgumentException(
                    "Email is already registered.");
        }

        // Check password confirmation
        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Passwords do not match.");
        }

        // Check BCrypt password length limit
        if (request.getPassword()
                .getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException(
                    "Password is too long.");
        }

        // Create new user
        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setUsername(username);
        user.setEmail(email);

        // Hash password using BCrypt
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Assign secure default role
        user.setRole("VIEWER");
        user.setEnabled(true);

        // Save user in database
        return userRepository.save(user);
    }
}
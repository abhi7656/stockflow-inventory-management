package com.stockflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.stockflow.model.User;
import com.stockflow.repository.UserRepository;

// Database Authentication Testing - Harpreet Kaur

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:stockflow-auth-tests",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "STOCKFLOW_ADMIN_PASSWORD=",
        "STOCKFLOW_STAFF_PASSWORD=",
        "STOCKFLOW_VIEWER_PASSWORD="
})

class AuthenticationTests {
    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;
    private static final String PASSWORD = "Test@12345";

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();
        userRepository.deleteAll();
    }

    private MockHttpServletRequestBuilder registration(String username, String email,
            String password, String confirmation) {
        return post("/register").with(csrf())
                .param("fullName", "Test User")
                .param("username", username).param("email", email)
                .param("password", password).param("confirmPassword", confirmation);
    }

    private void registerTestUser() throws Exception {
        mockMvc.perform(registration("testuser", "test@example.com", PASSWORD, PASSWORD))
                .andExpect(redirectedUrl("/login"));
    }

    @Test void loginPageIsAccessible() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
    }
    @Test void registrationPageIsAccessible() throws Exception {
        mockMvc.perform(get("/register")).andExpect(status().isOk())
                .andExpect(view().name("register")).andExpect(model().attributeExists("registrationRequest"));
    }
    @Test void successfulRegistration() throws Exception {
        mockMvc.perform(registration("testuser", "test@example.com", PASSWORD, PASSWORD))
                .andExpect(redirectedUrl("/login")).andExpect(flash().attributeExists("successMessage"));
        assertTrue(userRepository.existsByUsername("testuser"));
    }
    @Test void passwordIsHashed() throws Exception {
        registerTestUser();
        User user = userRepository.findByUsername("testuser").orElseThrow();
        assertNotEquals(PASSWORD, user.getPassword());
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPassword()));
    }
    @Test void newUserReceivesViewerRole() throws Exception {
        registerTestUser();
        User user = userRepository.findByUsername("testuser").orElseThrow();
        assertEquals("VIEWER", user.getRole());
        assertTrue(user.isEnabled());
    }
    @Test void duplicateUsernameIsRejected() throws Exception {
        registerTestUser();
        mockMvc.perform(registration("testuser", "other@example.com", PASSWORD, PASSWORD))
                .andExpect(status().isOk()).andExpect(model().attributeExists("errorMessage"));
        assertEquals(1L, userRepository.count());
    }
    @Test void duplicateEmailIsRejected() throws Exception {
        registerTestUser();
        mockMvc.perform(registration("otheruser", "test@example.com", PASSWORD, PASSWORD))
                .andExpect(status().isOk()).andExpect(model().attributeExists("errorMessage"));
        assertEquals(1L, userRepository.count());
    }
    @Test void mismatchedPasswordsAreRejected() throws Exception {
        mockMvc.perform(registration("testuser", "test@example.com", PASSWORD, "Wrong@12345"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("registrationRequest", "confirmPassword"));
        assertEquals(0L, userRepository.count());
    }
    @Test void invalidEmailIsRejected() throws Exception {
        mockMvc.perform(registration("testuser", "invalid-email", PASSWORD, PASSWORD))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("registrationRequest", "email"));
        assertEquals(0L, userRepository.count());
    }
    @Test void shortPasswordIsRejected() throws Exception {
        mockMvc.perform(registration("testuser", "test@example.com", "short", "short"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("registrationRequest", "password"));
        assertEquals(0L, userRepository.count());
    }
    @Test void reservedUsernameIsRejected() throws Exception {
        mockMvc.perform(registration("ADMIN", "test@example.com", PASSWORD, PASSWORD))
                .andExpect(status().isOk()).andExpect(model().attributeExists("errorMessage"));
        assertEquals(0L, userRepository.count());
    }
    @Test void reservedEmailIsRejected() throws Exception {
        mockMvc.perform(registration("testuser", "admin@stockflow.example", PASSWORD, PASSWORD))
                .andExpect(status().isOk()).andExpect(model().attributeExists("errorMessage"));
        assertEquals(0L, userRepository.count());
    }
    @Test void validLoginIsSuccessful() throws Exception {
        registerTestUser();
        mockMvc.perform(formLogin().user("testuser").password(PASSWORD))
                .andExpect(authenticated().withUsername("testuser").withRoles("VIEWER"))
                .andExpect(redirectedUrl("/"));
    }
    @Test void invalidLoginIsRejected() throws Exception {
        registerTestUser();
        mockMvc.perform(formLogin().user("testuser").password("WrongPassword"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }
    @Test void logoutIsSuccessful() throws Exception {
        mockMvc.perform(post("/logout").with(user("testuser").roles("VIEWER")).with(csrf()))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?logout"));
    }
    @Test void registrationWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/register").param("fullName", "Test User")
                .param("username", "testuser").param("email", "test@example.com")
                .param("password", PASSWORD).param("confirmPassword", PASSWORD))
                .andExpect(status().isForbidden());
        assertEquals(0L, userRepository.count());
    }
    @Test void guestCannotAccessProtectedProducts() throws Exception {
        mockMvc.perform(get("/products")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
    @Test void registrationCannotAssignAdminRole() throws Exception {
        mockMvc.perform(registration("testuser", "test@example.com", PASSWORD, PASSWORD).param("role", "ADMIN"))
                .andExpect(redirectedUrl("/login"));
        assertEquals("VIEWER", userRepository.findByUsername("testuser").orElseThrow().getRole());
    }
}

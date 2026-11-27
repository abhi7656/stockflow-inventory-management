package com.stockflow;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:stockflow-security-tests",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    // TEST 01
    @Test
    void guestCanAccessHomePage() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    // TEST 02
    @Test
    void guestCanAccessAboutPage() throws Exception {

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk());
    }

    // TEST 03
    @Test
    void guestCanAccessFeaturesPage() throws Exception {

        mockMvc.perform(get("/features"))
                .andExpect(status().isOk());
    }

    // TEST 04
    @Test
    void guestCanAccessLoginPage() throws Exception {

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    // TEST 05
    @Test
    void guestIsRedirectedToLogin() throws Exception {

        mockMvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // TEST 06
    @Test
    void viewerCannotAccessProducts() throws Exception {

        mockMvc.perform(get("/products")
                        .with(user("viewer").roles("VIEWER")))
                .andExpect(status().isForbidden());
    }

    // TEST 07
    @Test
    void viewerCannotAddProducts() throws Exception {

        mockMvc.perform(get("/products/new")
                        .with(user("viewer").roles("VIEWER")))
                .andExpect(status().isForbidden());
    }

    // TEST 08
    @Test
    void viewerCannotEditProducts() throws Exception {

        mockMvc.perform(get("/products/edit/1")
                        .with(user("viewer").roles("VIEWER")))
                .andExpect(status().isForbidden());
    }

    // TEST 09
    @Test
    void viewerCannotDeleteProducts() throws Exception {

        mockMvc.perform(post("/products/delete/1")
                        .with(user("viewer").roles("VIEWER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // TEST 10
    @Test
    void staffCanAccessProducts() throws Exception {

        mockMvc.perform(get("/products")
                        .with(user("staff").roles("STAFF")))
                .andExpect(status().isOk());
    }

    // TEST 11
    @Test
    void staffCanAccessAddProductPage() throws Exception {

        mockMvc.perform(get("/products/new")
                        .with(user("staff").roles("STAFF")))
                .andExpect(status().isOk());
    }

    // TEST 12
    @Test
    void staffCannotDeleteProducts() throws Exception {

        mockMvc.perform(post("/products/delete/1")
                        .with(user("staff").roles("STAFF"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // TEST 13
    @Test
    void staffCannotAccessAdminDashboard() throws Exception {

        mockMvc.perform(get("/admin/dashboard")
                        .with(user("staff").roles("STAFF")))
                .andExpect(status().isForbidden());
    }

    // TEST 14
    @Test
    void adminCanAccessProducts() throws Exception {

        mockMvc.perform(get("/products")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    // TEST 15
    @Test
    void adminCanAccessAddProductPage() throws Exception {

        mockMvc.perform(get("/products/new")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    // TEST 16
    @Test
    void adminCannotDeleteWithoutCsrfToken() throws Exception {

        mockMvc.perform(post("/products/delete/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    // TEST 17
    @Test
    void validAdminLoginIsSuccessful() throws Exception {

        mockMvc.perform(formLogin()
                        .user("admin")
                        .password("Admin@123"))
                .andExpect(authenticated()
                        .withUsername("admin")
                        .withRoles("ADMIN"));
    }

    // TEST 18
    @Test
    void validStaffLoginIsSuccessful() throws Exception {

        mockMvc.perform(formLogin()
                        .user("staff")
                        .password("Staff@123"))
                .andExpect(authenticated()
                        .withUsername("staff")
                        .withRoles("STAFF"));
    }

    // TEST 19
    @Test
    void validViewerLoginIsSuccessful() throws Exception {

        mockMvc.perform(formLogin()
                        .user("viewer")
                        .password("Viewer@123"))
                .andExpect(authenticated()
                        .withUsername("viewer")
                        .withRoles("VIEWER"));
    }

    // TEST 20
    @Test
    void invalidLoginIsRejected() throws Exception {

        mockMvc.perform(formLogin()
                        .user("admin")
                        .password("WrongPassword"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

  // TEST 21: Secure logout
@Test
void logoutClearsAuthentication() throws Exception {

    mockMvc.perform(post("/logout")
                    .with(user("staff").roles("STAFF"))
                    .with(csrf()))
            .andExpect(unauthenticated())
            .andExpect(redirectedUrl("/login?logout"));
}

}
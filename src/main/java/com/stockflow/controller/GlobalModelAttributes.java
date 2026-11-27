package com.stockflow.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addGlobalAttributes(
            Model model,
            Authentication authentication) {

        boolean authenticated =
                authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (!authenticated) {

            model.addAttribute("canManageProducts", false);
            model.addAttribute("isAdmin", false);

            return;
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        boolean isStaff = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_STAFF"::equals);

        String role;

        if (isAdmin) {
            role = "ADMIN";
        } else if (isStaff) {
            role = "STAFF";
        } else {
            role = "VIEWER";
        }

        model.addAttribute(
                "currentUsername",
                authentication.getName()
        );

        model.addAttribute("currentRole", role);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute(
                "canManageProducts",
                isAdmin || isStaff
        );
    }
}
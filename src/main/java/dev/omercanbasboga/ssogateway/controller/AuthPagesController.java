package dev.omercanbasboga.ssogateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Serves the server-rendered auth pages (login prompt, logout confirmation,
 * unauthorized notice) that sit in front of the OAuth2/OIDC authorization-code flow.
 */
@Controller
public class AuthPagesController {

    @GetMapping("/logout")
    public String logoutPage() {
        return "logout";
    }

    @GetMapping("/unauthorized")
    public String unauthorizedPage() {
        return "401unauthorized";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password!");
        }
        return "login";
    }

}

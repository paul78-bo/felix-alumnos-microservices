package com.peral.Felix_Frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "forward:/dashboard.html";
    }
    
    // Ruta para reset-password con token
    @GetMapping("/reset-password/{token}")
    public String resetPasswordWithToken(@PathVariable String token) {
        return "forward:/reset-password.html";
    }
    
    // Ruta para reset-password sin token
    @GetMapping("/reset-password")
    public String resetPassword() {
        return "forward:/reset-password.html";
    }
    
    // Ruta para forgot-password
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forward:/forgot-password.html";
    }
    
    // Ruta para login
    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }
    
    // Ruta para dashboard (por si alguien entra a /dashboard)
    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }
}
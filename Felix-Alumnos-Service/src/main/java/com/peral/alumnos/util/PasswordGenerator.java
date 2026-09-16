package com.peral.alumnos.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Super usuario secreto
        String superPassword = "Kl4veS3cr3t4#2024";
        String encodedSuperPassword = encoder.encode(superPassword);
        
        // Generar passwords más simples
        String adminPassword = "admin123";
        String encodedAdminPassword = encoder.encode(adminPassword);
        
        String profesorPassword = "prof123"; 
        String encodedProfesorPassword = encoder.encode(profesorPassword);
        
        String alumnoPassword = "alumno123";
        String encodedAlumnoPassword = encoder.encode(alumnoPassword);
        
        System.out.println("=== PASSWORDS BCrypt ===");
        System.out.println("Super Usuario: " + encodedSuperPassword);  // ← NUEVO
        System.out.println("Admin: " + encodedAdminPassword);
        System.out.println("Profesor: " + encodedProfesorPassword);
        System.out.println("Alumno: " + encodedAlumnoPassword);
        
        System.out.println("\n=== COMANDOS SQL ===");
        System.out.println("UPDATE usuarios SET password = '" + encodedAdminPassword + "' WHERE username = 'admin';");
        System.out.println("UPDATE usuarios SET password = '" + encodedProfesorPassword + "' WHERE username = 'profesor';");
        System.out.println("UPDATE usuarios SET password = '" + encodedAlumnoPassword + "' WHERE username = 'alumno';");
    }
}
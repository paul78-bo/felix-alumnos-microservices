package com.peral.alumnos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FelixAlumnosServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FelixAlumnosServiceApplication.class, args);
	}
	
	// ✅ ESTE MÉTODO SE EJECUTA AUTOMÁTICAMENTE
//	@Bean
//	public CommandLineRunner generatePasswords() {
//		return args -> {
//			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//			
//			System.out.println("=== GENERANDO CONTRASEÑAS BCrypt ===");
//			
//			String hashAdmin = encoder.encode("admin");
//			System.out.println("ADMIN: " + hashAdmin);
//			System.out.println("UPDATE usuarios SET password = '" + hashAdmin + "' WHERE username = 'admin';");
//			
//			String hashPassword = encoder.encode("password");
//			System.out.println("PROFESOR: " + hashPassword);
//			System.out.println("UPDATE usuarios SET password = '" + hashPassword + "' WHERE username = 'profesor';");
//			
//			String hash123456 = encoder.encode("123456");
//			System.out.println("ALUMNO: " + hash123456);
//			System.out.println("UPDATE usuarios SET password = '" + hash123456 + "' WHERE username = 'alumno';");
//			
//			// Verificación
//			System.out.println("\n=== VERIFICACIÓN ===");
//			System.out.println("Password 'admin' verificado: " + encoder.matches("admin", hashAdmin));
//		};
//	}
}
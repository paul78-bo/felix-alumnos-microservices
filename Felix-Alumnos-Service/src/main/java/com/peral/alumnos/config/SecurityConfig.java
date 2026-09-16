package com.peral.alumnos.config;

import com.peral.alumnos.filter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .cors(cors -> cors.and())
	        .sessionManagement(session -> session
	            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .headers(headers -> headers
	            .frameOptions(frameOptions -> frameOptions.sameOrigin())
	        )
	        .authorizeHttpRequests(auth -> auth
	            // 🔓 Endpoints públicos
	            .requestMatchers(
	                "/",                    // raíz
	                "/health",              // health check
	                "/api/auth/**",         // todo auth (login, forgot-password, etc.)
	                "/reset-password",      // GET con token
	                "/reset-password/**",   // POST y validaciones
	                "/api/tareas/acceso-entrega/**",  // Links seguros para ver entregas
	                "/h2-console/**",       // Consola H2 (solo desarrollo)
	                "/actuator/**",         // Métricas y health
	                // ✅ SWAGGER / OPENAPI - rutas públicas
	                "/swagger-ui.html",
	                "/swagger-ui/**",
	                "/v3/api-docs",
	                "/v3/api-docs/**",
	                "/swagger-resources",
	                "/swagger-resources/**",
	                "/webjars/**"
	            ).permitAll()
	            
	            // 🔐 Endpoints protegidos (requieren autenticación)
	            .requestMatchers("/api/alumnos/**").authenticated()
	            .requestMatchers("/api/tareas/**").authenticated()
	            .requestMatchers("/api/usuarios/**").authenticated()
	            
	            // 🔓 Todo lo demás por defecto
	            .anyRequest().permitAll()
	        )
	        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}
	
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
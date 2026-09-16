package com.peral.Felix_API_GATEWAY;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import com.peral.Felix_API_GATEWAY.gateway.JwtAuthenticationFilter;
import com.peral.Felix_API_GATEWAY.gateway.JwtUtil;

@SpringBootApplication
public class FelixApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(FelixApiGatewayApplication.class, args);
        System.out.println("🚀 API Gateway corriendo en http://localhost:8080");
        
        
        
        
    }
 // ✅ REGISTRAR EL FILTRO EXPLÍCITAMENTE
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil);
    }
    
        
}

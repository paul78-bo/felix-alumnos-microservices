package com.peral.Felix_Frontend;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FelixMicroSFrontendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FelixMicroSFrontendApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "8082"));
        app.run(args);
        System.out.println("🚀 Frontend INICIADO en: http://localhost:8082");
    }
}

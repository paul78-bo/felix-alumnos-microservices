package com.peral.Felix_API_GATEWAY.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret = "miClaveSecretaMuySeguraYFijaParaJWT12345678901234567890";
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
        System.out.println("🔑 JWT Secret configurado: " + (secret != null ? "✅" : "❌ NULL"));
    }
}
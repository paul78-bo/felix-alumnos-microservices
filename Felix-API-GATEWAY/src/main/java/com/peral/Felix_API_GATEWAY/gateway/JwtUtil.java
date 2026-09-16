package com.peral.Felix_API_GATEWAY.gateway;

import com.peral.Felix_API_GATEWAY.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private final String secretKey;
    private final Key signingKey;

    public JwtUtil(JwtConfig jwtConfig) {
        this.secretKey = jwtConfig.getSecret();
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        System.out.println("🔑 JWT Util inicializado - Longitud secreto: " + secretKey.length());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Token inválido: " + e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        Claims claims = getAllClaims(token);
        return claims.getSubject();
    }

    public String extractRole(String token) {
        Claims claims = getAllClaims(token);
        return claims.get("role", String.class);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
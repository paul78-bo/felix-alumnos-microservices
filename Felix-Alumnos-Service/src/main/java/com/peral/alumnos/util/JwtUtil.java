package com.peral.alumnos.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 🔐 Se obtiene desde application.yml o variable de entorno
	 @Value("${jwt.secret:miClaveSecretaMuySeguraYFijaParaJWT12345678901234567890}")
	    private String secretKeyValue;
    // ⏰ Tiempo de expiración configurable desde YAML
	 @Value("${jwt.expiration:1800000}") // 30 min
	    private long expirationTime;
	 
	   private SecretKey getSigningKey() {
	        // ✅ Verifica que secretKeyValue no sea nulo
	        if (secretKeyValue == null || secretKeyValue.isEmpty()) {
	            secretKeyValue = "miClaveSecretaMuySeguraYFijaParaJWT12345678901234567890";
	        }
	        return Keys.hmacShaKeyFor(secretKeyValue.getBytes(StandardCharsets.UTF_8));
	    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            System.out.println("🔐 Extrayendo claims del token...");
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("❌ Token expirado: " + e.getMessage());
            throw new RuntimeException("Token expirado");
        } catch (MalformedJwtException e) {
            System.out.println("❌ Token mal formado: " + e.getMessage());
            throw new RuntimeException("Token mal formado");
        } catch (JwtException e) {
            System.out.println("❌ Error JWT: " + e.getMessage());
            throw new RuntimeException("Token inválido: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            throw new RuntimeException("Error procesando token");
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String generateToken(UserDetails userDetails) {
        System.out.println("🔐 Generando token para: " + userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("ROLE_USER");

        claims.put("role", role);
        System.out.println("✅ Rol asignado: " + role);

        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        try {
            System.out.println("🔐 Creando token para: " + subject);
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            System.out.println("❌ Error creando token: " + e.getMessage());
            throw new RuntimeException("Error generando token JWT");
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean usernameValid = username.equals(userDetails.getUsername());
        boolean notExpired = !isTokenExpired(token);
        return usernameValid && notExpired;
    }

    public Boolean validateTokenStructure(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("❌ Estructura del token inválida: " + e.getMessage());
            return false;
        }
    }

    public String extractRole(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            System.out.println("❌ Error extrayendo rol: " + e.getMessage());
            return "ROLE_USER";
        }
    }

    public void printConfig() {
        System.out.println("🔐 JwtUtil Config:");
        System.out.println("   Secret Key: " + (secretKeyValue != null ? "PRESENTE (" + secretKeyValue.length() + " chars)" : "NULL"));
        System.out.println("   Expiration: " + expirationTime + " ms");
    }
    
    @PostConstruct
    public void init() {
        System.out.println("🔐 JwtUtil inicializado");
        System.out.println("   Secret length: " + (secretKeyValue != null ? secretKeyValue.length() : "null"));
        System.out.println("   Expiration: " + expirationTime + " ms");
    }
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String generateToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return createToken(claims, username);
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}

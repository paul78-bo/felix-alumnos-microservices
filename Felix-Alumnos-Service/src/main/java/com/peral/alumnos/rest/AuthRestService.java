package com.peral.alumnos.rest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.peral.alumnos.dto.ForgotPasswordRequest;
import com.peral.alumnos.dto.LoginRequest;
import com.peral.alumnos.dto.LoginResponse;
import com.peral.alumnos.dto.PasswordResetResponse;
import com.peral.alumnos.dto.ResetPasswordRequest;
import com.peral.alumnos.model.Usuario;
import com.peral.alumnos.repository.UsuarioRepository;
import com.peral.alumnos.service.PasswordResetService;
import com.peral.alumnos.service.RateLimiterService;
import com.peral.alumnos.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación, login y recuperación de contraseña")
public class AuthRestService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RateLimiterService rateLimiterService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordResetService passwordResetService;

    @Autowired
    public AuthRestService(AuthenticationManager authenticationManager, 
                           JwtUtil jwtUtil,
                           RateLimiterService rateLimiterService,
                           UsuarioRepository usuarioRepository,
                           PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.rateLimiterService = rateLimiterService;
        this.usuarioRepository = usuarioRepository;
        this.passwordResetService = passwordResetService;
    }
    
    @Operation(summary = "Health check", description = "Verifica si el servicio está activo")
    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("✅ Auth Service funcionando. Usa /api/auth/login para autenticarte.");
    }
    
    @Operation(summary = "Health check detallado", description = "Verifica el estado del servicio")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso, token generado"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "429", description = "Demasiados intentos, cuenta bloqueada temporalmente")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "Credenciales del usuario") @RequestBody LoginRequest loginRequest, 
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        String rateKey = loginRequest.getUsername() + ":" + clientIp;
        
        if (rateLimiterService.isBlocked(rateKey)) {
            long minutosRestantes = rateLimiterService.getBlockTimeRemaining(rateKey);
            long blockedUntil = System.currentTimeMillis() + (minutosRestantes * 60 * 1000);
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                        "error", "Demasiados intentos fallidos",
                        "message", "Espere " + minutosRestantes + " minutos antes de intentar nuevamente",
                        "blockedUntil", blockedUntil / 1000,
                        "minutesRemaining", minutosRestantes
                    ));
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
                );

            rateLimiterService.resetAttempts(rateKey);
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            Long userId = null;
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("ROLE_USER");
            
            try {
                Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                userId = usuario.getId();
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo obtener userId: " + e.getMessage());
                userId = 1L;
            }
            
            String token;
            if (userId != null) {
                token = jwtUtil.generateToken(loginRequest.getUsername(), userId, role);
            } else {
                token = jwtUtil.generateToken(userDetails);
            }

            LoginResponse response = new LoginResponse(token, role, loginRequest.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            rateLimiterService.registerFailedAttempt(rateKey);
            int intentosRestantes = rateLimiterService.getRemainingAttempts(rateKey);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "error", "Credenciales inválidas",
                        "remainingAttempts", intentosRestantes,
                        "message", "Le quedan " + intentosRestantes + " intentos"
                    ));
        }
    }
    
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un email con enlace para restablecer contraseña")
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.forgotPassword(request);
            PasswordResetResponse response = new PasswordResetResponse(
                "Se ha enviado un enlace de recuperación a tu correo", 
                true
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PasswordResetResponse response = new PasswordResetResponse(
                e.getMessage(), 
                false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @Operation(summary = "Restablecer contraseña", description = "Cambia la contraseña usando un shortId válido")
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request);
            PasswordResetResponse response = new PasswordResetResponse(
                "Contraseña restablecida exitosamente", 
                true
            );
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            PasswordResetResponse response = new PasswordResetResponse(
                e.getMessage(), 
                false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @Operation(summary = "Redirección para restablecer contraseña", description = "Redirige al frontend con el shortId")
    @GetMapping("/reset-password/{shortId}")
    public ResponseEntity<?> handleResetPasswordGet(
            @Parameter(description = "ShortId de recuperación") @PathVariable String shortId, 
            HttpServletResponse response) 
            throws IOException {
        
        System.out.println("🎯 BACKEND: Recibido shortId: " + shortId);
        
        try {
            boolean isValid = passwordResetService.validateShortId(shortId);
            System.out.println("🎯 BACKEND: validateShortId devolvió: " + isValid);
            
            if (!isValid) {
                System.out.println("❌ BACKEND: shortId inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token inválido o expirado");
            }
            
            System.out.println("✅ BACKEND: shortId válido, redirigiendo...");
            
            String frontendUrl = "http://localhost:8082/reset-password.html?shortId=" + shortId;
            response.sendRedirect(frontendUrl);
            return null;
            
        } catch (Exception e) {
            System.out.println("❌ BACKEND: Excepción: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Validar shortId", description = "Verifica si un shortId es válido para restablecer contraseña")
    @GetMapping("/validate-shortId")
    public ResponseEntity<PasswordResetResponse> validateShortId(@Parameter(description = "ShortId a validar") @RequestParam String shortId) {
        System.out.println("🔍 Validando shortId via AJAX: " + shortId);
        
        try {
            boolean isValid = passwordResetService.validateShortId(shortId);
            
            if (isValid) {
                PasswordResetResponse response = new PasswordResetResponse(
                    "Token válido", 
                    true
                );
                return ResponseEntity.ok(response);
            } else {
                PasswordResetResponse response = new PasswordResetResponse(
                    "Token inválido o expirado", 
                    false
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            PasswordResetResponse response = new PasswordResetResponse(
                "Error validando token: " + e.getMessage(), 
                false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Operation(summary = "Verificar token JWT", description = "Verifica si un token JWT es válido")
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("🔍 Verificando token...");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ No se encontró token en el header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valido", false, "message", "Token no proporcionado"));
        }
        
        String token = authHeader.substring(7);
        System.out.println("🔑 Token recibido");
        
        try {
            String username = jwtUtil.extractUsername(token);
            System.out.println("👤 Username del token: " + username);
            
            if (username != null && jwtUtil.validateToken(token, username)) {
                System.out.println("✅ Token válido para usuario: " + username);
                return ResponseEntity.ok(Map.of(
                    "valido", true,
                    "username", username,
                    "message", "Token válido"
                ));
            } else {
                System.out.println("❌ Token inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valido", false, "message", "Token inválido"));
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error validando token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valido", false, "message", "Error validando token: " + e.getMessage()));
        }
    }
    
    // ✅ Método auxiliar para obtener IP del cliente
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        if (ip == null || ip.isEmpty()) {
            ip = "desconocido";
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}
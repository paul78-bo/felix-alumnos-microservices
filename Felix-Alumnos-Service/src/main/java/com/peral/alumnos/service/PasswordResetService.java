package com.peral.alumnos.service;

import com.peral.alumnos.dto.ForgotPasswordRequest;
import com.peral.alumnos.dto.ResetPasswordRequest;
import com.peral.alumnos.exception.PasswordValidationException;
import com.peral.alumnos.exception.TokenExpiradoException;
import com.peral.alumnos.exception.TokenInvalidoException;
import com.peral.alumnos.exception.UsuarioNoEncontradoException;
import com.peral.alumnos.model.PasswordResetToken;
import com.peral.alumnos.model.Usuario;
import com.peral.alumnos.repository.PasswordResetTokenRepository;
import com.peral.alumnos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PasswordResetService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.reset-token-expiry-hours:24}")
    private int tokenExpiryHours;
    
    @Value("${app.frontend-url:http://localhost:8082}")
    private String frontendUrl;
    
    /**
     * Valida que la contraseña cumpla con los requisitos de seguridad
     */
    public void validatePassword(String password) {
        if (password == null) {
            throw new PasswordValidationException("La contraseña no puede ser nula");
        }
        
        // Longitud mínima
        if (password.length() < 8) {
            throw new PasswordValidationException("La contraseña debe tener al menos 8 caracteres");
        }
        
        // Al menos una mayúscula
        if (!password.matches(".*[A-Z].*")) {
            throw new PasswordValidationException("Debe contener al menos una letra mayúscula (A-Z)");
        }
        
        // Al menos un número
        if (!password.matches(".*\\d.*")) {
            throw new PasswordValidationException("Debe contener al menos un número (0-9)");
        }
        
        // Definir símbolos permitidos
        String simbolosPermitidos = "!@#$%^&*()_+-=[]{};':\"|,.<>/?";
        
        // Contar símbolos
        long countSimbolos = password.chars()
            .filter(ch -> simbolosPermitidos.indexOf(ch) >= 0)
            .count();
        
        if (countSimbolos < 2) {
            throw new PasswordValidationException(
                String.format("Debe contener al menos 2 símbolos especiales. Símbolos permitidos: %s", 
                simbolosPermitidos)
            );
        }
        
        // Validar caracteres no permitidos
        String caracteresInvalidos = password.replaceAll(
            "[A-Za-z0-9" + Pattern.quote(simbolosPermitidos) + "]", 
            ""
        );
        
        if (!caracteresInvalidos.isEmpty()) {
            throw new PasswordValidationException(
                "Contiene caracteres no permitidos: " + caracteresInvalidos
            );
        }
    }
    
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con email: " + email));
        
        tokenRepository.deleteByUsuario(usuario);
        
        PasswordResetToken resetToken = new PasswordResetToken(usuario, LocalDateTime.now().plusHours(tokenExpiryHours));
        tokenRepository.save(resetToken);
        
        // ✅ DEBUG: Ver qué valor tiene frontendUrl AQUÍ
        System.out.println("========== DEBUG PASSWORD RESET SERVICE ==========");
        System.out.println("📧 Email: " + email);
        System.out.println("🔑 shortId: " + resetToken.getShortId());
        System.out.println("🌐 frontendUrl desde properties: " + frontendUrl);
        
        String resetLink = frontendUrl + "/reset-password.html?shortId=" + resetToken.getShortId();

        
        
        System.out.println("🔗 Enlace ANTES de enviar: " + resetLink);

        System.out.println("==================================================");
        
        emailService.sendPasswordResetEmail(usuario.getEmail(), resetToken.getShortId()); // ← PASAS SOLO shortId
    }
    
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String shortId = request.getShortId();  // ✅ Cambiado de token a shortId
        String newPassword = request.getNewPassword();
        
        // Validar la nueva contraseña
        validatePassword(newPassword);
        
        // Buscar por shortId
        PasswordResetToken resetToken = tokenRepository.findByShortId(shortId)
                .orElseThrow(() -> new TokenInvalidoException("Token inválido"));
        
        // Validar token
        if (resetToken.isUsed()) {
            throw new TokenInvalidoException("Token ya ha sido utilizado");
        }
        
        if (resetToken.isExpired()) {
            throw new TokenExpiradoException("Token ha expirado");
        }
        
        // Actualizar contraseña
        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        
        // Marcar token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        // Eliminar tokens antiguos del usuario
        tokenRepository.deleteByUsuario(usuario);
    }
    
    public boolean validateShortId(String shortId) {
        System.out.println("🔍 Validando shortId: " + shortId);
        
        try {
            PasswordResetToken resetToken = tokenRepository.findByShortId(shortId)
                    .orElse(null);
            
            if (resetToken == null) {
                System.out.println("❌ Token no encontrado");
                return false;
            }
            
            if (resetToken.isUsed()) {
                System.out.println("❌ Token ya usado");
                return false;
            }
            
            if (resetToken.isExpired()) {
                System.out.println("❌ Token expirado");
                // ✅ ELIMINAR SOLO SI ESTÁ EXPIRADO
                tokenRepository.delete(resetToken);
                return false;
            }
            
            System.out.println("✅ Token válido");
            return true;
            
        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
            return false;
        }
    }
   
}
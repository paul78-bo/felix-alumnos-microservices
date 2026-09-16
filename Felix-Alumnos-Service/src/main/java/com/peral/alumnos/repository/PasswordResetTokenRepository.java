package com.peral.alumnos.repository;

import com.peral.alumnos.model.PasswordResetToken;
import com.peral.alumnos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUsuario(Usuario usuario);
    void deleteByUsuario(Usuario usuario);
    void deleteByExpiryDateBefore(java.time.LocalDateTime date);
    Optional<PasswordResetToken> findByShortId(String shortId);  // ✅ NUEVO

}
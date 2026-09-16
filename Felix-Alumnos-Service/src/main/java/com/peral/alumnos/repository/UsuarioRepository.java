package com.peral.alumnos.repository;

import com.peral.alumnos.model.Usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Boolean existsByUsername(String username);
    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByRole(String role);
    List<Usuario> findByEmailContainingIgnoreCase(String email);
    List<Usuario> findByUsernameContainingIgnoreCase(String username);
    
    // ✅ PAGINACIÓN
    @Query("SELECT u FROM Usuario u WHERE u.enabled = true")
    Page<Usuario> findAllByEnabledTrue(Pageable pageable);
    
    @Query("SELECT u FROM Usuario u WHERE u.enabled = true AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Usuario> buscarPaginado(@Param("busqueda") String busqueda, Pageable pageable);

    
}
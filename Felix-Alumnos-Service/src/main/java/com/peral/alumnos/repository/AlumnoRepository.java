package com.peral.alumnos.repository;

import com.peral.alumnos.model.Alumno;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    
    // ==============================================
    // MÉTODOS EXISTENTES
    // ==============================================
    List<Alumno> findByCarrera(String carrera);
    Optional<Alumno> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Alumno> findAllByOrderByEdadAsc();
    List<Alumno> findAllByOrderByEdadDesc();
    
    @Query("SELECT a FROM Alumno a WHERE a.activo = true")
    List<Alumno> findAllActivos();
    
    @Query("SELECT a FROM Alumno a WHERE a.id = :id AND a.activo = true")
    Optional<Alumno> findByIdActivo(@Param("id") Long id);
    
    @Query("SELECT a FROM Alumno a WHERE a.activo = true ORDER BY a.edad ASC")
    List<Alumno> findAllActivosOrderByEdadAsc();
    
    @Query("SELECT a FROM Alumno a WHERE a.activo = true ORDER BY a.edad DESC")
    List<Alumno> findAllActivosOrderByEdadDesc();
    
    Optional<Alumno> findByUsuarioId(Long usuarioId);
    List<Alumno> findAllByUsuarioId(Long usuarioId);
    
    // ==============================================
    // ✅ PAGINACIÓN - AGREGAR ESTOS
    // ==============================================
    
    @Query("SELECT a FROM Alumno a WHERE a.activo = true")
    Page<Alumno> findAllActivosPaginado(Pageable pageable);
    
    @Query("SELECT a FROM Alumno a WHERE a.activo = true AND " +
           "(LOWER(a.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(a.carrera) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Alumno> buscarPaginado(@Param("busqueda") String busqueda, Pageable pageable);
}
package com.peral.alumnos.repository;

import com.peral.alumnos.model.Tarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    
    // Buscar tareas por alumno
    List<Tarea> findByAlumnoId(Long alumnoId);
    
    // Buscar tareas por alumno ordenadas por fecha
    List<Tarea> findByAlumnoIdOrderByFechaSubidaDesc(Long alumnoId);
    
    // Buscar tareas por usuario (a través del alumno)
    List<Tarea> findByAlumnoUsuarioId(Long usuarioId);
    
    // Buscar tareas por título (para búsquedas)
    List<Tarea> findByTituloContainingIgnoreCase(String titulo);
    
    // ✅ PAGINACIÓN - CORREGIDO
    Page<Tarea> findByAlumnoId(Long alumnoId, Pageable pageable);
    
    @Query("SELECT t FROM Tarea t WHERE " +
           "(:titulo IS NULL OR LOWER(t.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
           "(:alumno IS NULL OR LOWER(t.alumno.nombre) LIKE LOWER(CONCAT('%', :alumno, '%'))) AND " +
           "(:estado IS NULL OR t.estado = :estado)")
    Page<Tarea> buscarTareasPaginado(@Param("titulo") String titulo,
                                      @Param("alumno") String alumno,
                                      @Param("estado") String estado,
                                      Pageable pageable);
    
    // Promedios
    @Query(value = "SELECT a.id, a.nombre, " +
           "ROUND(AVG(t.calificacion), 1) as promedio, " +
           "COUNT(t.id) as total " +
           "FROM alumnos a " +
           "LEFT JOIN tareas t ON t.alumno_id = a.id AND t.calificacion IS NOT NULL " +
           "GROUP BY a.id, a.nombre " +
           "HAVING COUNT(t.id) > 0 " +
           "ORDER BY promedio DESC", 
           nativeQuery = true)
    List<Object[]> findPromedioPorAlumno();

    List<Tarea> findByAlumnoIdAndCalificacionIsNotNull(Long alumnoId);
    long countByAlumnoId(Long alumnoId);
}
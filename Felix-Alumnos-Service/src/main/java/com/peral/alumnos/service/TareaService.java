package com.peral.alumnos.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.peral.alumnos.dto.TareaRequestDTO;
import com.peral.alumnos.dto.TareaResponseDTO;
import com.peral.alumnos.model.Tarea;

public interface TareaService {
    
    // Crear nueva tarea con archivo PDF
    TareaResponseDTO crearTarea(TareaRequestDTO request) throws IOException;
    
    // Obtener tarea por ID
    TareaResponseDTO obtenerTareaPorId(Long id);
    
    // Listar todas las tareas de un alumno
    List<TareaResponseDTO> listarTareasPorAlumno(Long alumnoId);
    
    // Listar todas las tareas de un usuario (admin/profesor)
    List<TareaResponseDTO> listarTareasPorUsuario(Long usuarioId);
    
    // Descargar archivo PDF
    byte[] descargarArchivo(Long id) throws IOException;
    
    // Calificar tarea
    TareaResponseDTO calificarTarea(Long id, Double calificacion, String comentarios);
    
    // Eliminar tarea
    void eliminarTarea(Long id);
    
    // Buscar tareas por título
    List<TareaResponseDTO> buscarPorTitulo(String titulo);
    
    List<TareaResponseDTO> listarTodas();
    
    // Descargar archivo de entrega del alumno
    byte[] descargarArchivoEntrega(Long id) throws IOException;
    
    String generarLinkAccesoEntrega(Long id, Long profesorId);
    
    byte[] obtenerEntregaPorShortId(String shortId);
    
    TareaResponseDTO entregarTarea(Long id, TareaRequestDTO request) throws IOException;
    
    List<Map<String, Object>> getPromedioPorAlumno();
    
    Map<String, Object> getMiPromedio(Long usuarioId);
    
    // ✅ NUEVOS MÉTODOS DE PAGINACIÓN
    Page<Tarea> listarTareasPaginado(Pageable pageable, String titulo, String alumno, String estado);
    Page<Tarea> listarMisTareasPaginado(Long usuarioId, Pageable pageable);
    
    TareaResponseDTO convertirADTO(Tarea tarea);
}
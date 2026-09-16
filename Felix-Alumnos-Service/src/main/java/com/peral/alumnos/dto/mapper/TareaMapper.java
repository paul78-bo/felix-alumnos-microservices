package com.peral.alumnos.dto.mapper;

import com.peral.alumnos.dto.TareaRequestDTO;
import com.peral.alumnos.dto.TareaResponseDTO;
import com.peral.alumnos.model.Alumno;
import com.peral.alumnos.model.Tarea;
import org.springframework.stereotype.Component;

@Component
public class TareaMapper {

    // ==============================================
    // CONVERTIR REQUEST A ENTIDAD (para crear)
    // ==============================================
    
    public Tarea toEntity(TareaRequestDTO request, Alumno alumno, String rutaArchivo) {
        if (request == null) return null;
        
        Tarea tarea = new Tarea();
        tarea.setTitulo(request.getTitulo());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setNombreArchivo(request.getArchivo().getOriginalFilename());
        tarea.setTipoArchivo(request.getArchivo().getContentType());
        tarea.setTamanioArchivo(request.getArchivo().getSize());
        tarea.setRutaArchivo(rutaArchivo);
        tarea.setAlumno(alumno);
        // fechaSubida se setea automáticamente en la entidad
        
        return tarea;
    }

    // ==============================================
    // ACTUALIZAR ENTIDAD DESDE REQUEST (para calificar)
    // ==============================================
    
    public void updateEntityFromCalificacion(Tarea tarea, Double calificacion, String comentarios) {
        if (calificacion != null) {
            tarea.setCalificacion(calificacion);
        }
        if (comentarios != null) {
            tarea.setComentarios(comentarios);
        }
    }

    // ==============================================
    // CONVERTIR ENTIDAD A DTO BÁSICO
    // ==============================================
    
    public TareaResponseDTO toDTO(Tarea tarea) {
        if (tarea == null) return null;
        
        TareaResponseDTO dto = new TareaResponseDTO();
        dto.setId(tarea.getId());
        dto.setTitulo(tarea.getTitulo());
        dto.setDescripcion(tarea.getDescripcion());
        dto.setNombreArchivo(tarea.getNombreArchivo());
        dto.setTipoArchivo(tarea.getTipoArchivo());
        dto.setTamanioArchivo(tarea.getTamanioArchivo());
        dto.setFechaSubida(tarea.getFechaSubida());
        dto.setCalificacion(tarea.getCalificacion());
        dto.setComentarios(tarea.getComentarios());
        
        // ✅ Mapear nuevos campos
        dto.setTipoTarea(tarea.getTipoTarea());
        dto.setEstado(tarea.getEstado());
        dto.setFechaEntrega(tarea.getFechaEntrega());
        dto.setArchivoEntrega(tarea.getArchivoEntrega());

        
        // Datos del alumno
        if (tarea.getAlumno() != null) {
            dto.setAlumnoId(tarea.getAlumno().getId());
            dto.setAlumnoNombre(tarea.getAlumno().getNombre());
            dto.setAlumnoEmail(tarea.getAlumno().getEmail());
        }
        
        return dto;
    }

    // ==============================================
    // CONVERTIR ENTIDAD A DTO CON DETALLES (para respuestas completas)
    // ==============================================
    
    public TareaResponseDTO toDetailedDTO(Tarea tarea) {
        // Por ahora es el mismo que toDTO, pero podrías agregar más campos si es necesario
        return toDTO(tarea);
    }
}
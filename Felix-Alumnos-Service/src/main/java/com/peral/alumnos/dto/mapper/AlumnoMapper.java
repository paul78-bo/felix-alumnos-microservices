package com.peral.alumnos.dto.mapper;

import com.peral.alumnos.dto.AlumnoRequestDTO;
import com.peral.alumnos.dto.AlumnoResponseDTO;
import com.peral.alumnos.model.Alumno;
import com.peral.alumnos.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Collections;    // ← AGREGAR
import java.util.List;           // ← AGREGAR
import java.util.stream.Collectors;  // ← AGREGAR

@Component
public class AlumnoMapper {

    // Convertir Request a Entidad
    public Alumno toEntity(AlumnoRequestDTO request, Usuario usuario) {
        if (request == null) return null;
        
        Alumno alumno = new Alumno();
        alumno.setNombre(request.getNombre());
        alumno.setEmail(request.getEmail());
        alumno.setEdad(request.getEdad());
        alumno.setCarrera(request.getCarrera());
        alumno.setUsuario(usuario);
        
       
     // ✅ ASIGNAR VALOR POR DEFECTO SI ES NULL
        alumno.setActivo(request.getActivo() != null ? request.getActivo() : true);
        
        return alumno;
    }

    // Actualizar Entidad desde Request
    public void updateEntity(AlumnoRequestDTO request, Alumno alumno) {
        if (request == null) return;
        
        if (request.getNombre() != null) {
            alumno.setNombre(request.getNombre());
        }
        if (request.getEmail() != null) {
            alumno.setEmail(request.getEmail());
        }
        if (request.getEdad() != null) {
            alumno.setEdad(request.getEdad());
        }
        if (request.getCarrera() != null) {
            alumno.setCarrera(request.getCarrera());
        }
        if (request.getActivo() != null) {
            alumno.setActivo(request.getActivo());
        }
        
        
    }

    // Convertir Entidad a ResponseDTO
    public AlumnoResponseDTO toResponseDTO(Alumno alumno) {
        if (alumno == null) return null;
        
        AlumnoResponseDTO dto = new AlumnoResponseDTO();
        dto.setId(alumno.getId());
        dto.setNombre(alumno.getNombre());
        dto.setEmail(alumno.getEmail());
        dto.setEdad(alumno.getEdad());
        dto.setCarrera(alumno.getCarrera());
        
        dto.setActivo(alumno.getActivo());
        
        if (alumno.getUsuario() != null) {
            dto.setUsuarioId(alumno.getUsuario().getId());
            dto.setUsuarioUsername(alumno.getUsuario().getUsername());
        }
        
        return dto;
    }

    // Convertir lista de Entidades a lista de ResponseDTOs
    public List<AlumnoResponseDTO> toResponseDTOList(List<Alumno> alumnos) {
        if (alumnos == null) return Collections.emptyList();
        
        return alumnos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
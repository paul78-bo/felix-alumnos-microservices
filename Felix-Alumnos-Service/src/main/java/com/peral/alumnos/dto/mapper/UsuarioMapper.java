package com.peral.alumnos.dto.mapper;

import com.peral.alumnos.dto.AlumnoResponseDTO;
import com.peral.alumnos.dto.CreateUsuarioRequest;
import com.peral.alumnos.dto.UpdateUsuarioRequest;
import com.peral.alumnos.dto.UsuarioDTO;
import com.peral.alumnos.dto.UsuarioResponseDTO;
import com.peral.alumnos.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
	
	
	
	// Método para convertir Request a Entity (útil para create/update)
	public Usuario toEntity(CreateUsuarioRequest request) {
	    if (request == null) return null;
	    Usuario usuario = new Usuario();
	    usuario.setUsername(request.getUsername());
	    usuario.setEmail(request.getEmail());
	    usuario.setRole(request.getRole());
	    // password se setea aparte (encriptada)
	    return usuario;
	}
	// Para update
	public void updateEntity(UpdateUsuarioRequest request, Usuario usuario) {
	    if (request.getUsername() != null) usuario.setUsername(request.getUsername());
	    if (request.getEmail() != null) usuario.setEmail(request.getEmail());
	    if (request.getRole() != null) usuario.setRole(request.getRole());
	    if (request.getEnabled() != null) usuario.setEnabled(request.getEnabled());
	    // password se maneja aparte
	}
    
    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setRole(usuario.getRole());
        dto.setEnabled(usuario.getEnabled());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        
        // Si tiene alumno asociado
        if (usuario.getAlumno() != null) {
            dto.setNombreAlumno(usuario.getAlumno().getNombre());
            dto.setAlumnoId(usuario.getAlumno().getId());
        }
        
        return dto;
    }
    
    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        if (usuario == null) return null;
        
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setRole(usuario.getRole());
        dto.setEnabled(usuario.getEnabled());
        
        // ✅ Incluir alumno
        if (usuario.getAlumno() != null) {
            AlumnoResponseDTO alumnoDTO = new AlumnoResponseDTO(
                usuario.getAlumno().getId(),
                usuario.getAlumno().getNombre(),
                usuario.getAlumno().getEmail(),
                usuario.getAlumno().getEdad(),
                usuario.getAlumno().getCarrera()
            );
            dto.setAlumno(alumnoDTO);
        }
        
        return dto;
    }
}
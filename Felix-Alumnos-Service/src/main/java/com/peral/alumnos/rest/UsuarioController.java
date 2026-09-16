package com.peral.alumnos.rest;

import com.peral.alumnos.dto.CreateUsuarioRequest;
import com.peral.alumnos.dto.UpdateUsuarioRequest;
import com.peral.alumnos.dto.UsuarioDTO;
import com.peral.alumnos.dto.UsuarioResponseDTO;
import com.peral.alumnos.model.Usuario;
import com.peral.alumnos.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Obtener todos los usuarios", description = "Solo para administradores")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuarios obtenidos exitosamente"),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @Operation(summary = "Obtener usuario por ID", description = "Solo para administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getById(@Parameter(description = "ID del usuario") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @Operation(summary = "Crear nuevo usuario", description = "Solo para administradores")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Username o email ya existe")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> create(@Valid @RequestBody CreateUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(request));
    }

    @Operation(summary = "Actualizar usuario", description = "Solo para administradores")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> update(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.update(id, request));
    }

    @Operation(summary = "Desactivar usuario", description = "Soft delete - solo para administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID del usuario") @PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cambiar rol de usuario", description = "Solo para administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String nuevoRol = request.get("rol");
        return ResponseEntity.ok(usuarioService.cambiarRol(id, nuevoRol));
    }

    @Operation(summary = "Activar/desactivar usuario", description = "Solo para administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        Boolean activo = request.get("activo");
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, activo));
    }

    @Operation(summary = "Buscar usuarios por rol", description = "Solo para administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<UsuarioDTO>> getByRol(@Parameter(description = "ROLE_ADMIN, ROLE_PROFESOR, ROLE_ALUMNO") @PathVariable String rol) {
        return ResponseEntity.ok(usuarioService.findByRol(rol));
    }

    @Operation(summary = "Buscar usuarios por email", description = "Solo para administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/buscar/email")
    public ResponseEntity<List<UsuarioDTO>> buscarPorEmail(@Parameter(description = "Email (parcial)") @RequestParam String email) {
        return ResponseEntity.ok(usuarioService.findByEmailContaining(email));
    }

    @Operation(summary = "Buscar usuarios por username", description = "Solo para administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/buscar/username")
    public ResponseEntity<List<UsuarioDTO>> buscarPorUsername(@Parameter(description = "Username (parcial)") @RequestParam String username) {
        return ResponseEntity.ok(usuarioService.findByUsernameContaining(username));
    }

    @Operation(summary = "Obtener mi perfil", description = "Obtiene el perfil del usuario autenticado")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getMiPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UsuarioResponseDTO perfil = usuarioService.getMiPerfil(username);
        return ResponseEntity.ok(perfil);
    }

    @Operation(summary = "Verificar si username existe", description = "Público - útil para validaciones en registro")
    @GetMapping("/existe/username/{username}")
    public ResponseEntity<Map<String, Boolean>> existeUsername(@PathVariable String username) {
        boolean existe = usuarioService.existeUsername(username);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    @Operation(summary = "Verificar si email existe", description = "Público - útil para validaciones en registro")
    @GetMapping("/existe/email/{email}")
    public ResponseEntity<Map<String, Boolean>> existeEmail(@PathVariable String email) {
        boolean existe = usuarioService.existeEmail(email);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    @Operation(summary = "Actualizar mi perfil", description = "Usuario actualiza sus propios datos")
    @PutMapping("/me")
    public ResponseEntity<?> updateMiPerfil(@RequestBody Map<String, Object> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            UsuarioDTO usuario = usuarioService.findByUsername(username);
            UpdateUsuarioRequest updateRequest = new UpdateUsuarioRequest();
            updateRequest.setUsername(usuario.getUsername());
            updateRequest.setEmail((String) request.get("email"));
            updateRequest.setRole(usuario.getRole());
            
            if (request.containsKey("password") && request.get("password") != null) {
                updateRequest.setPassword((String) request.get("password"));
            }
            
            if (request.containsKey("nombreAlumno")) {
                updateRequest.setNombreAlumno((String) request.get("nombreAlumno"));
            }
            if (request.containsKey("carreraAlumno")) {
                updateRequest.setCarreraAlumno((String) request.get("carreraAlumno"));
            }
            if (request.containsKey("edadAlumno")) {
                updateRequest.setEdadAlumno(((Number) request.get("edadAlumno")).intValue());
            }
            
            UsuarioResponseDTO perfilActualizado = usuarioService.update(usuario.getId(), updateRequest);
            return ResponseEntity.ok(perfilActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ ENDPOINT DE PAGINACIÓN
    @Operation(summary = "Listar usuarios paginados", description = "Solo para administradores")
    @GetMapping("/paginado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listarUsuariosPaginado(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String busqueda) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Usuario> pageUsuarios;
        
        if (busqueda != null && !busqueda.isEmpty()) {
            pageUsuarios = usuarioService.buscarPaginado(busqueda, pageable);
        } else {
            pageUsuarios = usuarioService.listarPaginado(pageable);
        }
        
        List<UsuarioDTO> contentDTO = pageUsuarios.getContent()
                .stream()
                .map(usuario -> {
                    UsuarioDTO dto = new UsuarioDTO();
                    dto.setId(usuario.getId());
                    dto.setUsername(usuario.getUsername());
                    dto.setEmail(usuario.getEmail());
                    dto.setRole(usuario.getRole());
                    dto.setEnabled(usuario.getEnabled());
                    if (usuario.getAlumno() != null) {
                        dto.setAlumnoId(usuario.getAlumno().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", contentDTO);
        response.put("totalElements", pageUsuarios.getTotalElements());
        response.put("totalPages", pageUsuarios.getTotalPages());
        response.put("currentPage", pageUsuarios.getNumber());
        response.put("size", pageUsuarios.getSize());
        
        return ResponseEntity.ok(response);
    }
}
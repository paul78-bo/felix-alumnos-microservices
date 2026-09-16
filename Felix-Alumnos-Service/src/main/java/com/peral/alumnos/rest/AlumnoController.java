package com.peral.alumnos.rest;

import com.peral.alumnos.dto.AlumnoRequestDTO;
import com.peral.alumnos.dto.AlumnoResponseDTO;
import com.peral.alumnos.dto.mapper.AlumnoMapper;
import com.peral.alumnos.model.Alumno;
import com.peral.alumnos.service.AlumnoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alumnos")
@Tag(name = "Alumnos", description = "Endpoints para gestión de alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;
    private final AlumnoMapper alumnoMapper;

    public AlumnoController(AlumnoService alumnoService, AlumnoMapper alumnoMapper) {
        this.alumnoService = alumnoService;
        this.alumnoMapper = alumnoMapper;
    }

    // ==============================================
    // MANEJADOR DE ERRORES DE VALIDACIÓN
    // ==============================================
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);
        response.put("message", "Error de validación en los datos enviados");
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ==============================================
    // ENDPOINTS
    // ==============================================

    @Operation(summary = "Listar todos los alumnos", description = "Obtiene la lista completa de alumnos activos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping
    public ResponseEntity<List<AlumnoResponseDTO>> getAll() {
        return ResponseEntity.ok(alumnoService.findAll());
    }

    @Operation(summary = "Obtener alumno por ID", description = "Busca un alumno por su identificador único")
    @GetMapping("/{id}")
    public ResponseEntity<AlumnoResponseDTO> getById(
            @Parameter(description = "ID del alumno") @PathVariable Long id) {
        AlumnoResponseDTO alumno = alumnoService.findById(id);
        return ResponseEntity.ok(alumno);
    }

    @Operation(summary = "Crear nuevo alumno", description = "Registra un nuevo alumno en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Alumno creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    @PostMapping
    public ResponseEntity<AlumnoResponseDTO> create(@Valid @RequestBody AlumnoRequestDTO dto) {
        AlumnoResponseDTO nuevoAlumno = alumnoService.create(dto);
        return new ResponseEntity<>(nuevoAlumno, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar alumno", description = "Modifica los datos de un alumno existente")
    @PutMapping("/{id}")
    public ResponseEntity<AlumnoResponseDTO> update(
            @Parameter(description = "ID del alumno a actualizar") @PathVariable Long id,
            @Valid @RequestBody AlumnoRequestDTO dto) {
        AlumnoResponseDTO alumnoActualizado = alumnoService.update(id, dto);
        return ResponseEntity.ok(alumnoActualizado);
    }

    @Operation(summary = "Eliminar alumno", description = "Elimina (soft delete) un alumno del sistema")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID del alumno a eliminar") @PathVariable Long id) {
        alumnoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar alumnos ordenados por edad (ascendente)", description = "Alumnos ordenados de menor a mayor edad")
    @GetMapping("/orden/asc")
    public ResponseEntity<List<AlumnoResponseDTO>> findAllOrderByEdadAsc() {
        return ResponseEntity.ok(alumnoService.findAllOrderByEdadAsc());
    }

    @Operation(summary = "Listar alumnos ordenados por edad (descendente)", description = "Alumnos ordenados de mayor a menor edad")
    @GetMapping("/orden/desc")
    public ResponseEntity<List<AlumnoResponseDTO>> findAllOrderByEdadDesc() {
        return ResponseEntity.ok(alumnoService.findAllOrderByEdadDesc());
    }
   
    @Operation(summary = "Buscar alumnos por ID de usuario", description = "Obtiene los alumnos asociados a un usuario")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<AlumnoResponseDTO>> getByUsuarioId(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        System.out.println("📡 GET /api/alumnos/usuario/" + usuarioId);
        List<AlumnoResponseDTO> alumnos = alumnoService.findByUsuarioId(usuarioId);
        return ResponseEntity.ok(alumnos);
    }
    
    @Operation(summary = "Listar alumnos paginados", description = "Obtiene alumnos con paginación y búsqueda opcional")
    @GetMapping("/paginado")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'ALUMNO')")
    public ResponseEntity<Map<String, Object>> listarAlumnosPaginado(
            @Parameter(description = "Número de página (0 = primera)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Término de búsqueda (nombre, email, carrera)") @RequestParam(required = false) String busqueda) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Alumno> pageAlumnos;
        
        if (busqueda != null && !busqueda.isEmpty()) {
            pageAlumnos = alumnoService.buscarPaginado(busqueda, pageable);
        } else {
            pageAlumnos = alumnoService.listarPaginado(pageable);
        }
        
        List<AlumnoResponseDTO> contentDTO = pageAlumnos.getContent()
                .stream()
                .map(alumnoMapper::toResponseDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", contentDTO);
        response.put("totalElements", pageAlumnos.getTotalElements());
        response.put("totalPages", pageAlumnos.getTotalPages());
        response.put("currentPage", pageAlumnos.getNumber());
        response.put("size", pageAlumnos.getSize());
        
        return ResponseEntity.ok(response);
    }
}
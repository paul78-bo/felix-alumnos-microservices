package com.peral.alumnos.rest;

import com.peral.alumnos.dto.TareaRequestDTO;
import com.peral.alumnos.dto.TareaResponseDTO;
import com.peral.alumnos.model.Tarea;
import com.peral.alumnos.service.TareaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tareas")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tareas", description = "Endpoints para gestión de tareas, entregas y calificaciones")
public class TareaController {

	private final TareaService tareaService;

	public TareaController(TareaService tareaService) {
		this.tareaService = tareaService;
	}

	// ==============================================
	// 1. SUBIR NUEVA TAREA (PDF)
	// ==============================================

	@Operation(summary = "Subir nueva tarea", description = "Sube una nueva tarea (alumno entrega, profesor asigna)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
		@ApiResponse(responseCode = "400", description = "Datos inválidos o archivo no válido"),
		@ApiResponse(responseCode = "401", description = "No autenticado")
	})
	@PreAuthorize("hasAnyRole('ALUMNO', 'PROFESOR', 'ADMIN')")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<TareaResponseDTO> crearTarea(@Valid @ModelAttribute TareaRequestDTO request)
			throws IOException {
		TareaResponseDTO nuevaTarea = tareaService.crearTarea(request);
		return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
	}

	// ==============================================
	// 2. OBTENER TAREA POR ID
	// ==============================================

	@Operation(summary = "Obtener tarea por ID", description = "Busca una tarea por su identificador")
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ALUMNO', 'PROFESOR', 'ADMIN')")
	public ResponseEntity<TareaResponseDTO> obtenerTarea(@Parameter(description = "ID de la tarea") @PathVariable Long id) {
		TareaResponseDTO tarea = tareaService.obtenerTareaPorId(id);
		return ResponseEntity.ok(tarea);
	}

	// ==============================================
	// 3. LISTAR TAREAS DE UN ALUMNO
	// ==============================================

	@Operation(summary = "Listar tareas de un alumno", description = "Obtiene todas las tareas de un alumno específico")
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMIN')")
	@GetMapping("/alumno/{alumnoId}")
	public ResponseEntity<List<TareaResponseDTO>> listarPorAlumno(@Parameter(description = "ID del alumno") @PathVariable Long alumnoId) {
		List<TareaResponseDTO> tareas = tareaService.listarTareasPorAlumno(alumnoId);
		return ResponseEntity.ok(tareas);
	}

	// ==============================================
	// 4. LISTAR MIS TAREAS (ALUMNO)
	// ==============================================

	@Operation(summary = "Listar mis tareas", description = "Alumno obtiene solo sus tareas")
	@GetMapping("/mis-tareas")
	public ResponseEntity<List<TareaResponseDTO>> listarMisTareas(@RequestAttribute("userId") Long usuarioId) {
	    System.out.println("========== /mis-tareas ==========");
	    System.out.println("Usuario ID recibido: " + usuarioId);
	    
	    try {
	        List<TareaResponseDTO> tareas = tareaService.listarTareasPorUsuario(usuarioId);
	        System.out.println("Tareas encontradas: " + tareas.size());
	        return ResponseEntity.ok(tareas);
	    } catch (Exception e) {
	        System.out.println("❌ ERROR en /mis-tareas: " + e.getMessage());
	        e.printStackTrace();
	        throw e;
	    }
	}
	
	// ==============================================
	// 4.1 PAGINACIÓN DE TAREAS
	// ==============================================

	@Operation(summary = "Listar tareas paginadas", description = "Obtiene tareas con paginación, filtros y ordenamiento")
	@GetMapping("/paginado")
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMIN', 'ALUMNO')")
	public ResponseEntity<Map<String, Object>> listarTareasPaginado(
	        @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
	        @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
	        @Parameter(description = "Filtrar por título") @RequestParam(required = false) String titulo,
	        @Parameter(description = "Filtrar por nombre de alumno") @RequestParam(required = false) String alumno,
	        @Parameter(description = "Filtrar por estado") @RequestParam(required = false) String estado,
	        @Parameter(description = "Ordenar por fecha (reciente/antiguo)") @RequestParam(defaultValue = "reciente") String ordenFecha,
	        @RequestAttribute(name = "userId", required = false) Long usuarioId) {
	    
	    Sort sort = "reciente".equals(ordenFecha) 
	        ? Sort.by("fechaSubida").descending() 
	        : Sort.by("fechaSubida").ascending();
	    
	    Pageable pageable = PageRequest.of(page, size, sort);
	    Page<Tarea> pageTareas;
	    
	    String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
	    
	    if ("ROLE_ALUMNO".equals(role)) {
	        pageTareas = tareaService.listarMisTareasPaginado(usuarioId, pageable);
	    } else {
	        pageTareas = tareaService.listarTareasPaginado(pageable, titulo, alumno, estado);
	    }
	    
	    List<TareaResponseDTO> contentDTO = pageTareas.getContent()
	            .stream()
	            .map(tareaService::convertirADTO)
	            .collect(Collectors.toList());
	    
	    Map<String, Object> response = new HashMap<>();
	    response.put("content", contentDTO);
	    response.put("totalElements", pageTareas.getTotalElements());
	    response.put("totalPages", pageTareas.getTotalPages());
	    response.put("currentPage", pageTareas.getNumber());
	    response.put("size", pageTareas.getSize());
	    
	    return ResponseEntity.ok(response);
	}
	
	// ==============================================
	// 5. DESCARGAR ARCHIVO PDF
	// ==============================================

	@Operation(summary = "Descargar enunciado de tarea", description = "Descarga el archivo PDF del enunciado")
	@GetMapping("/{id}/descargar")
	public ResponseEntity<Resource> descargarArchivo(@Parameter(description = "ID de la tarea") @PathVariable Long id) throws IOException {
	    System.out.println("========== DESCARGAR ARCHIVO ==========");
	    System.out.println("ID de tarea: " + id);
	    
	    try {
	        byte[] archivo = tareaService.descargarArchivo(id);
	        TareaResponseDTO tarea = tareaService.obtenerTareaPorId(id);
	        
	        System.out.println("Archivo obtenido, tamaño: " + archivo.length);
	        
	        ByteArrayResource resource = new ByteArrayResource(archivo);
	        
	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + tarea.getNombreArchivo() + "\"")
	                .contentType(MediaType.APPLICATION_PDF)
	                .contentLength(archivo.length)
	                .body(resource);
	                
	    } catch (Exception e) {
	        System.out.println("❌ ERROR al descargar: " + e.getMessage());
	        e.printStackTrace();
	        throw e;
	    }
	}
	
	// ==============================================
	// 5.1 DESCARGAR ENTREGA DEL ALUMNO
	// ==============================================

	@Operation(summary = "Descargar entrega del alumno", description = "Descarga el archivo PDF entregado por el alumno")
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMIN')")
	@GetMapping("/{id}/descargar-entrega")
	public ResponseEntity<Resource> descargarEntrega(@Parameter(description = "ID de la tarea") @PathVariable Long id) throws IOException {
	    System.out.println("========== DESCARGAR ENTREGA ==========");
	    System.out.println("ID de tarea: " + id);
	    
	    try {
	        byte[] archivo = tareaService.descargarArchivoEntrega(id);
	        TareaResponseDTO tarea = tareaService.obtenerTareaPorId(id);
	        
	        System.out.println("Archivo entrega obtenido, tamaño: " + archivo.length);
	        
	        ByteArrayResource resource = new ByteArrayResource(archivo);
	        
	        String nombreArchivo = "entrega_tarea_" + id + ".pdf";
	        if (tarea.getArchivoEntrega() != null) {
	            nombreArchivo = tarea.getArchivoEntrega();
	        }
	        
	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
	                .contentType(MediaType.APPLICATION_PDF)
	                .contentLength(archivo.length)
	                .body(resource);
	                
	    } catch (Exception e) {
	        System.out.println("❌ ERROR al descargar entrega: " + e.getMessage());
	        e.printStackTrace();
	        throw e;
	    }
	}

	// ==============================================
	// 6. CALIFICAR TAREA
	// ==============================================

	@Operation(summary = "Calificar tarea", description = "Asigna una calificación y comentarios a una tarea")
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMIN')")
	@PostMapping("/{id}/calificar")
	public ResponseEntity<TareaResponseDTO> calificarTarea(
			@Parameter(description = "ID de la tarea") @PathVariable Long id,
			@RequestBody Map<String, Object> request) {

		Double calificacion;
		Object califObj = request.get("calificacion");

		if (califObj instanceof Number) {
			calificacion = ((Number) califObj).doubleValue();
		} else if (califObj instanceof String) {
			calificacion = Double.parseDouble((String) califObj);
		} else {
			throw new RuntimeException("Formato de calificación inválido");
		}

		String comentarios = (String) request.get("comentarios");

		TareaResponseDTO tareaCalificada = tareaService.calificarTarea(id, calificacion, comentarios);
		return ResponseEntity.ok(tareaCalificada);
	}
	
	// ==============================================
	// 7. ELIMINAR TAREA
	// ==============================================

	@Operation(summary = "Eliminar tarea", description = "Elimina una tarea del sistema")
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarTarea(@Parameter(description = "ID de la tarea") @PathVariable Long id) {
		tareaService.eliminarTarea(id);
		return ResponseEntity.noContent().build();
	}

	// ==============================================
	// 8. BUSCAR TAREAS POR TÍTULO
	// ==============================================

	@Operation(summary = "Buscar tareas por título", description = "Busca tareas que contengan el texto en el título")
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMIN')")
	@GetMapping("/buscar")
	public ResponseEntity<List<TareaResponseDTO>> buscarPorTitulo(@Parameter(description = "Texto a buscar") @RequestParam String titulo) {
		List<TareaResponseDTO> tareas = tareaService.buscarPorTitulo(titulo);
		return ResponseEntity.ok(tareas);
	}

	// ==============================================
	// 9. LISTAR TODAS LAS TAREAS
	// ==============================================

	@Operation(summary = "Listar todas las tareas", description = "Obtiene todas las tareas (solo admin/profesor)")
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMIN')")
	@GetMapping
	public ResponseEntity<List<TareaResponseDTO>> listarTodas() {
		List<TareaResponseDTO> tareas = tareaService.listarTodas();
		return ResponseEntity.ok(tareas);
	}

	// ==============================================
	// GENERAR LINK SEGURO PARA VER ENTREGA
	// ==============================================

	@Operation(summary = "Generar link seguro para ver entrega", description = "Genera un shortId para ver la entrega sin autenticación")
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMIN')")
	@PostMapping("/{id}/generar-link-entrega")
	public ResponseEntity<Map<String, String>> generarLinkEntrega(
			@Parameter(description = "ID de la tarea") @PathVariable Long id, 
			@RequestAttribute("userId") Long profesorId) {
	    String shortId = tareaService.generarLinkAccesoEntrega(id, profesorId);
	    
	    Map<String, String> response = new HashMap<>();
	    response.put("shortId", shortId);
	    return ResponseEntity.ok(response);
	}

	// ==============================================
	// VER ENTREGA CON LINK SEGURO
	// ==============================================

	@Operation(summary = "Ver entrega con link seguro", description = "Accede al PDF de entrega usando un shortId (sin autenticación)")
	@GetMapping("/acceso-entrega/{shortId}")
	public ResponseEntity<byte[]> verEntregaConLinkSeguro(@Parameter(description = "ShortId generado") @PathVariable String shortId) throws IOException {
	    System.out.println("========== VER ENTREGA CON LINK SEGURO ==========");
	    System.out.println("ShortId: " + shortId);
	    
	    try {
	        byte[] archivo = tareaService.obtenerEntregaPorShortId(shortId);
	        
	        return ResponseEntity.ok()
	                .contentType(MediaType.APPLICATION_PDF)
	                .contentLength(archivo.length)
	                .header(HttpHeaders.CACHE_CONTROL, "no-cache, private")
	                .body(archivo);
	                
	    } catch (Exception e) {
	        System.out.println("❌ ERROR: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	    }
	}
	
	// ==============================================
	// 10. ENTREGAR TAREA EXISTENTE (ALUMNO)
	// ==============================================

	@Operation(summary = "Entregar tarea existente", description = "Alumno entrega un archivo para una tarea asignada")
	@PreAuthorize("hasAnyRole('ALUMNO')")
	@PutMapping("/{id}/entregar")
	public ResponseEntity<TareaResponseDTO> entregarTarea(
	        @Parameter(description = "ID de la tarea") @PathVariable Long id,
	        @Valid @ModelAttribute TareaRequestDTO request) throws IOException {
	    
	    System.out.println("========== ENTREGAR TAREA ==========");
	    System.out.println("ID Tarea: " + id);
	    
	    TareaResponseDTO tareaActualizada = tareaService.entregarTarea(id, request);
	    return ResponseEntity.ok(tareaActualizada);
	}
	
	// ==============================================
	// ESTADÍSTICAS
	// ==============================================

	@Operation(summary = "Promedio por alumno", description = "Obtiene el promedio de calificaciones de todos los alumnos")
	@PreAuthorize("hasAnyRole('PROFESOR', 'ADMIN')")
	@GetMapping("/estadisticas/promedio-por-alumno")
	public ResponseEntity<List<Map<String, Object>>> getPromedioPorAlumno() {
	    List<Map<String, Object>> promedios = tareaService.getPromedioPorAlumno();
	    return ResponseEntity.ok(promedios);
	}
	
	@Operation(summary = "Mi promedio", description = "Obtiene el promedio de calificaciones del alumno autenticado")
	@PreAuthorize("hasAnyRole('ALUMNO', 'PROFESOR', 'ADMIN')")
	@GetMapping("/estadisticas/mi-promedio")
	public ResponseEntity<Map<String, Object>> getMiPromedio(@RequestAttribute("userId") Long usuarioId) {
	    Map<String, Object> promedio = tareaService.getMiPromedio(usuarioId);
	    return ResponseEntity.ok(promedio);
	}
}
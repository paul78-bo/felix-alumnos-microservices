package com.peral.alumnos.service;

import com.peral.alumnos.dto.TareaRequestDTO;
import com.peral.alumnos.dto.TareaResponseDTO;
import com.peral.alumnos.dto.mapper.TareaMapper;
import com.peral.alumnos.exception.AlumnoNoEncontradoException;
import com.peral.alumnos.exception.ArchivoInvalidoException;
import com.peral.alumnos.model.AccesoEntrega;
import com.peral.alumnos.model.Alumno;
import com.peral.alumnos.model.Tarea;
import com.peral.alumnos.repository.AccesoEntregaRepository;
import com.peral.alumnos.repository.AlumnoRepository;
import com.peral.alumnos.repository.TareaRepository;
import com.peral.alumnos.util.FileStorageUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TareaServiceImpl implements TareaService {

	private final TareaRepository tareaRepository;
	private final AlumnoRepository alumnoRepository;
	private final FileStorageUtil fileStorageUtil;
	private final TareaMapper tareaMapper;
	
	@Autowired
	private AccesoEntregaRepository accesoEntregaRepository;

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final int SHORT_ID_LENGTH = 32;

	public TareaServiceImpl(TareaRepository tareaRepository, AlumnoRepository alumnoRepository,
			FileStorageUtil fileStorageUtil, TareaMapper tareaMapper) {
		this.tareaRepository = tareaRepository;
		this.alumnoRepository = alumnoRepository;
		this.fileStorageUtil = fileStorageUtil;
		this.tareaMapper = tareaMapper;
	}

	@Override
	@Transactional
	public TareaResponseDTO crearTarea(TareaRequestDTO request) throws IOException {
	    
	    System.out.println("========== CREAR TAREA ==========");
	    System.out.println("Título: " + request.getTitulo());
	    System.out.println("Alumno ID: " + request.getAlumnoId());
	    System.out.println("Archivo: " + (request.getArchivo() != null ? request.getArchivo().getOriginalFilename() : "null"));
	    System.out.println("Tipo Tarea: " + request.getTipoTarea());
	    
	    try {
	        MultipartFile archivo = request.getArchivo();
	        
	        // Validar título
	        if (request.getTitulo() == null || request.getTitulo().trim().isEmpty()) {
	            throw new RuntimeException("❌ El título no puede estar vacío");
	        }
	        
	        // Validar que sea PDF
	        if (!fileStorageUtil.esPdf(archivo)) {
	            throw new ArchivoInvalidoException("Solo se permiten archivos PDF");
	        }
	        
	        // Validar tamaño
	        if (archivo.getSize() > 10 * 1024 * 1024) {
	            throw new ArchivoInvalidoException("El archivo no puede ser mayor a 10MB");
	        }
	        
	        // Buscar alumno
	        Alumno alumno = alumnoRepository.findById(request.getAlumnoId())
	                .orElseThrow(() -> new AlumnoNoEncontradoException("Alumno no encontrado con ID: " + request.getAlumnoId()));
	        
	        System.out.println("✅ Alumno encontrado: " + alumno.getNombre());
	        
	        // Guardar archivo físico
	        String nombreArchivo = fileStorageUtil.guardarArchivo(archivo, alumno.getId());
	        System.out.println("✅ Archivo guardado: " + nombreArchivo);
	        
	        // Crear entidad
	        Tarea tarea = tareaMapper.toEntity(request, alumno, nombreArchivo);
	        
	        // Obtener rol del usuario
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        String userRole = auth.getAuthorities().stream()
	                .findFirst()
	                .map(g -> g.getAuthority())
	                .orElse("ROLE_USER");
	        
	        System.out.println("👤 Rol: " + userRole);
	        
	        // Setear tipo de tarea
	        if ("ROLE_ALUMNO".equals(userRole)) {
	            tarea.setTipoTarea("ENTREGA");
	            tarea.setArchivoEntrega(nombreArchivo);
	            tarea.setFechaEntrega(LocalDateTime.now());
	            System.out.println("📤 Tarea marcada como ENTREGA");
	        } else {
	            tarea.setTipoTarea("ENUNCIADO");
	            System.out.println("📄 Tarea marcada como ENUNCIADO");
	        }
	        
	        tarea.setEstado("PENDIENTE");
	        
	        // Guardar
	        Tarea tareaGuardada = tareaRepository.save(tarea);
	        System.out.println("✅ Tarea guardada con ID: " + tareaGuardada.getId());
	        
	        return tareaMapper.toDTO(tareaGuardada);
	        
	    } catch (Exception e) {
	        System.out.println("❌ ERROR: " + e.getMessage());
	        e.printStackTrace();
	        throw e;
	    }
	}
	// ==============================================
	// 2. OBTENER TAREA POR ID
	// ==============================================

	@Override
	public TareaResponseDTO obtenerTareaPorId(Long id) {
		Tarea tarea = tareaRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));

		return tareaMapper.toDTO(tarea);
	}

	// ==============================================
	// 3. LISTAR TAREAS POR ALUMNO
	// ==============================================

	@Override
	public List<TareaResponseDTO> listarTareasPorAlumno(Long alumnoId) {
		// Verificar que el alumno existe
		if (!alumnoRepository.existsById(alumnoId)) {
			throw new AlumnoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId);
		}

		return tareaRepository.findByAlumnoIdOrderByFechaSubidaDesc(alumnoId).stream().map(tareaMapper::toDTO)
				.collect(Collectors.toList());
	}

	// ==============================================
	// 4. LISTAR TAREAS POR USUARIO
	// ==============================================

	@Override
	public List<TareaResponseDTO> listarTareasPorUsuario(Long usuarioId) {
		System.out.println("========== listarTareasPorUsuario ==========");
		System.out.println("Usuario ID recibido: " + usuarioId);

		try {
			// Buscar el alumno asociado a este usuario
			Optional<Alumno> alumnoOpt = alumnoRepository.findByUsuarioId(usuarioId);

			if (alumnoOpt.isEmpty()) {
				System.out.println("⚠️ No se encontró alumno para usuarioId: " + usuarioId);
				return new ArrayList<>();
			}

			Alumno alumno = alumnoOpt.get();
			System.out.println("✅ Alumno encontrado - ID: " + alumno.getId());

			List<Tarea> tareas = tareaRepository.findByAlumnoIdOrderByFechaSubidaDesc(alumno.getId());
			System.out.println("📚 Tareas encontradas: " + tareas.size());

			List<TareaResponseDTO> result = tareas.stream().map(tareaMapper::toDTO).collect(Collectors.toList());
			System.out.println("✅ DTOs convertidos: " + result.size());

			return result;

		} catch (Exception e) {
			System.out.println("❌ ERROR: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Error al listar tareas del alumno: " + e.getMessage(), e);
		}
	}

	// ==============================================
	// 5. DESCARGAR ARCHIVO PDF
	// ==============================================
	@Override
	public byte[] descargarArchivo(Long id) throws IOException {
		System.out.println("========== DESCARGAR ARCHIVO SERVICE ==========");
		System.out.println("ID de tarea: " + id);

		Tarea tarea = tareaRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));

		System.out.println("Tarea encontrada: " + tarea.getTitulo());
		System.out.println("RutaArchivo: " + tarea.getRutaArchivo());
		System.out.println("Alumno ID: " + tarea.getAlumno().getId());

		// Construir ruta completa
		String rutaCompleta = "uploads/tareas/" + tarea.getAlumno().getId() + "/" + tarea.getRutaArchivo();
		System.out.println("Ruta completa: " + rutaCompleta);

		File file = new File(rutaCompleta);
		System.out.println("¿El archivo existe? " + file.exists());
		System.out.println("Tamaño: " + (file.exists() ? file.length() : "N/A"));

		if (!file.exists()) {
			throw new IOException("Archivo no encontrado: " + rutaCompleta);
		}

		return fileStorageUtil.cargarArchivo(tarea.getRutaArchivo(), tarea.getAlumno().getId());
	}

	// ==============================================
	// 6. CALIFICAR TAREA
	// ==============================================

	@Override
	@Transactional
	public TareaResponseDTO calificarTarea(Long id, Double calificacion, String comentarios) {
		System.out.println("========== INICIO CALIFICAR TAREA ==========");
		System.out.println("ID recibido: " + id);
		System.out.println("Calificación recibida: " + calificacion);
		System.out.println("Comentarios recibidos: " + comentarios);

		try {
			// Buscar la tarea
			Tarea tarea = tareaRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));

			System.out.println("Tarea encontrada: " + tarea.getTitulo());
			System.out.println("Calificación actual: " + tarea.getCalificacion());
			System.out.println("Alumno ID: " + (tarea.getAlumno() != null ? tarea.getAlumno().getId() : "null"));

			// Validar que la calificación sea válida
			if (calificacion == null || calificacion < 0 || calificacion > 10) {
				throw new RuntimeException("Calificación inválida: " + calificacion);
			}

			// Actualizar usando mapper
			tareaMapper.updateEntityFromCalificacion(tarea, calificacion, comentarios);
			System.out.println("Mapper ejecutado");

			// Guardar
			Tarea tareaActualizada = tareaRepository.save(tarea);
			System.out.println("Tarea guardada con calificación: " + tareaActualizada.getCalificacion());

			// Convertir a DTO
			TareaResponseDTO dto = tareaMapper.toDTO(tareaActualizada);
			System.out.println("DTO creado correctamente");
			System.out.println("========== FIN CALIFICAR TAREA ==========");

			return dto;

		} catch (Exception e) {
			System.out.println("❌ ERROR en calificarTarea: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	// ==============================================
	// 7. ELIMINAR TAREA
	// ==============================================

	@Override
	@Transactional
	public void eliminarTarea(Long id) {
		Tarea tarea = tareaRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));

		// ✅ CORREGIDO: Pasar el ID del alumno también
		fileStorageUtil.eliminarArchivo(tarea.getRutaArchivo(), tarea.getAlumno().getId());

		// Eliminar de BD
		tareaRepository.delete(tarea);
	}

	// ==============================================
	// 8. BUSCAR TAREAS POR TÍTULO
	// ==============================================

	@Override
	public List<TareaResponseDTO> buscarPorTitulo(String titulo) {
		return tareaRepository.findByTituloContainingIgnoreCase(titulo).stream().map(tareaMapper::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<TareaResponseDTO> listarTodas() {
		return tareaRepository.findAll().stream().map(tareaMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public byte[] descargarArchivoEntrega(Long id) throws IOException {
	    System.out.println("========== DESCARGAR ARCHIVO ENTREGA ==========");
	    System.out.println("ID de tarea: " + id);
	    
	    Tarea tarea = tareaRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));
	    
	    if (tarea.getArchivoEntrega() == null || tarea.getArchivoEntrega().isEmpty()) {
	        throw new RuntimeException("El alumno aún no ha entregado esta tarea");
	    }
	    
	    System.out.println("Archivo entrega: " + tarea.getArchivoEntrega());
	    System.out.println("Alumno ID: " + tarea.getAlumno().getId());
	    
	    return fileStorageUtil.cargarArchivo(tarea.getArchivoEntrega(), tarea.getAlumno().getId());
	}

	
	
	public String generarLinkAccesoEntrega(Long tareaId, Long profesorId) {
	    // Verificar que el profesor tiene acceso a esta tarea
	    Tarea tarea = tareaRepository.findById(tareaId)
	        .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
	    
	    // Generar shortId único
	    String shortId;
	    do {
	        byte[] bytes = new byte[SHORT_ID_LENGTH];
	        secureRandom.nextBytes(bytes);
	        shortId = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	    } while (accesoEntregaRepository.findByShortId(shortId).isPresent());
	    
	    // Crear acceso con expiración (24 horas)
	    AccesoEntrega acceso = new AccesoEntrega(
	        shortId, 
	        tareaId, 
	        profesorId,
	        LocalDateTime.now().plusHours(24)
	    );
	    
	    accesoEntregaRepository.save(acceso);
	    
	    return shortId;
	}

	public byte[] obtenerEntregaPorShortId(String shortId) {
	    AccesoEntrega acceso = accesoEntregaRepository.findByShortId(shortId)
	        .orElseThrow(() -> new RuntimeException("Link inválido"));
	    
	    // Validar expiración
	    if (acceso.getFechaExpiracion().isBefore(LocalDateTime.now())) {
	        throw new RuntimeException("Link expirado");
	    }
	    
	    // Obtener la tarea
	    Tarea tarea = tareaRepository.findById(acceso.getTareaId())
	        .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
	    
	    if (tarea.getArchivoEntrega() == null || tarea.getArchivoEntrega().isEmpty()) {
	        throw new RuntimeException("No hay entrega disponible");
	    }
	    
	    // ✅ CORREGIDO: Cargar el archivo como byte[], no devolver el String
	    try {
	        // archivoEntrega almacena el NOMBRE del archivo, necesitas la ruta completa
	        return fileStorageUtil.cargarArchivo(tarea.getArchivoEntrega(), tarea.getAlumno().getId());
	    } catch (IOException e) {
	        throw new RuntimeException("Error al leer el archivo: " + e.getMessage());
	    }
	}

	@Override
	@Transactional
	public TareaResponseDTO entregarTarea(Long id, TareaRequestDTO request) throws IOException {
	    System.out.println("========== ENTREGAR TAREA SERVICE ==========");
	    System.out.println("Buscando tarea ID: " + id);
	    
	    // 1. Buscar la tarea existente
	    Tarea tarea = tareaRepository.findById(id)
	        .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));
	    
	    System.out.println("Tarea encontrada: " + tarea.getTitulo());
	    System.out.println("Alumno ID asignado: " + tarea.getAlumno().getId());
	    System.out.println("Alumno ID recibido: " + request.getAlumnoId());
	    
	    // 2. Validar que el alumno sea el correcto
	    if (!tarea.getAlumno().getId().equals(request.getAlumnoId())) {
	        throw new RuntimeException("Esta tarea no pertenece al alumno actual");
	    }
	    
	    // 3. Validar archivo
	    MultipartFile archivo = request.getArchivo();
	    if (archivo == null || archivo.isEmpty()) {
	        throw new RuntimeException("Debe seleccionar un archivo PDF");
	    }
	    
	    if (!archivo.getContentType().equals("application/pdf")) {
	        throw new RuntimeException("Solo se permiten archivos PDF");
	    }
	    
	    // 4. Guardar archivo
	    String nombreArchivo = fileStorageUtil.guardarArchivo(archivo, tarea.getAlumno().getId());
	    System.out.println("Archivo guardado: " + nombreArchivo);
	    
	    // 5. Actualizar la tarea
	    tarea.setTitulo(request.getTitulo());
	    tarea.setDescripcion(request.getDescripcion());
	    tarea.setArchivoEntrega(nombreArchivo);
	    tarea.setFechaEntrega(LocalDateTime.now());
	    tarea.setEstado("ENTREGADA");
	    
	    // 6. Guardar
	    Tarea tareaActualizada = tareaRepository.save(tarea);
	    System.out.println("✅ Tarea actualizada con éxito");
	    
	    return tareaMapper.toDTO(tareaActualizada);
	}

	@Override
	public List<Map<String, Object>> getPromedioPorAlumno() {
	    System.out.println("📊 Ejecutando consulta de promedios...");
	    try {
	        List<Object[]> resultados = tareaRepository.findPromedioPorAlumno();
	        System.out.println("✅ Resultados obtenidos: " + (resultados != null ? resultados.size() : "null"));
	        
	        List<Map<String, Object>> promedios = new ArrayList<>();
	        if (resultados != null) {
	            for (Object[] row : resultados) {
	                Map<String, Object> alumno = new HashMap<>();
	                alumno.put("alumnoId", row[0]);
	                alumno.put("alumnoNombre", row[1]);
	                double promedio = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
	                alumno.put("promedio", Math.round(promedio * 10) / 10.0);
	                alumno.put("tareasCalificadas", row[3]);
	                promedios.add(alumno);
	            }
	        }
	        return promedios;
	    } catch (Exception e) {
	        System.out.println("❌ Error en consulta: " + e.getMessage());
	        e.printStackTrace();
	        throw e;
	    }
	}

	@Override
	public Map<String, Object> getMiPromedio(Long usuarioId) {
	    System.out.println("📊 Calculando promedio para usuario: " + usuarioId);
	    
	    // Buscar el alumno asociado al usuario
	    Optional<Alumno> alumnoOpt = alumnoRepository.findByUsuarioId(usuarioId);
	    if (alumnoOpt.isEmpty()) {
	        throw new RuntimeException("No se encontró alumno para este usuario");
	    }
	    
	    Alumno alumno = alumnoOpt.get();
	    
	    // Calcular promedio de sus tareas calificadas
	    List<Tarea> tareasCalificadas = tareaRepository.findByAlumnoIdAndCalificacionIsNotNull(alumno.getId());
	    
	    double promedio = tareasCalificadas.stream()
	        .mapToDouble(Tarea::getCalificacion)
	        .average()
	        .orElse(0.0);
	    
	    Map<String, Object> resultado = new HashMap<>();
	    resultado.put("alumnoId", alumno.getId());
	    resultado.put("alumnoNombre", alumno.getNombre());
	    resultado.put("promedio", Math.round(promedio * 10) / 10.0);
	    resultado.put("tareasCalificadas", tareasCalificadas.size());
	    resultado.put("totalTareas", tareaRepository.countByAlumnoId(alumno.getId()));
	    
	    return resultado;
	}

    // ==============================================
    // ✅ PAGINACIÓN
    // ==============================================

    @Override
    public Page<Tarea> listarTareasPaginado(Pageable pageable, String titulo, String alumno, String estado) {
        if ((titulo != null && !titulo.isEmpty()) || (alumno != null && !alumno.isEmpty()) || (estado != null && !estado.isEmpty())) {
            return tareaRepository.buscarTareasPaginado(titulo, alumno, estado, pageable);
        }
        return tareaRepository.findAll(pageable);
    }
    
  

    @Override
    public Page<Tarea> listarMisTareasPaginado(Long usuarioId, Pageable pageable) {
        Optional<Alumno> alumnoOpt = alumnoRepository.findByUsuarioId(usuarioId);
        if (alumnoOpt.isEmpty()) {
            return Page.empty(pageable);
        }
        // ✅ Cambia a findByAlumnoId
        return tareaRepository.findByAlumnoId(alumnoOpt.get().getId(), pageable);
    }
    
    
    @Override
    public TareaResponseDTO convertirADTO(Tarea tarea) {
        return tareaMapper.toDTO(tarea);
    }
	
}
package com.peral.alumnos.service;

import com.peral.alumnos.dto.AlumnoResponseDTO;
import com.peral.alumnos.dto.CreateUsuarioRequest;
import com.peral.alumnos.dto.UpdateUsuarioRequest;
import com.peral.alumnos.dto.UsuarioDTO;
import com.peral.alumnos.dto.UsuarioResponseDTO;
import com.peral.alumnos.dto.mapper.UsuarioMapper;
import com.peral.alumnos.exception.EmailDuplicadoException;
import com.peral.alumnos.exception.PasswordValidationException;
import com.peral.alumnos.exception.UsernameDuplicadoException;
import com.peral.alumnos.exception.UsuarioNoEncontradoException;
import com.peral.alumnos.model.Alumno;
import com.peral.alumnos.model.Usuario;
import com.peral.alumnos.repository.AlumnoRepository;
import com.peral.alumnos.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final PasswordResetService passwordResetService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
            AlumnoRepository alumnoRepository,
            PasswordEncoder passwordEncoder,
            UsuarioMapper usuarioMapper,
            PasswordResetService passwordResetService) {
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
        this.passwordResetService = passwordResetService;
    }

    // ==============================================
    // CRUD BÁSICO
    // ==============================================

    @Override
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con ID: " + id));
        
        UsuarioResponseDTO response = usuarioMapper.toResponseDTO(usuario);
        
        if (usuario.getAlumno() != null) {
            Alumno alumno = usuario.getAlumno();
            AlumnoResponseDTO alumnoDTO = new AlumnoResponseDTO(
                alumno.getId(),
                alumno.getNombre(),
                alumno.getEmail(),
                alumno.getEdad(),
                alumno.getCarrera()
            );
            response.setAlumno(alumnoDTO);
        }
        
        return response;
    }

    // ==============================================
    // CREATE CON VALIDACIÓN DE CONTRASEÑA
    // ==============================================
    
    @Override
    @Transactional
    public UsuarioResponseDTO create(CreateUsuarioRequest request) {
        // Validar unicidad con mensajes detallados
        if (existeUsername(request.getUsername())) {
            throw new UsernameDuplicadoException("El username '" + request.getUsername() + "' ya está en uso");
        }
        if (existeEmail(request.getEmail())) {
            throw new EmailDuplicadoException("El email '" + request.getEmail() + "' ya está registrado");
        }
        
        // Validar contraseña con el método de PasswordResetService
        if (request.getPassword() != null) {
            passwordResetService.validatePassword(request.getPassword());
        } else {
            throw new PasswordValidationException("La contraseña es obligatoria");
        }
        
        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(request.getRole());
        usuario.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Si es alumno...
        if ("ROLE_ALUMNO".equals(request.getRole()) && request.getNombreAlumno() != null) {
            Alumno alumno = new Alumno();
            alumno.setNombre(request.getNombreAlumno());
            alumno.setEmail(request.getEmail());
            alumno.setEdad(request.getEdadAlumno());
            alumno.setCarrera(request.getCarreraAlumno());
            alumno.setUsuario(usuarioGuardado);
            alumnoRepository.save(alumno);
            usuarioGuardado.setAlumno(alumno);
            usuarioRepository.save(usuarioGuardado);
        }
        
        return usuarioMapper.toResponseDTO(usuarioGuardado);
    }
    
    
    @Override
    @Transactional
    public UsuarioResponseDTO update(Long id, UpdateUsuarioRequest request) {
        System.out.println("========== UPDATE USUARIO ==========");
        System.out.println("ID: " + id);
        
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con ID: " + id));
            
            System.out.println("Usuario encontrado: " + usuario.getUsername());
            
            // Validar email único (si cambió)
            if (!usuario.getEmail().equals(request.getEmail()) && existeEmail(request.getEmail())) {
                throw new EmailDuplicadoException("El email '" + request.getEmail() + "' ya está registrado");
            }
            
            System.out.println("Email validado");
            
            // Actualizar campos
            usuario.setEmail(request.getEmail());
            
            // Solo actualizar password si se proporcionó
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                passwordResetService.validatePassword(request.getPassword());
                usuario.setPassword(passwordEncoder.encode(request.getPassword()));
                System.out.println("Contraseña actualizada");
            }
            
            // Actualizar rol (solo si viene)
            if (request.getRole() != null && !request.getRole().isEmpty()) {
                usuario.setRole(request.getRole());
                System.out.println("Rol actualizado a: " + request.getRole());
            }
            
            Usuario usuarioActualizado = usuarioRepository.save(usuario);
            System.out.println("Usuario guardado");
            
            // Actualizar alumno si es ROLE_ALUMNO y tiene datos
            if ("ROLE_ALUMNO".equals(usuario.getRole()) && request.getNombreAlumno() != null) {
                System.out.println("Actualizando datos de alumno");
                Alumno alumno = usuario.getAlumno();
                if (alumno == null) {
                    alumno = new Alumno();
                    alumno.setUsuario(usuarioActualizado);
                    System.out.println("Creando nuevo alumno");
                }
                if (request.getNombreAlumno() != null) alumno.setNombre(request.getNombreAlumno());
                if (request.getCarreraAlumno() != null) alumno.setCarrera(request.getCarreraAlumno());
                if (request.getEdadAlumno() != null) alumno.setEdad(request.getEdadAlumno());
                alumno.setEmail(usuario.getEmail());
                alumnoRepository.save(alumno);
                System.out.println("Alumno actualizado");
            }
            
            return usuarioMapper.toResponseDTO(usuarioActualizado);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con ID: " + id));
        
        usuario.setEnabled(false);
        usuarioRepository.save(usuario);
    }

    // ==============================================
    // MÉTODOS ESPECÍFICOS
    // ==============================================

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarRol(Long id, String nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con ID: " + id));
        
        if (!nuevoRol.matches("ROLE_ADMIN|ROLE_PROFESOR|ROLE_ALUMNO")) {
            throw new RuntimeException("Rol inválido");
        }
        
        usuario.setRole(nuevoRol);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        
        if (!"ROLE_ALUMNO".equals(nuevoRol) && usuario.getAlumno() != null) {
            alumnoRepository.delete(usuario.getAlumno());
            usuario.setAlumno(null);
        }
        
        return usuarioMapper.toResponseDTO(usuarioActualizado);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarEstado(Long id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con ID: " + id));
        
        usuario.setEnabled(activo);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        
        return usuarioMapper.toResponseDTO(usuarioActualizado);
    }

   
   
    @Override
    public boolean existeUsername(String username) {
        return usuarioRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean existeEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    @Override
    public UsuarioResponseDTO getMiPerfil(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        
        UsuarioResponseDTO response = usuarioMapper.toResponseDTO(usuario);
        
        // ✅ Incluir datos del alumno si existe
        if (usuario.getAlumno() != null) {
            Alumno alumno = usuario.getAlumno();
            AlumnoResponseDTO alumnoDTO = new AlumnoResponseDTO(
                alumno.getId(),
                alumno.getNombre(),
                alumno.getEmail(),
                alumno.getEdad(),
                alumno.getCarrera()
            );
            response.setAlumno(alumnoDTO);
        }
        
        return response;
    }
    @Override
    public List<UsuarioDTO> findByRol(String rol) {
        return usuarioRepository.findByRole(rol)  // ← AHORA USA JPA
                .stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> findByEmailContaining(String email) {
        return usuarioRepository.findByEmailContainingIgnoreCase(email)  // ← JPA
                .stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> findByUsernameContaining(String username) {
        return usuarioRepository.findByUsernameContainingIgnoreCase(username)  // ← JPA
                .stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioDTO findByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado: " + username));
        return usuarioMapper.toDTO(usuario);
    }

    // ==============================================
    // ✅ PAGINACIÓN
    // ==============================================

    @Override
    public Page<Usuario> listarPaginado(Pageable pageable) {
        return usuarioRepository.findAllByEnabledTrue(pageable);
    }

    @Override
    public Page<Usuario> buscarPaginado(String busqueda, Pageable pageable) {
        return usuarioRepository.buscarPaginado(busqueda, pageable);
    }
    
    
    
}
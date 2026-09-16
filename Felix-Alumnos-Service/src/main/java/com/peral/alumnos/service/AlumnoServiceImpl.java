package com.peral.alumnos.service;

import com.peral.alumnos.dto.AlumnoRequestDTO;
import com.peral.alumnos.dto.AlumnoResponseDTO;
import com.peral.alumnos.dto.mapper.AlumnoMapper;
import com.peral.alumnos.exception.AlumnoNoEncontradoException;
import com.peral.alumnos.exception.EmailDuplicadoException;
import com.peral.alumnos.model.Alumno;
import com.peral.alumnos.repository.AlumnoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlumnoServiceImpl implements AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final AlumnoMapper alumnoMapper;

    public AlumnoServiceImpl(AlumnoRepository alumnoRepository, AlumnoMapper alumnoMapper) {
        this.alumnoRepository = alumnoRepository;
        this.alumnoMapper = alumnoMapper;
    }

    @Override
    public List<AlumnoResponseDTO> findAll() {
        return alumnoRepository.findAll()
                .stream()
                .map(alumnoMapper::toResponseDTO)
                .filter(dto -> dto.getActivo())
                .collect(Collectors.toList());
    }
   
    @Override
    public AlumnoResponseDTO findById(Long id) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new AlumnoNoEncontradoException("Alumno no encontrado con ID: " + id));
        
        if (!alumno.getActivo()) {
            throw new AlumnoNoEncontradoException("Alumno no encontrado con ID: " + id);
        }
        
        return alumnoMapper.toResponseDTO(alumno);
    }

    @Override
    @Transactional
    public AlumnoResponseDTO create(AlumnoRequestDTO dto) {
        if (alumnoRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException("El email '" + dto.getEmail() + "' ya está registrado");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Alumno alumno = alumnoMapper.toEntity(dto, null);
        Alumno alumnoGuardado = alumnoRepository.save(alumno);
        return alumnoMapper.toResponseDTO(alumnoGuardado);
    }

    @Override
    @Transactional
    public AlumnoResponseDTO update(Long id, AlumnoRequestDTO dto) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new AlumnoNoEncontradoException("Alumno no encontrado con ID: " + id));
        
        if (!alumno.getActivo()) {
            throw new AlumnoNoEncontradoException("No se puede editar un alumno inactivo");
        }

        if (!alumno.getEmail().equals(dto.getEmail()) && 
            alumnoRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException("El email '" + dto.getEmail() + "' ya está registrado");
        }

        alumnoMapper.updateEntity(dto, alumno);
        Alumno alumnoActualizado = alumnoRepository.save(alumno);
        return alumnoMapper.toResponseDTO(alumnoActualizado);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new AlumnoNoEncontradoException("Alumno no encontrado con ID: " + id));
        
        alumno.setActivo(false);
        alumnoRepository.save(alumno);
        
        System.out.println("✅ Alumno ID " + id + " marcado como inactivo (soft delete)");
    }

    @Override
    public List<AlumnoResponseDTO> findAllOrderByEdadAsc() {
        return alumnoRepository.findAllByOrderByEdadAsc()
                .stream()
                .filter(Alumno::getActivo)
                .map(alumnoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlumnoResponseDTO> findAllOrderByEdadDesc() {
        return alumnoRepository.findAllByOrderByEdadDesc()
                .stream()
                .filter(Alumno::getActivo)
                .map(alumnoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AlumnoResponseDTO> findByUsuarioId(Long usuarioId) {
        System.out.println("🔍 Buscando alumnos por usuarioId: " + usuarioId);
        
        List<Alumno> alumnos = alumnoRepository.findAllByUsuarioId(usuarioId);
        
        return alumnos.stream()
                .map(alumnoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ✅ PAGINACIÓN - SOLO ESTOS DOS MÉTODOS
    @Override
    public Page<Alumno> listarPaginado(Pageable pageable) {
        return alumnoRepository.findAllActivosPaginado(pageable);
    }

    @Override
    public Page<Alumno> buscarPaginado(String busqueda, Pageable pageable) {
        return alumnoRepository.buscarPaginado(busqueda, pageable);
    }
}
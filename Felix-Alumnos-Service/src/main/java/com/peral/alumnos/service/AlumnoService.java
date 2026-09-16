package com.peral.alumnos.service;

import com.peral.alumnos.dto.AlumnoRequestDTO;
import com.peral.alumnos.dto.AlumnoResponseDTO;
import com.peral.alumnos.model.Alumno;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AlumnoService {

    List<AlumnoResponseDTO> findAll();
    AlumnoResponseDTO findById(Long id);
    AlumnoResponseDTO create(AlumnoRequestDTO dto);
    AlumnoResponseDTO update(Long id, AlumnoRequestDTO dto);
    void delete(Long id);
    List<AlumnoResponseDTO> findAllOrderByEdadAsc();
    List<AlumnoResponseDTO> findAllOrderByEdadDesc();
    List<AlumnoResponseDTO> findByUsuarioId(Long usuarioId);
    
    // ✅ Paginación
    Page<Alumno> listarPaginado(Pageable pageable);
    Page<Alumno> buscarPaginado(String busqueda, Pageable pageable);
}
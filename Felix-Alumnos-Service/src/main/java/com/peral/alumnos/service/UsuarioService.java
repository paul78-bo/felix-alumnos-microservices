package com.peral.alumnos.service;

import com.peral.alumnos.dto.CreateUsuarioRequest;
import com.peral.alumnos.dto.UpdateUsuarioRequest;
import com.peral.alumnos.dto.UsuarioDTO;
import com.peral.alumnos.dto.UsuarioResponseDTO;
import com.peral.alumnos.model.Usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UsuarioService {
    
    List<UsuarioDTO> findAll();
    UsuarioResponseDTO findById(Long id);
    UsuarioResponseDTO create(CreateUsuarioRequest request);
    UsuarioResponseDTO update(Long id, UpdateUsuarioRequest request);
    void delete(Long id);
    UsuarioResponseDTO cambiarRol(Long id, String nuevoRol);
    UsuarioResponseDTO cambiarEstado(Long id, Boolean activo);
    List<UsuarioDTO> findByRol(String rol);
    List<UsuarioDTO> findByEmailContaining(String email);
    List<UsuarioDTO> findByUsernameContaining(String username);
    boolean existeUsername(String username);
    boolean existeEmail(String email);
    UsuarioResponseDTO getMiPerfil(String username);
    UsuarioDTO findByUsername(String username);
    
    // ✅ NUEVOS MÉTODOS DE PAGINACIÓN
    Page<Usuario> listarPaginado(Pageable pageable);
    Page<Usuario> buscarPaginado(String busqueda, Pageable pageable);
}
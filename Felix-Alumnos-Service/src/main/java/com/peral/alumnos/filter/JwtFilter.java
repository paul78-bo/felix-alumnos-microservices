package com.peral.alumnos.filter;

import com.peral.alumnos.service.CustomUserDetailsService;
import com.peral.alumnos.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        System.out.println("\n-------------------------------");
        System.out.println("🔐 JwtFilter - Request: " + path);

        // 🔓 Endpoints públicos (Swagger, Auth, H2)
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/api/auth")
                || path.startsWith("/h2-console")
                || path.startsWith("/reset-password")
        		|| path.startsWith("/actuator/")) {  // ← AÑADE ESTA LÍNEA

			System.out.println("✅ Ruta pública, saltando JWT");
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("👤 Usuario extraído del token: " + username);
            } catch (Exception e) {
                System.out.println("❌ Error al extraer usuario: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Cabecera Authorization ausente o inválida");
        }

        // 🔐 Validar token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
             // ✅ AGREGAR ESTO - Extraer userId del token
                Long userId = jwtUtil.extractUserId(jwt);
                request.setAttribute("userId", userId);
                
                System.out.println("✅ Token válido, usuario autenticado - ID: " + userId);
                System.out.println("✅ Token válido, usuario autenticado");
            } else {
                System.out.println("❌ Token no válido o expirado");
            }
        }

        filterChain.doFilter(request, response);
    }

}

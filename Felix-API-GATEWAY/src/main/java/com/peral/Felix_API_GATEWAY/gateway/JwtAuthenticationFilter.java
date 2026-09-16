package com.peral.Felix_API_GATEWAY.gateway;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

	private final JwtUtil jwtUtil;

	public JwtAuthenticationFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
		System.out.println("🔐 JWT Filter inicializado");
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();
		String method = request.getMethod().name();
		
		System.out.println("🔥 FILTRO PROCESANDO: " + path);
		System.out.println("🌐 Gateway recibió: " + method + " " + path);
		
		// Permitir login y OPTIONS (preflight)
		if (path.startsWith("/api/auth") || path.startsWith("/actuator/") ||  
		    path.startsWith("/reset-password") || path.equals("/favicon.ico") || 
		    "OPTIONS".equalsIgnoreCase(method)) {
			
			System.out.println("✅ Ruta permitida sin JWT: " + path);
			System.out.println("🔍 Path original: " + path);
			
			// ==============================================
			// ✅ CORRECCIÓN: Cambiar la ruta REAL de la petición
			// ==============================================
			ServerHttpRequest mutatedRequest;
			
			if (path != null && path.startsWith("/reset-password")) {
			    String newPath = "/api/auth" + path;
			    System.out.println("🔄 Ruta transformada a: " + newPath);
			    System.out.println("📤 Enviando a: http://localhost:8081" + newPath);
			    
			    // ✅ CAMBIO IMPORTANTE: usar .path() para modificar la ruta real
			    mutatedRequest = request.mutate()
			    		.path(newPath)  // ← Cambia la ruta de la petición
			    		.header("X-Gateway-Route", "auth-route")
			    		.header("X-Backend-Target", "http://localhost:8081" + newPath)
			    		.build();
			} else {
			    System.out.println("❌ No se transformó");
			    System.out.println("📤 Enviando a: http://localhost:8081" + path);
			    
			    mutatedRequest = request.mutate()
			    		.header("X-Gateway-Route", "auth-route")
			    		.header("X-Backend-Target", "http://localhost:8081" + path)
			    		.build();
			}

			return chain.filter(exchange.mutate().request(mutatedRequest).build());
		}

		// Obtener token para rutas protegidas
		String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			System.out.println("❌ No hay token en la petición: " + path);
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		String token = authHeader.substring(7);

		if (!jwtUtil.validateToken(token)) {
			System.out.println("❌ Token inválido para: " + path);
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		// Extraer claims
		String username = jwtUtil.extractUsername(token);
		String role = jwtUtil.extractRole(token);

		System.out.println("✅ Token válido - Usuario: " + username + ", Rol: " + role);

		// Agregar headers custom al request
		ServerHttpRequest mutated = request.mutate()
				.header("X-User", username)
				.header("X-Role", role)
				.header("X-Gateway-Route", "protected-route")
				.header("X-Backend-Target", "http://localhost:8081" + path)
				.build();

		return chain.filter(exchange.mutate().request(mutated).build());
	}
}
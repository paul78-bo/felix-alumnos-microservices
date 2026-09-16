const auth = {
	
	apiBase: 'http://localhost:8080/api/auth',
	
	// ✅ Verificar sesión con el backend
	async verificarSesionInicial() {
	    const token = this.getToken();
	    if (!token) return false;
	    
	    // Primero verificar expiración local
	    try {
	        const payload = JSON.parse(atob(token.split('.')[1]));
	        const expiracion = payload.exp * 1000;
	        if (Date.now() > expiracion) {
	            console.log('⏰ Token expirado localmente');
	            this.logout();
	            return false;
	        }
	    } catch (e) {
	        console.error('Error decodificando token:', e);
	        this.logout();
	        return false;
	    }
	    
	    // ✅ Verificar con el backend que el token sea válido
	    const esValido = await this.verificarConBackend(token);
	    if (!esValido) {
	        console.log('❌ Token inválido según el backend');
	        this.logout();
	        return false;
	    }
	    
	    console.log('✅ Sesión válida');
	    return true;
	},
	
	// ✅ Verificar token contra el backend
	async verificarConBackend(token) {
	    try {
	        const response = await fetch(`${this.apiBase}/verificar`, {
	            method: 'GET',
	            headers: { 
	                'Authorization': `Bearer ${token}`,
	                'Content-Type': 'application/json'
	            }
	        });
	        
	        if (!response.ok) {
	            console.log('❌ Respuesta backend: ', response.status);
	            return false;
	        }
	        
	        const data = await response.json();
	        console.log('✅ Backend Verificada:', data);
	        return data.valido === true;
	        
	    } catch (error) {
	        console.error('Error verificando token en backend:', error);
	        return false;
	    }
	},
	
	// ✅ Login completo con rate limiting
	async login(username, password) {
	    console.log('🔐 Intentando login para:', username);
	    
	    try {
	        const response = await fetch(`${this.apiBase}/login`, {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/json' },
	            body: JSON.stringify({ username, password })
	        });
	        
	        console.log('📡 Status respuesta:', response.status);
	        
	        // ✅ MANEJAR RATE LIMITING (429 - Too Many Requests)
			if (response.status === 429) {
			    const data = await response.json();
			    return { 
			        success: false, 
			        status: 429,
			        message: data.message || 'Demasiados intentos. Espere un momento.',
			        blockedUntil: data.blockedUntil  // ← Agregar esto
			    };
			}
	        // ✅ LOGIN EXITOSO
	        if (response.ok) {
	            const data = await response.json();
	            
	            // Limpiar sesiones anteriores
	            sessionStorage.clear();
	            
	            // Guardar todos los datos necesarios
	            sessionStorage.setItem('jwt_token', data.token);
	            sessionStorage.setItem('token', data.token);
	            sessionStorage.setItem('user_role', data.role);
	            sessionStorage.setItem('username', data.username);
	            sessionStorage.setItem('user_nombre', data.username);
	            
	            // Guardar expiración calculada
	            try {
	                const payload = JSON.parse(atob(data.token.split('.')[1]));
	                const expiracion = payload.exp * 1000;
	                sessionStorage.setItem('token_expires', expiracion);
	            } catch (e) {}
	            
	            console.log('✅ Sesión iniciada correctamente');
	            return { success: true };
	        }
	        
	        // ✅ LOGIN FALLIDO (401) - con mensaje del backend
	        if (response.status === 401) {
	            try {
	                const data = await response.json();
	                return { 
	                    success: false, 
	                    status: 401,
	                    message: data.message || data.error || 'Credenciales incorrectas',
	                    remainingAttempts: data.remainingAttempts
	                };
	            } catch (e) {
	                return { success: false, message: 'Credenciales incorrectas' };
	            }
	        }
	        
	        // OTROS ERRORES
	        return { success: false, message: 'Error en el servidor' };
	        
	    } catch (error) {
	        console.error('❌ Error de conexión:', error);
	        return { success: false, message: 'Error de conexión con el servidor' };
	    }
	},
	// ✅ Cerrar sesión
	logout() {
	    console.log('👋 Ejecutando logout...');
	    sessionStorage.clear();
	    localStorage.clear();
	    window.location.href = 'login.html';
	},
	
	// ✅ Verificación rápida (solo local, sin backend)
	hasValidToken() {
	    const token = sessionStorage.getItem('jwt_token');
	    if (!token) return false;
	    
	    try {
	        const payload = JSON.parse(atob(token.split('.')[1]));
	        const expiracion = payload.exp * 1000;
	        if (Date.now() > expiracion) {
	            console.log('⏰ Token expirado localmente');
	            this.logout();
	            return false;
	        }
	    } catch (e) {
	        console.error('Error verificando token:', e);
	        return false;
	    }
	    
	    return true;
	},
	
	
	getToken() {
	    return sessionStorage.getItem('jwt_token');
	},
	
	// ✅ Obtener información del usuario
	getUserInfo() {
	    return {
	        username: sessionStorage.getItem('username'),
	        role: sessionStorage.getItem('user_role'),
	        token: sessionStorage.getItem('jwt_token')
	    };
	},
	
	// ✅ Verificar permisos
	tienePermiso(accion) {
	    const role = sessionStorage.getItem('user_role');
	    const permisos = {
	        'crear_alumno': ['ROLE_ADMIN', 'ROLE_PROFESOR'],
	        'editar_alumno': ['ROLE_ADMIN', 'ROLE_PROFESOR'],
	        'eliminar_alumno': ['ROLE_ADMIN'],
	        'ver_alumnos': ['ROLE_ADMIN', 'ROLE_PROFESOR', 'ROLE_ALUMNO']
	    };
	    return permisos[accion]?.includes(role) || false;
	},
	
	// ✅ Obtener rol amigable
	getRolAmigable() {
	    const role = sessionStorage.getItem('user_role');
	    const roles = {
	        'ROLE_ADMIN': 'Administrador',
	        'ROLE_PROFESOR': 'Profesor',
	        'ROLE_ALUMNO': 'Alumno'
	    };
	    return roles[role] || 'Usuario';
	},
	
	// ✅ Redirigir a recuperación de contraseña
	redirectToForgotPassword() {
	    window.location.href = 'forgot-password.html';
	}
};

// ==============================================
// FUNCIONES GLOBALES
// ==============================================

function mostrarMenuUsuario() {
    const userInfo = auth.getUserInfo();
    const username = userInfo.username;
    const role = auth.getRolAmigable();

    const authButtons = document.getElementById('authButtons');
    const userMenu = document.getElementById('userMenu');
    const userInfoSpan = document.getElementById('userInfo');

    if (authButtons) authButtons.style.display = 'none';
    if (userMenu) userMenu.style.display = 'block';
    if (userInfoSpan) userInfoSpan.textContent = `${username} (${role})`;
}

function mostrarBotonesAuth() {
    const authButtons = document.getElementById('authButtons');
    const userMenu = document.getElementById('userMenu');
    if (authButtons) authButtons.style.display = 'block';
    if (userMenu) userMenu.style.display = 'none';
}

// Funciones globales
window.mostrarMenuUsuario = mostrarMenuUsuario;
window.mostrarBotonesAuth = mostrarBotonesAuth;
window.logout = () => {
    auth.logout();
};
window.auth = auth;

console.log('✅ auth.js cargado correctamente');


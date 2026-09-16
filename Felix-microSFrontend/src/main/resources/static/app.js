const app = {
    async init() {
        console.log('🚀 Inicializando aplicación...');
        this.configurarEventos();
        
        // NUNCA mostrar alumnos primero, SIEMPRE login
        this.mostrarPagina('login');
        
        // Esperar un poco para que la UI se estabilice
        setTimeout(async () => {
            await this.verificarAutenticacion();
        }, 300);
    },

    configurarEventos() {
        console.log('⚙️ Configurando eventos...');
        
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            console.log('✅ Formulario de login encontrado');
            loginForm.addEventListener('submit', async e => {
                e.preventDefault();
                console.log('📝 Submit del formulario de login');
                
                const username = document.getElementById('username').value;
                const password = document.getElementById('password').value;
                
                console.log('🔐 Credenciales:', { username, password: '***' });
                
                const ok = await auth.login(username, password);
                if (ok) {
                    console.log('✅ Login exitoso');
                    window.mostrarMenuUsuario();
                    this.mostrarPagina('alumnos');
                    
                    // Cargar alumnos SOLO después de login exitoso
                    console.log('🔄 Cargando alumnos después del login...');
                    if (typeof alumnos !== 'undefined') {
                        alumnos.cargarAlumnos();
                    }
                } else {
                    console.error('❌ Login fallido');
                    alert('Credenciales incorrectas o error de conexión');
                }
            });
        } else {
            console.warn('⚠️ Formulario de login NO encontrado');
        }
    },

    async verificarAutenticacion() {
        console.log('🔍 Verificando autenticación al iniciar...');
        
        // Verificación SÍNCRONA y RÁPIDA primero
        const hasToken = auth.hasValidToken();
        
        if (!hasToken) {
            console.log('❌ Usuario NO autenticado, mostrando login');
            this.mostrarPagina('login');
            return;
        }
        
        // Si hay token, mostrar página de alumnos PERO verificar async
        console.log('🔑 Token encontrado, verificando con backend...');
        window.mostrarMenuUsuario();
        this.mostrarPagina('alumnos');
        
        // Cargar alumnos y hacer validación REAL con backend
        if (typeof alumnos !== 'undefined') {
            setTimeout(() => {
                alumnos.cargarAlumnos();
            }, 100);
        }
        
        // Validación async en segundo plano
        try {
            const isReallyAuth = await auth.isAuthenticated();
            if (!isReallyAuth) {
                console.log('⚠️ Token inválido en validación async');
            }
        } catch (error) {
            console.error('❌ Error en validación async:', error);
        }
    },

    mostrarPagina(pagina) {
        console.log(`📄 Cambiando a página: ${pagina}`);
        
        document.querySelectorAll('.page').forEach(p => {
            p.classList.remove('active');
        });
        
        const pEl = document.getElementById(pagina + 'Page');
        if (pEl) {
            pEl.classList.add('active');
            console.log(`✅ Página ${pagina} activada`);
            
            // ✅ ACTUALIZAR NOMBRE DEL USUARIO
            this.actualizarNombreEnPagina(pagina);
            
        } else {
            console.error(`❌ Página ${pagina} no encontrada`);
        }
    },  // ← ✅ COMA AGREGADA AQUÍ

    // ✅ NUEVA FUNCIÓN: Actualizar nombre del usuario en la página actual
    actualizarNombreEnPagina(pagina) {
        const userInfo = auth.getUserInfo();
        const username = userInfo.username;
        const roleRaw = localStorage.getItem('user_role');
        const role = auth.getRolAmigable ? auth.getRolAmigable() : 'Usuario';

        if (pagina === 'alumnos') {
            const alumnosUserNameText = document.getElementById('alumnosUserNameText');
            if (alumnosUserNameText) {
                alumnosUserNameText.innerHTML = `<strong>${username}</strong> <span class="badge bg-${getRoleBadgeColor(roleRaw)}">${role}</span>`;
            }
        }
    }
};  // ← FIN DEL OBJETO app

// ==============================================
// FUNCIONES GLOBALES - MENÚ COMPLETO
// ==============================================

// Función mejorada para mostrar menú lateral y mensaje de bienvenida
window.mostrarMenuUsuario = function() {
    const userInfo = auth.getUserInfo();
    const username = userInfo.username;      // ← "Felix"
    const role = auth.getRolAmigable ? auth.getRolAmigable() : 'Usuario';  // ← "Administrador"
    const roleRaw = localStorage.getItem('user_role');

    console.log('👤 Mostrando menú lateral para:', username, '(', role, ')');

    // ==========================================
    // 1. ELEMENTOS DEL NAVBAR SUPERIOR
    // ==========================================
    const sidebar = document.getElementById('sidebar');
    const authButtons = document.getElementById('authButtons');
    const welcomeMessage = document.getElementById('welcomeMessage');
    const pageSubtitle = document.getElementById('pageSubtitle');
	const menuTareas = document.getElementById('menuTareas');
	if (menuTareas) {
	    menuTareas.style.display = 'block';  // Visible para todos los roles
	}
    
    // ✅ Mensaje de bienvenida: "Bienvenido, Felix (Administrador)"
    if (welcomeMessage) {
        welcomeMessage.innerHTML = `Bienvenido, <strong>${username}</strong> <span class="badge bg-${getRoleBadgeColor(roleRaw)} ms-2">${role}</span>`;
    }
    
    if (pageSubtitle) {
        pageSubtitle.textContent = 'Gestión Académica';
    }

    // ==========================================
    // 2. INFORMACIÓN DEL USUARIO EN SIDEBAR
    // ==========================================
    const sidebarUserInfo = document.getElementById('sidebarUserInfo');
    const sidebarUserName = document.getElementById('sidebarUserName');
    const sidebarUserRole = document.getElementById('sidebarUserRole');
    
    if (sidebarUserInfo) {
        sidebarUserInfo.style.display = 'block';
        if (sidebarUserName) sidebarUserName.textContent = username;  // ← "Felix"
        if (sidebarUserRole) {
            sidebarUserRole.innerHTML = `<span class="badge bg-${getRoleBadgeColor(roleRaw)}" style="font-size: 0.7rem;">${role}</span>`;
        }
    }

    // ==========================================
    // 3. CONTROL DE VISIBILIDAD
    // ==========================================
    if (sidebar) sidebar.classList.remove('hidden');
    if (authButtons) authButtons.style.display = 'none';
    
    // Mostrar opción de usuarios SOLO para ADMIN
    const menuUsuarios = document.getElementById('menuUsuarios');
    if (menuUsuarios) {
        menuUsuarios.style.display = roleRaw === 'ROLE_ADMIN' ? 'block' : 'none';
    }
    
    // ==========================================
    // 4. MARCAR ITEM ACTIVO EN EL MENÚ
    // ==========================================
    const currentPage = document.querySelector('.page.active')?.id || 'alumnosPage';
    const menuItems = document.querySelectorAll('.sidebar-menu li');
    menuItems.forEach(item => item.classList.remove('active'));
    
    if (currentPage === 'alumnosPage') {
        document.getElementById('menuAlumnos')?.classList.add('active');
        if (pageSubtitle) pageSubtitle.textContent = 'Gestión de Alumnos';
    }
    
    // ==========================================
    // 5. ACTUALIZAR NOMBRE EN PÁGINA DE ALUMNOS
    // ==========================================
    const alumnosUserNameText = document.getElementById('alumnosUserNameText');
    if (alumnosUserNameText) {
        alumnosUserNameText.innerHTML = `<strong>${username}</strong> <span class="badge bg-${getRoleBadgeColor(roleRaw)}">${role}</span>`;
    }
}

// Función auxiliar para color del badge según rol
function getRoleBadgeColor(role) {
    const colors = {
        'ROLE_ADMIN': 'danger',
        'ROLE_PROFESOR': 'warning',
        'ROLE_ALUMNO': 'success'
    };
    return colors[role] || 'secondary';
}

// Función para ocultar menú (cuando se cierra sesión)
window.mostrarBotonesAuth = function() {
    console.log('👤 Mostrando botones de autenticación');
    
    const sidebar = document.getElementById('sidebar');
    const authButtons = document.getElementById('authButtons');
    const welcomeMessage = document.getElementById('welcomeMessage');
    const pageSubtitle = document.getElementById('pageSubtitle');
    const sidebarUserInfo = document.getElementById('sidebarUserInfo');
    
    if (sidebar) sidebar.classList.add('hidden');
    if (authButtons) authButtons.style.display = 'block';
    
    // Ocultar información del usuario en sidebar
    if (sidebarUserInfo) sidebarUserInfo.style.display = 'none';
    
    // Restaurar mensaje genérico
    if (welcomeMessage) welcomeMessage.textContent = 'Bienvenido';
    if (pageSubtitle) pageSubtitle.textContent = 'Gestión Académica';
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    console.log('📄 DOM completamente cargado');
    app.init();
});

// Funciones globales
window.mostrarPagina = pagina => app.mostrarPagina(pagina);
window.app = app;

console.log('✅ app.js cargado correctamente');
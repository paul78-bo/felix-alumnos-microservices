const perfil = {
    apiBase: 'http://localhost:8080/api/usuarios',
    datosOriginales: null,

    async cargarPerfil() {
        console.log('🔍 Cargando perfil...');
        
        const token = auth.getToken();
        if (!token) {
            window.location.href = 'login.html';
            return;
        }

        try {
            const response = await fetch(`${this.apiBase}/me`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                if (response.status === 401) {
                    alert('Sesión expirada');
                    auth.logout();
                    window.location.href = 'login.html';
                }
                throw new Error('Error al cargar perfil');
            }

            const data = await response.json();
            this.datosOriginales = data;
            this.mostrarPerfil(data);
            
        } catch (error) {
            console.error('Error:', error);
            alert('Error al cargar el perfil');
        }
    },

    mostrarPerfil(usuario) {
        console.log('📝 Mostrando perfil:', usuario);

        // Información básica del usuario
        document.getElementById('perfilNombre').textContent = usuario.username;
        document.getElementById('viewId').textContent = usuario.id || '-';
        document.getElementById('viewUsername').textContent = usuario.username;
        document.getElementById('viewEmail').textContent = usuario.email;
        
        // Rol con badge
        const roleBadge = document.getElementById('perfilRolBadge');
        const roleText = document.getElementById('viewRole');
        const userRole = usuario.role;
        
        if (userRole === 'ROLE_ADMIN') {
            roleBadge.innerHTML = '<span class="badge bg-danger">Administrador</span>';
            roleText.textContent = 'Administrador';
            document.getElementById('alumnoInfo').style.display = 'none';
            document.getElementById('alumnoCarreraInfo').style.display = 'none';
            document.getElementById('alumnoEdadInfo').style.display = 'none';
        } else if (userRole === 'ROLE_PROFESOR') {
            roleBadge.innerHTML = '<span class="badge bg-warning">Profesor</span>';
            roleText.textContent = 'Profesor';
            document.getElementById('alumnoInfo').style.display = 'none';
            document.getElementById('alumnoCarreraInfo').style.display = 'none';
            document.getElementById('alumnoEdadInfo').style.display = 'none';
        } else if (userRole === 'ROLE_ALUMNO') {
            roleBadge.innerHTML = '<span class="badge bg-success">Alumno</span>';
            roleText.textContent = 'Alumno';
            
            // Mostrar información del alumno
            if (usuario.alumno) {
                document.getElementById('viewAlumnoNombre').textContent = usuario.alumno.nombre || '-';
                document.getElementById('viewAlumnoCarrera').textContent = usuario.alumno.carrera || '-';
                document.getElementById('viewAlumnoEdad').textContent = usuario.alumno.edad || '-';
            }
        }
        
        // Asegurar que el modo vista esté visible y el modo edición oculto
        document.getElementById('viewMode').style.display = 'block';
        document.getElementById('editMode').style.display = 'none';
    },

    toggleEditMode() {
        console.log('✏️ toggleEditMode ejecutado');
        
        const viewMode = document.getElementById('viewMode');
        const editMode = document.getElementById('editMode');
        const usuario = this.datosOriginales;
        
        if (!usuario) {
            console.log('No hay datos de usuario');
            return;
        }
        
        if (editMode.style.display === 'block') {
            // Volver a modo vista
            viewMode.style.display = 'block';
            editMode.style.display = 'none';
        } else {
            // Cargar datos en el formulario de edición
            document.getElementById('editId').value = usuario.id || '';
            document.getElementById('editUsername').value = usuario.username || '';
            document.getElementById('editEmail').value = usuario.email || '';
            document.getElementById('editPassword').value = '';
            
            const userRole = usuario.role;
            const editAlumnoFields = document.getElementById('editAlumnoFields');
            const editCarreraFields = document.getElementById('editCarreraFields');
            const editEdadFields = document.getElementById('editEdadFields');
            
            if (userRole === 'ROLE_ALUMNO' && usuario.alumno) {
                if (editAlumnoFields) editAlumnoFields.style.display = 'block';
                if (editCarreraFields) editCarreraFields.style.display = 'block';
                if (editEdadFields) editEdadFields.style.display = 'block';
                document.getElementById('editAlumnoNombre').value = usuario.alumno.nombre || '';
                document.getElementById('editAlumnoCarrera').value = usuario.alumno.carrera || '';
                document.getElementById('editAlumnoEdad').value = usuario.alumno.edad || '';
            } else {
                if (editAlumnoFields) editAlumnoFields.style.display = 'none';
                if (editCarreraFields) editCarreraFields.style.display = 'none';
                if (editEdadFields) editEdadFields.style.display = 'none';
            }
            
            // Mostrar formulario
            viewMode.style.display = 'none';
            editMode.style.display = 'block';
        }
    },

	async guardarPerfil() {
	    console.log('💾 guardarPerfil ejecutado');
	    
	    const token = auth.getToken();
	    const userRole = this.datosOriginales.role;
	    
	    // ✅ Enviar solo los datos que pueden cambiar
	    const updateData = {
	        email: document.getElementById('editEmail').value,
	        nombreAlumno: document.getElementById('editAlumnoNombre').value,
	        carreraAlumno: document.getElementById('editAlumnoCarrera').value,
	        edadAlumno: parseInt(document.getElementById('editAlumnoEdad').value)
	    };
	    
	    // Solo incluir password si se proporcionó
	    const password = document.getElementById('editPassword').value;
	    if (password && password.trim() !== '') {
	        updateData.password = password;
	    }
	    
	    console.log('📤 Enviando actualización:', updateData);
	    
	    try {
	        const response = await fetch(`${this.apiBase}/me`, {
	            method: 'PUT',
	            headers: {
	                'Authorization': `Bearer ${token}`,
	                'Content-Type': 'application/json'
	            },
	            body: JSON.stringify(updateData)
	        });
	        
	        if (!response.ok) {
	            const error = await response.json();
	            throw new Error(error.message || 'Error al actualizar perfil');
	        }
	        
	        const data = await response.json();
	        alert('✅ Perfil actualizado correctamente');
	        
	        this.datosOriginales = data;
	        this.mostrarPerfil(data);
	        this.toggleEditMode();
	        
	    } catch (error) {
	        console.error('Error:', error);
	        alert('❌ Error al actualizar perfil: ' + error.message);
	    }
	}
	
	
	
	
};

// Funciones globales
window.cargarPerfil = () => perfil.cargarPerfil();
window.toggleEditMode = () => perfil.toggleEditMode();
window.guardarPerfil = () => perfil.guardarPerfil();
window.perfil = perfil;
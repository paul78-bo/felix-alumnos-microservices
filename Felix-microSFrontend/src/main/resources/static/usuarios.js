const usuarios = {
    apiBase: 'http://localhost:8080/api/usuarios',
    
    // Variables de paginación
    paginaActual: 0,
    tamanoPagina: 10,
    totalPaginas: 0,
    busquedaActual: '',

    async cargarUsuarios() {
        console.log('🔄 Cargando usuarios paginados...');
        
        const token = auth.getToken();
        if (!token) {
            window.location.href = 'login.html';
            return;
        }

        let url = `${this.apiBase}/paginado?page=${this.paginaActual}&size=${this.tamanoPagina}`;
        if (this.busquedaActual) {
            url += `&busqueda=${encodeURIComponent(this.busquedaActual)}`;
        }

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: { 
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });

            if (response.status === 401 || response.status === 403) {
                alert('No tienes permisos para ver usuarios');
                window.location.href = 'alumnos.html';
                return;
            }

            if (!response.ok) throw new Error('Error al cargar usuarios');

            const data = await response.json();
            this.mostrarUsuarios(data.content);
            this.actualizarPaginacion(data);
            
        } catch (error) {
            console.error('❌ Error:', error);
            this.mostrarError('Error al cargar usuarios');
        }
    },

    actualizarPaginacion(data) {
        this.totalPaginas = data.totalPages;
        this.paginaActual = data.currentPage;
        
        const paginacionDiv = document.getElementById('paginacion');
        if (!paginacionDiv) return;
        
        let html = '<div class="d-flex justify-content-center gap-2 mt-3">';
        html += `<button class="btn btn-sm btn-outline-primary" onclick="usuarios.irPagina(0)" ${this.paginaActual === 0 ? 'disabled' : ''}>Primera</button>`;
        html += `<button class="btn btn-sm btn-outline-primary" onclick="usuarios.irPagina(${this.paginaActual - 1})" ${this.paginaActual === 0 ? 'disabled' : ''}>Anterior</button>`;
        html += `<span class="align-self-center mx-2">Página ${this.paginaActual + 1} de ${this.totalPaginas}</span>`;
        html += `<button class="btn btn-sm btn-outline-primary" onclick="usuarios.irPagina(${this.paginaActual + 1})" ${this.paginaActual + 1 >= this.totalPaginas ? 'disabled' : ''}>Siguiente</button>`;
        html += `<button class="btn btn-sm btn-outline-primary" onclick="usuarios.irPagina(${this.totalPaginas - 1})" ${this.paginaActual + 1 >= this.totalPaginas ? 'disabled' : ''}>Última</button>`;
        html += '</div>';
        
        paginacionDiv.innerHTML = html;
    },

    irPagina(pagina) {
        if (pagina >= 0 && pagina < this.totalPaginas) {
            this.paginaActual = pagina;
            this.cargarUsuarios();
        }
    },

    async buscar(termino) {
        this.busquedaActual = termino.trim();
        this.paginaActual = 0;
        this.cargarUsuarios();
    },

    mostrarUsuarios(usuariosList) {
        const tbody = document.getElementById('usuariosTable');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!usuariosList || usuariosList.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="fas fa-users-slash me-2"></i>
                        No hay usuarios registrados
                    </td>
                </tr>`;
            return;
        }

        usuariosList.forEach(usuario => {
            const rolClass = {
                'ROLE_ADMIN': 'danger',
                'ROLE_PROFESOR': 'warning',
                'ROLE_ALUMNO': 'success'
            }[usuario.role] || 'secondary';

            const rolText = {
                'ROLE_ADMIN': 'Administrador',
                'ROLE_PROFESOR': 'Profesor',
                'ROLE_ALUMNO': 'Alumno'
            }[usuario.role] || usuario.role;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${usuario.id}</td>
                <td><strong>${usuario.username}</strong></td>
                <td><a href="mailto:${usuario.email}">${usuario.email}</a></td>
                <td><span class="badge bg-${rolClass}">${rolText}</span></td>
                <td>
                    ${usuario.enabled ? 
                        '<span class="badge bg-success"><i class="fas fa-check-circle me-1"></i>Activo</span>' : 
                        '<span class="badge bg-secondary"><i class="fas fa-ban me-1"></i>Inactivo</span>'
                    }
                </td>
                <td>
                    ${usuario.alumnoId ? 
                        `<span class="badge bg-info">ID: ${usuario.alumnoId}</span>` : 
                        '<span class="text-muted">-</span>'
                    }
                </td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-warning" onclick="usuarios.editar(${usuario.id})" title="Editar">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-outline-info" onclick="usuarios.mostrarModalRol(${usuario.id}, '${usuario.username}')" title="Cambiar Rol">
                            <i class="fas fa-user-tag"></i>
                        </button>
                        <button class="btn btn-outline-${usuario.enabled ? 'secondary' : 'success'}" 
                                onclick="usuarios.cambiarEstado(${usuario.id}, ${!usuario.enabled})" 
                                title="${usuario.enabled ? 'Desactivar' : 'Activar'}">
                            <i class="fas fa-${usuario.enabled ? 'ban' : 'check'}"></i>
                        </button>
                        <button class="btn btn-outline-danger" onclick="usuarios.eliminar(${usuario.id}, '${usuario.username}')" title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    },

    initForm() {
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');
        
        if (id) {
            document.getElementById('formTitle').textContent = 'Editar Usuario';
            document.getElementById('passwordOptional').textContent = ' (dejar vacío para no cambiar)';
            document.getElementById('passwordHelp').textContent = 'Solo si deseas cambiar la contraseña';
            this.cargarUsuarioParaEditar(id);
        }

        document.getElementById('role').addEventListener('change', (e) => {
            document.getElementById('alumnoFields').style.display = 
                e.target.value === 'ROLE_ALUMNO' ? 'block' : 'none';
        });
    },

    async cargarUsuarioParaEditar(id) {
        try {
            const token = auth.getToken();
            const response = await fetch(`${this.apiBase}/${id}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) throw new Error('Error al cargar usuario');

            const usuario = await response.json();
            
            document.getElementById('usuarioId').value = usuario.id;
            document.getElementById('username').value = usuario.username;
            document.getElementById('email').value = usuario.email;
            document.getElementById('role').value = usuario.role;
            document.getElementById('enabled').checked = usuario.enabled;

            if (usuario.alumno) {
                document.getElementById('nombreAlumno').value = usuario.alumno.nombre;
                document.getElementById('edadAlumno').value = usuario.alumno.edad;
                document.getElementById('carreraAlumno').value = usuario.alumno.carrera;
                document.getElementById('alumnoFields').style.display = 'block';
            }

        } catch (error) {
            alert('Error al cargar usuario');
            window.location.href = 'usuarios.html';
        }
    },

    async guardar() {
        const id = document.getElementById('usuarioId').value;
        const esEdicion = !!id;

        const usuario = {
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            role: document.getElementById('role').value,
            enabled: document.getElementById('enabled').checked
        };

        const password = document.getElementById('password').value;
        if (password) {
            usuario.password = password;
        }

        if (usuario.role === 'ROLE_ALUMNO') {
            usuario.nombreAlumno = document.getElementById('nombreAlumno').value;
            usuario.edadAlumno = document.getElementById('edadAlumno').value;
            usuario.carreraAlumno = document.getElementById('carreraAlumno').value;
        }

        if (!usuario.username || !usuario.email || !usuario.role) {
            alert('Todos los campos son obligatorios');
            return;
        }

        if (!esEdicion && !password) {
            alert('La contraseña es obligatoria para nuevos usuarios');
            return;
        }

        try {
            const token = auth.getToken();
            const url = esEdicion ? `${this.apiBase}/${id}` : this.apiBase;
            const method = esEdicion ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(usuario)
            });

            if (response.status === 403) {
                alert('No tienes permisos para realizar esta acción');
                return;
            }

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error);
            }

            alert(esEdicion ? 'Usuario actualizado' : 'Usuario creado');
            window.location.href = 'usuarios.html';

        } catch (error) {
            alert('Error: ' + error.message);
        }
    },

    async eliminar(id, username) {
        if (!confirm(`¿Eliminar al usuario ${username}?`)) return;

        try {
            const token = auth.getToken();
            const response = await fetch(`${this.apiBase}/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) throw new Error('Error al eliminar');

            alert('Usuario desactivado');
            this.cargarUsuarios();

        } catch (error) {
            alert('Error al eliminar usuario');
        }
    },

    mostrarModalRol(id, username) {
        document.getElementById('cambiarRolUserId').value = id;
        document.getElementById('cambiarRolUsername').textContent = username;
        new bootstrap.Modal(document.getElementById('cambiarRolModal')).show();
    },

    async guardarCambioRol() {
        const id = document.getElementById('cambiarRolUserId').value;
        const nuevoRol = document.getElementById('nuevoRol').value;

        try {
            const token = auth.getToken();
            const response = await fetch(`${this.apiBase}/${id}/rol`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ rol: nuevoRol })
            });

            if (!response.ok) throw new Error('Error al cambiar rol');

            bootstrap.Modal.getInstance(document.getElementById('cambiarRolModal')).hide();
            alert('Rol actualizado');
            this.cargarUsuarios();

        } catch (error) {
            alert('Error al cambiar rol');
        }
    },

    async cambiarEstado(id, nuevoEstado) {
        try {
            const token = auth.getToken();
            const response = await fetch(`${this.apiBase}/${id}/estado`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ activo: nuevoEstado })
            });

            if (!response.ok) throw new Error('Error al cambiar estado');

            alert(nuevoEstado ? 'Usuario activado' : 'Usuario desactivado');
            this.cargarUsuarios();

        } catch (error) {
            alert('Error al cambiar estado');
        }
    },

    filtrar() {
        const searchUsername = document.getElementById('searchUsername')?.value || '';
        const searchEmail = document.getElementById('searchEmail')?.value || '';
        
        let busqueda = '';
        if (searchUsername) {
            busqueda = searchUsername;
        } else if (searchEmail) {
            busqueda = searchEmail;
        }
        
        this.buscar(busqueda);
    },

    editar(id) {
        window.location.href = `form-usuario.html?id=${id}`;
    },

    mostrarError(msg) {
        const alertContainer = document.getElementById('alertContainer') || (() => {
            const div = document.createElement('div');
            div.id = 'alertContainer';
            div.className = 'container mt-3';
            const container = document.querySelector('.container');
            if (container) container.prepend(div);
            return div;
        })();

        alertContainer.innerHTML = `
            <div class="alert alert-danger alert-dismissible fade show">
                <strong>Error:</strong> ${msg}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    },

    limpiarFiltroUsername() {
        const input = document.getElementById('searchUsername');
        if (input) {
            input.value = '';
            this.buscar('');
        }
    },

    limpiarFiltroEmail() {
        const input = document.getElementById('searchEmail');
        if (input) {
            input.value = '';
            this.buscar('');
        }
    },

    limpiarFiltros() {
        this.buscar('');
        const input1 = document.getElementById('searchUsername');
        const input2 = document.getElementById('searchEmail');
        if (input1) input1.value = '';
        if (input2) input2.value = '';
    }
};

window.usuarios = usuarios;
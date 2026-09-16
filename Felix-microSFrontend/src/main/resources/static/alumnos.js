const alumnos = {
    apiBase: 'http://localhost:8080/api/alumnos',
    
    modal: null,
    cache: {
        lista: null,
        timestamp: null,
        validez: 5 * 60 * 1000
    },
    busquedaActual: '',
    
    // ✅ Variables de paginación (declaradas correctamente como propiedades del objeto)
    paginaActual: 0,
    tamanoPagina: 10,
    totalPaginas: 0,

    init() {
        console.log('📚 Inicializando módulo de alumnos');
        
        if (typeof auth === 'undefined') {
            console.error('❌ ERROR: auth.js no está cargado antes que alumnos.js');
            setTimeout(() => {
                if (typeof auth !== 'undefined') {
                    console.log('✅ auth.js cargado tardíamente, continuando...');
                    this.continuarInicializacion();
                } else {
                    console.error('❌ auth.js nunca se cargó');
                }
            }, 1000);
            return;
        }
        
        this.continuarInicializacion();
    },
    
    continuarInicializacion() {
        const modalElement = document.getElementById('alumnoModal');
        if (modalElement) {
            this.modal = new bootstrap.Modal(modalElement);
            modalElement.addEventListener('hidden.bs.modal', () => this.limpiarFormulario());
            console.log('✅ Modal inicializado');
        }
    },

    async cargarAlumnos() {
        console.log('🔄 Cargando alumnos paginados...');
        
        const token = auth.getToken();
        if (!token) return;
        
        let url = `${this.apiBase}/paginado?page=${this.paginaActual}&size=${this.tamanoPagina}`;
        if (this.busquedaActual) {
            url += `&busqueda=${encodeURIComponent(this.busquedaActual)}`;
        }
        
        try {
            const response = await fetch(url, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            if (response.ok) {
                const data = await response.json();
                this.mostrarAlumnos(data.content);
                this.actualizarPaginacion(data);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    },

    actualizarPaginacion(data) {
        this.totalPaginas = data.totalPages;
        this.paginaActual = data.currentPage;
        
        const paginacionDiv = document.getElementById('paginacion');
        if (!paginacionDiv) return;
        
        let html = '<div class="d-flex justify-content-center gap-2 mt-3">';
        html += `<button class="btn btn-sm btn-outline-primary" onclick="alumnos.irPagina(0)" ${this.paginaActual === 0 ? 'disabled' : ''}>Primera</button>`;
        html += `<button class="btn btn-sm btn-outline-primary" onclick="alumnos.irPagina(${this.paginaActual - 1})" ${this.paginaActual === 0 ? 'disabled' : ''}>Anterior</button>`;
        html += `<span class="align-self-center mx-2">Página ${this.paginaActual + 1} de ${this.totalPaginas}</span>`;
        html += `<button class="btn btn-sm btn-outline-primary" onclick="alumnos.irPagina(${this.paginaActual + 1})" ${this.paginaActual + 1 >= this.totalPaginas ? 'disabled' : ''}>Siguiente</button>`;
        html += `<button class="btn btn-sm btn-outline-primary" onclick="alumnos.irPagina(${this.totalPaginas - 1})" ${this.paginaActual + 1 >= this.totalPaginas ? 'disabled' : ''}>Última</button>`;
        html += '</div>';
        
        paginacionDiv.innerHTML = html;
    },

    irPagina(pagina) {
        if (pagina >= 0 && pagina < this.totalPaginas) {
            this.paginaActual = pagina;
            this.cargarAlumnos();
        }
    },

    buscar(termino) {
        console.log('🔍 Buscando:', termino);
        this.busquedaActual = termino.trim();
        this.paginaActual = 0;
        this.cargarAlumnos();
    },

    limpiarBusqueda() {
        this.busquedaActual = '';
        this.paginaActual = 0;
        const input = document.getElementById('buscarAlumno');
        if (input) input.value = '';
        this.cargarAlumnos();
        console.log('🧹 Búsqueda limpiada');
    },

    mostrarAlumnos(alumnosList) {
        console.log('📋 Mostrando', alumnosList.length, 'alumnos en la tabla');
        
        const tbody = document.getElementById('alumnosTable');
        if (!tbody) {
            console.error('❌ No se encontró tbody#alumnosTable');
            return;
        }
        
        tbody.innerHTML = '';
        
        const puedeCrear = auth.tienePermiso('crear_alumno');
        const puedeEditar = auth.tienePermiso('editar_alumno');
        const puedeEliminar = auth.tienePermiso('eliminar_alumno');
        
        const adminActions = document.getElementById('adminActions');
        const accionesHeader = document.getElementById('accionesHeader');
        
        if (adminActions) adminActions.style.display = puedeCrear ? 'block' : 'none';
        if (accionesHeader) accionesHeader.style.display = (puedeEditar || puedeEliminar) ? 'table-cell' : 'none';

        if (!alumnosList || alumnosList.length === 0) {
            const mensaje = this.busquedaActual 
                ? `No se encontraron resultados para "${this.busquedaActual}"`
                : 'No hay alumnos registrados';
            
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">
                        <i class="fas fa-users-slash me-2"></i>
                        ${mensaje}
                    </td>
                </tr>`;
            return;
        }

        alumnosList.forEach(alumno => {
            const tr = document.createElement('tr');
            let accionesHTML = '<td>-<\/td>';
            
            if (puedeEditar || puedeEliminar) {
                accionesHTML = `<td><div class="btn-group btn-group-sm">`;
                if (puedeEditar) {
                    accionesHTML += `
                        <button class="btn btn-outline-warning" 
                                onclick="alumnos.editar(${alumno.id})"
                                title="Editar">
                            <i class="fas fa-edit"></i>
                        </button>`;
                }
                if (puedeEliminar) {
                    accionesHTML += `
                        <button class="btn btn-outline-danger" 
                                onclick="alumnos.eliminar(${alumno.id})"
                                title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>`;
                }
                accionesHTML += '</div></td>';
            }

            let nombre = alumno.nombre || '';
            let email = alumno.email || '';
            let carrera = alumno.carrera || '';
            
            if (this.busquedaActual) {
                const termino = this.busquedaActual.toLowerCase();
                nombre = this.resaltarTexto(nombre, termino);
                email = this.resaltarTexto(email, termino);
                carrera = this.resaltarTexto(carrera, termino);
            }

            tr.innerHTML = `
                <tr>${alumno.id}</td>
                <td>${nombre}</td>
                <td><a href="mailto:${alumno.email}">${email}</a></td>
                <td>${alumno.edad || ''}</td>
                <td>${carrera}</td>
                ${accionesHTML}
            `;
            tbody.appendChild(tr);
        });
        
        console.log('✅ Tabla actualizada correctamente');
    },

    resaltarTexto(texto, termino) {
        if (!texto || !termino) return texto;
        const regex = new RegExp(`(${termino})`, 'gi');
        return texto.replace(regex, '<span class="search-highlight">$1</span>');
    },

    mostrarFormulario(alumno = null) {
        console.log('📝 Mostrando formulario para:', alumno ? 'EDITAR' : 'NUEVO');
        
        if (!alumno && !auth.tienePermiso('crear_alumno')) {
            alert('❌ No tiene permisos para crear alumnos');
            return;
        }
        if (alumno && !auth.tienePermiso('editar_alumno')) {
            alert('❌ No tiene permisos para editar alumnos');
            return;
        }
        
        document.getElementById('modalTitleText').textContent = alumno ? 'Editar Alumno' : 'Nuevo Alumno';
        document.getElementById('btnGuardar').textContent = alumno ? 'Actualizar' : 'Guardar';
        document.getElementById('btnGuardar').disabled = false;

        if (alumno) {
            document.getElementById('alumnoId').value = alumno.id;
            document.getElementById('alumnoNombre').value = alumno.nombre;
            document.getElementById('alumnoEmail').value = alumno.email;
            document.getElementById('alumnoEdad').value = alumno.edad;
            document.getElementById('alumnoCarrera').value = alumno.carrera;
        } else {
            this.limpiarFormulario();
        }

        if (this.modal) {
            this.modal.show();
        }
    },

    limpiarFormulario() {
        const form = document.getElementById('alumnoForm');
        if (form) form.reset();
        document.getElementById('alumnoId').value = '';
    },

    async guardar() {
        console.log('💾 Iniciando guardado de alumno...');
        
        const btnGuardar = document.getElementById('btnGuardar');
        const originalText = btnGuardar.textContent;
        
        try {
            btnGuardar.disabled = true;
            btnGuardar.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i> Guardando...';

            const alumno = {
                id: document.getElementById('alumnoId').value || null,
                nombre: document.getElementById('alumnoNombre').value.trim(),
                email: document.getElementById('alumnoEmail').value.trim(),
                edad: parseInt(document.getElementById('alumnoEdad').value),
                carrera: document.getElementById('alumnoCarrera').value.trim()
            };

            if (!alumno.nombre || !alumno.email || !alumno.edad || !alumno.carrera) {
                alert('⚠️ Complete todos los campos');
                return;
            }

            if (isNaN(alumno.edad) || alumno.edad < 16 || alumno.edad > 80) {
                alert('⚠️ Edad debe ser entre 16 y 80 años');
                return;
            }

            const token = auth.getToken();
            if (!token) {
                alert('❌ Sesión expirada');
                auth.logout();
                return;
            }

            const url = alumno.id ? `${this.apiBase}/${alumno.id}` : this.apiBase;
            const method = alumno.id ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method, 
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(alumno)
            });

            if (response.status === 401) {
                alert('❌ Sesión expirada');
                auth.logout();
                return;
            }
            
            if (response.status === 409) {
                alert('❌ El email ya está registrado');
                return;
            }

            if (!response.ok) {
                alert(`❌ Error ${response.status}: No se pudo guardar el alumno`);
                return;
            }

            this.cache.lista = null;
            
            if (this.modal) {
                this.modal.hide();
            }
            
            alert('✅ Alumno guardado correctamente');
            this.cargarAlumnos();
            
        } catch (e) {
            console.error('❌ Error:', e);
            alert(`❌ Error de conexión: ${e.message}`);
        } finally {
            btnGuardar.disabled = false;
            btnGuardar.textContent = originalText;
            btnGuardar.innerHTML = `<i class="fas fa-save me-1"></i> ${originalText}`;
        }
    },
    
    async editar(id) {
        try {
            const token = auth.getToken();
            if (!token) {
                alert('❌ Sesión expirada');
                return;
            }

            const response = await fetch(`${this.apiBase}/${id}`, {
                headers: { 
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });
            
            if (!response.ok) {
                if (response.status === 404) {
                    alert('❌ Alumno no encontrado');
                    this.cargarAlumnos();
                    return;
                }
                throw new Error(`HTTP ${response.status}`);
            }
            
            const data = await response.json();
            this.mostrarFormulario(data);
        } catch (e) {
            console.error('❌ Error:', e);
            this.mostrarError('Error al cargar alumno: ' + e.message);
        }
    },

    async eliminar(id) {
        if (!confirm('¿Está seguro de eliminar este alumno?')) return;
        
        try {
            const token = auth.getToken();
            if (!token) {
                alert('❌ Sesión expirada');
                return;
            }

            const response = await fetch(`${this.apiBase}/${id}`, {
                method: 'DELETE', 
                headers: { 
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });
            
            if (!response.ok) {
                if (response.status === 404) {
                    alert('❌ Alumno no encontrado');
                } else {
                    throw new Error(`HTTP ${response.status}`);
                }
                return;
            }
            
            this.cache.lista = null;
            alert('✅ Alumno eliminado correctamente');
            this.cargarAlumnos();
            
        } catch (e) {
            console.error('❌ Error:', e);
            this.mostrarError('Error al eliminar alumno: ' + e.message);
        }
    },

    mostrarError(msg) {
        console.error('❌ Mostrando error:', msg);
        const alertContainer = document.getElementById('alertContainer');
        if (alertContainer) {
            const alertDiv = document.createElement('div');
            alertDiv.className = 'alert alert-danger alert-dismissible fade show';
            alertDiv.innerHTML = `<strong>Error:</strong> ${msg}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
            alertContainer.appendChild(alertDiv);
            setTimeout(() => alertDiv.remove(), 5000);
        } else {
            alert('❌ ' + msg);
        }
    }
};

if (typeof auth !== 'undefined') {
    console.log('🚀 Módulo de alumnos cargado');
    window.alumnos = alumnos;
    
    document.addEventListener('DOMContentLoaded', () => {
        alumnos.init();
        alumnos.cargarAlumnos();  // ✅ Cargar alumnos al inicio
    });
}
const tareas = {
    apiBase: 'http://localhost:8080/api/tareas',
    alumnosBase: 'http://localhost:8080/api/alumnos',
    
    // Variables de paginación
    paginaActual: 0,
    tamanoPagina: 10,
    totalPaginas: 0,
    busquedaTitulo: '',
    busquedaAlumno: '',
    filtroEstado: '',

    async cargarTareas() {
        console.log('🔄 Cargando tareas paginadas...');
        
        const token = auth.getToken();
        if (!token) {
            window.location.href = 'login.html';
            return;
        }

        let url = `${this.apiBase}/paginado?page=${this.paginaActual}&size=${this.tamanoPagina}`;
        if (this.busquedaTitulo) {
            url += `&titulo=${encodeURIComponent(this.busquedaTitulo)}`;
        }
        if (this.busquedaAlumno) {
            url += `&alumno=${encodeURIComponent(this.busquedaAlumno)}`;
        }
        if (this.filtroEstado) {
            url += `&estado=${encodeURIComponent(this.filtroEstado)}`;
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
                alert('No tienes permisos para ver tareas');
                window.location.href = 'alumnos.html';
                return;
            }

            if (!response.ok) throw new Error('Error al cargar tareas');

            const data = await response.json();
            this.mostrarTareas(data.content);
            this.actualizarPaginacion(data);
            
        } catch (error) {
            console.error('❌ Error:', error);
            this.mostrarError('Error al cargar tareas');
        }
    },

    actualizarPaginacion(data) {
        this.totalPaginas = data.totalPages;
        this.paginaActual = data.currentPage;
        
        const paginacionDiv = document.getElementById('paginacion');
        if (!paginacionDiv) return;
        
        let html = '<div class="d-flex justify-content-center gap-2 mt-3">';
        html += `<button class="btn btn-sm btn-outline-primary" onclick="tareas.irPagina(0)" ${this.paginaActual === 0 ? 'disabled' : ''}>Primera</button>`;
        html += `<button class="btn btn-sm btn-outline-primary" onclick="tareas.irPagina(${this.paginaActual - 1})" ${this.paginaActual === 0 ? 'disabled' : ''}>Anterior</button>`;
        html += `<span class="align-self-center mx-2">Página ${this.paginaActual + 1} de ${this.totalPaginas}</span>`;
        html += `<button class="btn btn-sm btn-outline-primary" onclick="tareas.irPagina(${this.paginaActual + 1})" ${this.paginaActual + 1 >= this.totalPaginas ? 'disabled' : ''}>Siguiente</button>`;
        html += `<button class="btn btn-sm btn-outline-primary" onclick="tareas.irPagina(${this.totalPaginas - 1})" ${this.paginaActual + 1 >= this.totalPaginas ? 'disabled' : ''}>Última</button>`;
        html += '</div>';
        
        paginacionDiv.innerHTML = html;
    },

    irPagina(pagina) {
        if (pagina >= 0 && pagina < this.totalPaginas) {
            this.paginaActual = pagina;
            this.cargarTareas();
        }
    },

    async cargarTareasSinPaginacion() {
        console.log('🔄 Cargando tareas sin paginación...');
        
        const token = auth.getToken();
        if (!token) {
            window.location.href = 'login.html';
            return;
        }

        try {
            let url = this.apiBase;
            const userRole = sessionStorage.getItem('user_role');
            
            if (userRole === 'ROLE_ALUMNO') {
                url = `${this.apiBase}/mis-tareas`;
                console.log('👨‍🎓 Alumno - Cargando SOLO sus tareas');
            } else {
                console.log('👨‍🏫 Admin/Profesor - Cargando TODAS las tareas');
            }
            
            const response = await fetch(url, {
                method: 'GET',
                headers: { 
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });

            if (response.status === 401 || response.status === 403) {
                alert('No tienes permisos para ver tareas');
                window.location.href = 'alumnos.html';
                return;
            }

            if (!response.ok) throw new Error('Error al cargar tareas');

            const data = await response.json();
            this.mostrarTareas(data);
            
        } catch (error) {
            console.error('❌ Error:', error);
            this.mostrarError('Error al cargar tareas');
        }
    },

    mostrarTareas(tareasList) {
        const tbody = document.getElementById('tareasTable');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!tareasList || tareasList.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center text-muted py-4">
                        <i class="fas fa-tasks-slash me-2"></i>
                        No hay tareas registradas
                    </td>
                </tr>`;
            return;
        }

        const userRole = sessionStorage.getItem('user_role');
        const esAlumno = userRole === 'ROLE_ALUMNO';
        const esProfesorOAdmin = userRole === 'ROLE_ADMIN' || userRole === 'ROLE_PROFESOR';

        tareasList.forEach(tarea => {
            const fecha = new Date(tarea.fechaSubida).toLocaleDateString();
            const calificacion = tarea.calificacion ? tarea.calificacion.toFixed(1) : '-';
            
            const nombreEntrega = tarea.archivoEntrega ? 
                tarea.archivoEntrega.substring(0, 30) + (tarea.archivoEntrega.length > 30 ? '...' : '') : 
                'No entregada';
            
            let estadoTexto = '';
            let estadoColor = '';
            
            if (tarea.calificacion) {
                estadoTexto = 'Calificada';
                estadoColor = 'success';
            } else if (tarea.archivoEntrega) {
                estadoTexto = 'Entregada';
                estadoColor = 'info';
            } else {
                estadoTexto = 'Pendiente';
                estadoColor = 'warning';
            }
            const estadoBadge = `<span class="badge bg-${estadoColor}">${estadoTexto}</span>`;

            const tr = document.createElement('tr');
            
            const columnaAlumno = esAlumno ? '' : `<td>${tarea.alumnoNombre}</td>`;
            const yaDescargo = localStorage.getItem(`descargado_${tarea.id}`) === 'true';
            
            const mostrarDescargar = esProfesorOAdmin || (esAlumno && tarea.tipoTarea === 'ENUNCIADO' && !tarea.archivoEntrega);
            const mostrarEntregar = esAlumno && tarea.tipoTarea === 'ENUNCIADO' && !tarea.archivoEntrega && yaDescargo;
            const mostrarVerEntrega = esProfesorOAdmin && tarea.archivoEntrega;
            
            tr.innerHTML = `
                <td>${tarea.id}${esAlumno ? ' (Mi tarea)' : ''}</td>
                <td><a href="tarea-detalle.html?id=${tarea.id}">${tarea.titulo}</a></td>
                ${columnaAlumno}
                <td class="text-center">${nombreEntrega}</td>
                <td>${fecha}</td>
                <td class="text-center"><strong>${calificacion}</strong></td>
                <td>${estadoBadge}</td>
                <td>
                    <div class="btn-group btn-group-sm">
                        ${mostrarDescargar ? `
                        <button class="btn btn-outline-primary btn-descargar" data-id="${tarea.id}" title="Descargar enunciado">
                            <i class="fas fa-download"></i> Descargar mi Tarea
                        </button>
                        ` : ''}
                        ${mostrarEntregar ? `
                        <button class="btn btn-outline-success btn-entregar" data-id="${tarea.id}" title="Entregar tarea">
                            <i class="fas fa-upload"></i> Entregar Tarea
                        </button>
                        ` : ''}
                        ${mostrarVerEntrega ? `
                        <button class="btn btn-outline-info btn-ver-entrega" data-id="${tarea.id}" title="Ver entrega del alumno">
                            <i class="fas fa-eye"></i> Ver Entrega
                        </button>
                        ` : ''}
                        ${esProfesorOAdmin ? `
                        <button class="btn btn-outline-warning btn-calificar" data-id="${tarea.id}" title="Calificar">
                            <i class="fas fa-star"></i>
                        </button>
                        <button class="btn btn-outline-danger btn-eliminar" data-id="${tarea.id}" title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                        ` : ''}
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });

        this.configurarEventos(esAlumno);
    },

    configurarEventos(esAlumno) {
        // Evento: Descargar tarea
        document.querySelectorAll('.btn-descargar').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-id');
                
                if (esAlumno) {
                    localStorage.setItem(`descargado_${id}`, 'true');
                }
                
                await this.descargarEnunciado(id);
                
                if (esAlumno) {
                    setTimeout(() => this.cargarTareas(), 500);
                }
            });
        });

        // Evento: Entregar tarea
        document.querySelectorAll('.btn-entregar').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-id');
                window.location.href = `entregar-tarea.html?id=${id}`;
            });
        });

        // Evento: Ver entrega
        document.querySelectorAll('.btn-ver-entrega').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-id');
                console.log('👁️ Ver Entrega desde tareas.html, ID:', id);
                
                const originalHtml = btn.innerHTML;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generando...';
                btn.disabled = true;
                
                const url = await this.generarLinkYVerEntrega(id);
                
                btn.innerHTML = originalHtml;
                btn.disabled = false;
                
                if (url) {
                    window.open(url, '_blank');
                } else {
                    alert('No se pudo generar el enlace para ver la entrega');
                }
            });
        });

        // Evento: Calificar
        document.querySelectorAll('.btn-calificar').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-id');
                window.location.href = `tarea-detalle.html?id=${id}`;
            });
        });

        // Evento: Eliminar
        document.querySelectorAll('.btn-eliminar').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-id');
                this.eliminar(id);
            });
        });
    },

    async guardar() {
        const titulo = document.getElementById('titulo').value;
        const descripcion = document.getElementById('descripcion').value;
        const archivo = document.getElementById('archivo').files[0];
        const userRole = sessionStorage.getItem('user_role');
        const esAlumno = userRole === 'ROLE_ALUMNO';
        
        let alumnoId;
        if (esAlumno) {
            alumnoId = await this.obtenerAlumnoIdPorUsuario();
            console.log('📚 Alumno ID obtenido:', alumnoId);
        } else {
            alumnoId = document.getElementById('alumnoId').value;
        }

        if (!titulo || !archivo || !alumnoId) {
            alert('Complete todos los campos obligatorios');
            return;
        }

        const formData = new FormData();
        formData.append('titulo', titulo);
        formData.append('descripcion', descripcion || '');
        formData.append('alumnoId', alumnoId);
        formData.append('archivo', archivo);
        
        if (esAlumno) {
            formData.append('tipoTarea', 'ENTREGA');
        }

        const token = auth.getToken();

        try {
            const response = await fetch(this.apiBase, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (response.status === 201) {
                const data = await response.json();
                console.log('✅ Tarea creada:', data);
                alert(esAlumno ? '✅ Tarea entregada correctamente' : '✅ Tarea subida correctamente');
                window.location.href = 'tareas.html';
            } else if (response.status === 400) {
                const error = await response.json();
                alert('❌ Error: ' + (error.errors?.archivo || 'Archivo inválido'));
            } else {
                const error = await response.text();
                console.error('Error response:', error);
                alert('❌ Error al subir la tarea');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('❌ Error de conexión');
        }
    },

    async cargarAlumnos() {
        const token = auth.getToken();
        try {
            const response = await fetch(this.alumnosBase, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            if (response.ok) {
                const alumnos = await response.json();
                const select = document.getElementById('alumnoId');
                const alumnoField = document.getElementById('alumnoField');
                
                if (!select) return;
                
                const userRole = sessionStorage.getItem('user_role');
                
                if (userRole === 'ROLE_ALUMNO') {
                    if (alumnoField) alumnoField.style.display = 'none';
                } else {
                    alumnos.forEach(alumno => {
                        const option = document.createElement('option');
                        option.value = alumno.id;
                        option.textContent = `${alumno.nombre} (${alumno.email})`;
                        select.appendChild(option);
                    });
                }
            }
        } catch (error) {
            console.error('Error cargando alumnos:', error);
        }
    },

    async obtenerAlumnoIdPorUsuario() {
        const token = auth.getToken();
        try {
            const response = await fetch('http://localhost:8080/api/usuarios/me', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                const usuario = await response.json();
                if (usuario.alumno && usuario.alumno.id) {
                    return usuario.alumno.id;
                }
            }
        } catch (error) {
            console.error('Error obteniendo alumno ID:', error);
        }
        return 1;
    },

    async eliminar(id) {
        if (!confirm('¿Está seguro de eliminar esta tarea?')) return;
        const token = auth.getToken();
        const tareaId = id || document.getElementById('tareaId')?.value;
        try {
            const response = await fetch(`${this.apiBase}/${tareaId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                alert('✅ Tarea eliminada');
                window.location.href = 'tareas.html';
            } else {
                alert('❌ Error al eliminar tarea');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('❌ Error de conexión');
        }
    },

    async descargarEnunciado(id) {
        console.log('📄 Descargando enunciado de tarea ID:', id);
        const token = auth.getToken();
        
        try {
            const response = await fetch(`${this.apiBase}/${id}/descargar`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            if (!response.ok) throw new Error('Error al descargar el enunciado');
            
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `enunciado_tarea_${id}.pdf`;
            
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            
            console.log('✅ Enunciado descargado');
            
        } catch (error) {
            console.error('Error:', error);
            alert('Error al descargar el enunciado');
        }
    },

    async descargarEntrega(id) {
        console.log('📥 Descargando entrega de tarea ID:', id);
        const token = auth.getToken();
        
        try {
            const response = await fetch(`${this.apiBase}/${id}/descargar-entrega`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            if (!response.ok) {
                if (response.status === 404) {
                    alert('El alumno aún no ha entregado esta tarea');
                } else {
                    throw new Error('Error al descargar la entrega');
                }
                return;
            }
            
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `entrega_tarea_${id}.pdf`;
            
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            
            console.log('✅ Entrega descargada');
            
        } catch (error) {
            console.error('Error:', error);
            alert('Error al descargar la entrega');
        }
    },

    initForm: async function() {
        console.log('🔧 Inicializando formulario de tareas...');
        
        const token = auth.getToken();
        if (!token) {
            window.location.href = 'login.html';
            return;
        }
        
        const userRole = sessionStorage.getItem('user_role');
        const esAlumno = userRole === 'ROLE_ALUMNO';
        
        const alumnoField = document.getElementById('alumnoField');
        if (alumnoField) {
            alumnoField.style.display = esAlumno ? 'none' : 'block';
        }
        
        if (!esAlumno) {
            await this.cargarAlumnos();
        }
        
        const form = document.getElementById('tareaForm');
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.guardar();
            });
        }
        
        const archivoInput = document.getElementById('archivo');
        if (archivoInput) {
            archivoInput.addEventListener('change', (e) => {
                const archivo = e.target.files[0];
                const fileNameSpan = document.getElementById('fileName');
                if (fileNameSpan && archivo) {
                    fileNameSpan.textContent = archivo.name;
                }
            });
        }
        
        console.log('✅ Formulario inicializado');
    },

    async cargarDetalle(id) {
        console.log('🔍 Cargando detalle de tarea ID:', id);
        
        const token = auth.getToken();
        if (!token) {
            alert('Sesión expirada');
            window.location.href = 'login.html';
            return;
        }
        
        try {
            const response = await fetch(`${this.apiBase}/${id}`, {
                method: 'GET',
                headers: { 
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'application/json'
                }
            });
            
            console.log('📥 Status respuesta:', response.status);
            
            if (!response.ok) {
                if (response.status === 401) {
                    alert('Sesión expirada');
                    auth.logout();
                    window.location.href = 'login.html';
                    return;
                }
                if (response.status === 404) {
                    alert('Tarea no encontrada');
                    window.location.href = 'tareas.html';
                    return;
                }
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }
            
            const tarea = await response.json();
            console.log('✅ Tarea cargada:', tarea);
            this.mostrarDetalle(tarea);
            
        } catch (error) {
            console.error('❌ Error al cargar tarea:', error);
            alert('Error al cargar la tarea: ' + error.message);
            window.location.href = 'tareas.html';
        }
    },

    mostrarDetalle(tarea) {
        console.log('📝 Mostrando detalle de tarea:', tarea);
        
        document.getElementById('tareaId').value = tarea.id;
        document.getElementById('detalleId').textContent = tarea.id;
        document.getElementById('detalleTitulo').textContent = tarea.titulo;
        document.getElementById('detalleDescripcion').textContent = tarea.descripcion || '-';
        document.getElementById('detalleFecha').textContent = new Date(tarea.fechaSubida).toLocaleString();
        document.getElementById('detalleFechaLimite').textContent = tarea.fechaLimite ? new Date(tarea.fechaLimite).toLocaleString() : 'No especificada';
        
        document.getElementById('detalleProfesorNombre').textContent = tarea.profesorNombre || 'No especificado';
        document.getElementById('detalleProfesorEmail').textContent = tarea.profesorEmail || '-';
        
        document.getElementById('detalleAlumnoNombre').textContent = tarea.alumnoNombre || '-';
        document.getElementById('detalleAlumnoEmail').textContent = tarea.alumnoEmail || '-';
        
        const btnImprimir = document.getElementById('btnImprimir');
        
        if (tarea.calificacion) {
            document.getElementById('detalleCalificacion').textContent = tarea.calificacion.toFixed(1);
            document.getElementById('detalleComentarios').textContent = tarea.comentarios || '-';
            if (btnImprimir) btnImprimir.style.display = 'inline-block';
        } else {
            document.getElementById('detalleCalificacion').textContent = '-';
            document.getElementById('detalleComentarios').textContent = '-';
            if (btnImprimir) btnImprimir.style.display = 'none';
        }

        const userRole = sessionStorage.getItem('user_role');
        const btnCalificar = document.getElementById('btnCalificar');
        if (btnCalificar) {
            btnCalificar.style.display = (userRole === 'ROLE_ADMIN' || userRole === 'ROLE_PROFESOR') ? 'inline-block' : 'none';
        }
        
        const accionesProfesor = document.getElementById('accionesProfesor');
        if (accionesProfesor && userRole === 'ROLE_ALUMNO') {
            accionesProfesor.style.display = 'none';
        }
		
        if (typeof window.mostrarEntregaAlumno === 'function') {
            window.mostrarEntregaAlumno(tarea);
        }
    },

    filtrar() {
        const searchTitulo = document.getElementById('searchTitulo')?.value || '';
        const searchAlumno = document.getElementById('searchAlumno')?.value || '';
        const filterEstado = document.getElementById('filterEstado')?.value || '';
        
        this.busquedaTitulo = searchTitulo;
        this.busquedaAlumno = searchAlumno;
        this.filtroEstado = filterEstado;
        this.paginaActual = 0;
        
        this.cargarTareas();
    },

    obtenerFechaDeFila(row) {
        const fechaIndex = 3;
        if (!row.cells[fechaIndex]) return new Date(0);
        const fechaTexto = row.cells[fechaIndex].textContent;
        const partes = fechaTexto.split('/');
        if (partes.length === 3) {
            return new Date(partes[2], partes[1] - 1, partes[0]);
        }
        return new Date(0);
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
        alertContainer.innerHTML = `<div class="alert alert-danger alert-dismissible fade show"><strong>Error:</strong> ${msg}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>`;
    },
	
    async generarLinkYVerEntrega(tareaId) {
        const token = auth.getToken();
        
        try {
            const response = await fetch(`http://localhost:8080/api/tareas/${tareaId}/generar-link-entrega`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) throw new Error('Error al generar link');
            
            const data = await response.json();
            const shortId = data.shortId;
            
            const url = `http://localhost:8081/api/tareas/acceso-entrega/${shortId}`;
            
            return url;
            
        } catch (error) {
            console.error('Error:', error);
            alert('Error al generar link de visualización');
            return null;
        }
    },
	
    imprimirCalificacion() {
        console.log('🖨️ Imprimir calificación ejecutado');
        try {
            const tareaId = document.getElementById('tareaId')?.value || 'N/A';
            const titulo = document.getElementById('detalleTitulo')?.innerText || 'Sin título';
            const alumnoNombre = document.getElementById('detalleAlumnoNombre')?.innerText || 'No especificado';
            const calificacion = document.getElementById('detalleCalificacion')?.innerText || '-';
            const comentarios = document.getElementById('detalleComentarios')?.innerText || '-';
            const fechaSubida = document.getElementById('detalleFecha')?.innerText || '-';
            const contenido = `<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Calificación - ${titulo}</title><style>body{font-family:Arial,sans-serif;margin:30px}h1{color:#2c3e50;font-size:20px}.info{background:#f8f9fa;padding:15px;margin:15px 0;border-radius:5px}.calificacion{text-align:center;font-size:32px;color:#27ae60;margin:20px}.comentarios{border-left:3px solid #f39c12;padding:10px;margin:15px 0;background:#fff9e6}.footer{margin-top:30px;text-align:center;font-size:11px;color:#7f8c8d}td{padding:5px}.label{font-weight:bold;width:130px}</style></head><body><h1>🏫 Sistema Académico</h1><h3>Constancia de Calificación de Tarea</h3><div class="info"><h4>📄 Información de la Tarea</h4><table><tr><td class="label">ID Tarea:</td><td>${tareaId}</td></tr><tr><td class="label">Título:</td><td>${titulo}</td></tr><tr><td class="label">Alumno:</td><td>${alumnoNombre}</td></tr><tr><td class="label">Fecha de entrega:</td><td>${fechaSubida}</td></tr></table></div><div class="calificacion"><strong>Calificación:</strong> ${calificacion} / 10</div><div class="comentarios"><strong>💬 Comentarios:</strong><br>${comentarios}</div><div class="footer">Documento generado el ${new Date().toLocaleString()}<br>Sistema Académico - Constancia Oficial</div></body></html>`;
            const ventana = window.open('', '_blank', 'width=700,height=500');
            if (ventana) {
                ventana.document.write(contenido);
                ventana.document.close();
                ventana.print();
            } else {
                alert('Por favor, permite las ventanas emergentes para imprimir');
            }
        } catch (error) {
            console.error('Error en impresión:', error);
            alert('Error al generar la impresión: ' + error.message);
        }
    },
	
    limpiarFiltros() {
        const searchTitulo = document.getElementById('searchTitulo');
        const searchAlumno = document.getElementById('searchAlumno');
        const filterEstado = document.getElementById('filterEstado');
        
        if (searchTitulo) searchTitulo.value = '';
        if (searchAlumno) searchAlumno.value = '';
        if (filterEstado) filterEstado.value = '';
        
        this.busquedaTitulo = '';
        this.busquedaAlumno = '';
        this.filtroEstado = '';
        this.paginaActual = 0;
        
        this.cargarTareas();
    },
	
    mostrarFormCalificacion() {
        console.log('📝 Mostrando formulario de calificación');
        
        const calificacionDisplay = document.getElementById('calificacionDisplay');
        const calificacionForm = document.getElementById('calificacionForm');
        
        if (calificacionDisplay) calificacionDisplay.style.display = 'none';
        if (calificacionForm) calificacionForm.style.display = 'block';
        
        const calificacionActual = document.getElementById('detalleCalificacion')?.textContent;
        const comentariosActuales = document.getElementById('detalleComentarios')?.textContent;
        
        if (calificacionActual && calificacionActual !== '-') {
            document.getElementById('inputCalificacion').value = calificacionActual;
        }
        if (comentariosActuales && comentariosActuales !== '-') {
            document.getElementById('inputComentarios').value = comentariosActuales;
        }
    },

    cancelarCalificacion() {
        console.log('❌ Cancelando calificación');
        
        const calificacionDisplay = document.getElementById('calificacionDisplay');
        const calificacionForm = document.getElementById('calificacionForm');
        
        if (calificacionDisplay) calificacionDisplay.style.display = 'block';
        if (calificacionForm) calificacionForm.style.display = 'none';
        
        const inputCalificacion = document.getElementById('inputCalificacion');
        const inputComentarios = document.getElementById('inputComentarios');
        
        if (inputCalificacion) inputCalificacion.value = '';
        if (inputComentarios) inputComentarios.value = '';
    },

    async guardarCalificacion() {
        console.log('💾 Guardando calificación...');
        
        const tareaId = document.getElementById('tareaId')?.value;
        const calificacion = parseFloat(document.getElementById('inputCalificacion')?.value);
        const comentarios = document.getElementById('inputComentarios')?.value || '';
        
        if (!tareaId) {
            alert('No se puede identificar la tarea');
            return;
        }
        
        if (isNaN(calificacion) || calificacion < 0 || calificacion > 10) {
            alert('La calificación debe ser un número entre 0 y 10');
            return;
        }
        
        const token = auth.getToken();
        if (!token) {
            alert('Sesión expirada');
            window.location.href = 'login.html';
            return;
        }
        
        try {
            const response = await fetch(`${this.apiBase}/${tareaId}/calificar`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ calificacion, comentarios })
            });
            
            if (response.ok) {
                const tarea = await response.json();
                
                const detalleCalificacion = document.getElementById('detalleCalificacion');
                const detalleComentarios = document.getElementById('detalleComentarios');
                const btnImprimir = document.getElementById('btnImprimir');
                
                if (detalleCalificacion) detalleCalificacion.textContent = tarea.calificacion.toFixed(1);
                if (detalleComentarios) detalleComentarios.textContent = tarea.comentarios || '-';
                if (btnImprimir) btnImprimir.style.display = 'inline-block';
                
                this.cancelarCalificacion();
                
                alert('✅ Calificación guardada correctamente');
            } else {
                const error = await response.text();
                console.error('Error response:', error);
                alert('❌ Error al guardar la calificación');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('❌ Error de conexión: ' + error.message);
        }
    },
	
    async cargarPromediosPorAlumno() {
        const token = auth.getToken();
        const userRole = sessionStorage.getItem('user_role');
        const esAdminProfesor = userRole === 'ROLE_ADMIN' || userRole === 'ROLE_PROFESOR';
        const esAlumno = userRole === 'ROLE_ALUMNO';
        
        const tbody = document.getElementById('promediosTable');
        if (!tbody) return;
        
        try {
            let response;
            
            if (esAdminProfesor) {
                response = await fetch(`${this.apiBase}/estadisticas/promedio-por-alumno`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
            } else {
                response = await fetch(`${this.apiBase}/estadisticas/mi-promedio`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
            }
            
            if (response.ok) {
                if (esAdminProfesor) {
                    const promedios = await response.json();
                    if (promedios.length === 0) {
                        tbody.innerHTML = `<td><td colspan="3" class="text-center text-muted">No hay calificaciones registradas<\/td></tr>`;
                    } else {
                        tbody.innerHTML = promedios.map(p => `
                            <tr>
                                <td><strong>${p.alumnoNombre}</strong></td>
                                <td>
                                    <span class="badge bg-${p.promedio >= 6 ? 'success' : 'danger'} fs-6 me-2">${p.promedio}</span>
                                    <div class="progress" style="height: 5px;">
                                        <div class="progress-bar bg-${p.promedio >= 6 ? 'success' : 'danger'}" 
                                             style="width: ${(p.promedio / 10) * 100}%"></div>
                                    </div>
                                 </td>
                                <td><span class="badge bg-info">${p.tareasCalificadas}</span> tareas</td>
                            </tr>
                        `).join('');
                    }
                } else {
                    const miPromedio = await response.json();
                    tbody.innerHTML = `
                        <tr>
                            <td><strong>${miPromedio.alumnoNombre}</strong></td>
                            <td>
                                <span class="badge bg-${miPromedio.promedio >= 6 ? 'success' : 'danger'} fs-6 me-2">${miPromedio.promedio}</span>
                                <div class="progress" style="height: 5px;">
                                    <div class="progress-bar bg-${miPromedio.promedio >= 6 ? 'success' : 'danger'}" 
                                         style="width: ${(miPromedio.promedio / 10) * 100}%"></div>
                                </div>
                            </td>
                            <td><span class="badge bg-info">${miPromedio.tareasCalificadas}</span> de ${miPromedio.totalTareas} tareas</td>
                        </tr>
                    `;
                }
            } else {
                tbody.innerHTML = `<td><td colspan="3" class="text-center text-danger">Error al cargar promedios<\/td></tr>`;
            }
        } catch (error) {
            console.error('Error cargando promedios:', error);
            tbody.innerHTML = `<td><td colspan="3" class="text-center text-danger">Error de conexión<\/td></tr>`;
        }
    }
};

window.tareas = tareas;
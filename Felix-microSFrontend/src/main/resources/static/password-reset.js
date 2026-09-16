const passwordReset = {
    apiBase: 'http://localhost:8080/api/auth',
    
    showMessage(type, message) {
        console.log(`📢 ${type.toUpperCase()}: ${message}`);
        
        const container = document.getElementById('messageContainer');
        if (!container) return;
        
        const alertClass = {
            'success': 'alert-success',
            'error': 'alert-danger',
            'warning': 'alert-warning',
            'info': 'alert-info'
        }[type] || 'alert-info';
        
        const icon = {
            'success': 'check-circle',
            'error': 'exclamation-circle',
            'warning': 'exclamation-triangle',
            'info': 'info-circle'
        }[type] || 'info-circle';
        
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert ${alertClass} alert-dismissible fade show`;
        alertDiv.innerHTML = `
            <i class="fas fa-${icon} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        container.innerHTML = '';
        container.appendChild(alertDiv);
        
        setTimeout(() => {
            if (alertDiv.parentNode) alertDiv.remove();
        }, 5000);
    },
    
    async forgotPassword(email) {
        console.log('📧 Solicitando recuperación para:', email);
        
        const btnSubmit = document.getElementById('btnSubmit');
        if (btnSubmit) {
            btnSubmit.disabled = true;
            btnSubmit.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> Enviando...';
        }
        
        try {
            const response = await fetch(`${this.apiBase}/forgot-password`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json', 
                    'Accept': 'application/json' 
                },
                body: JSON.stringify({ email: email })
            });
            
            const data = await response.json();
            console.log('📨 Respuesta:', data);
            
            if (response.ok && data.success) {
                this.showMessage('success', '✅ ¡Enlace enviado! Revisa tu correo electrónico.');
                const form = document.getElementById('forgotPasswordForm');
                if (form) form.reset();
                setTimeout(() => window.location.href = 'login.html', 5000);
            } else {
                this.showMessage('error', data.message || 'Error al enviar el enlace');
            }
        } catch (error) {
            console.error('❌ Error:', error);
            this.showMessage('error', 'Error de conexión. Intenta nuevamente.');
        } finally {
            if (btnSubmit) {
                btnSubmit.disabled = false;
                btnSubmit.innerHTML = '<i class="fas fa-paper-plane me-2"></i> Enviar enlace de recuperación';
            }
        }
    },
    
    async validateShortId(shortId) {
        console.log('🔍 Validando shortId:', shortId);
        
        const statusDiv = document.getElementById('tokenStatus');
        const statusText = document.getElementById('tokenStatusText');
        
        if (statusDiv) statusDiv.classList.remove('d-none');
        if (statusText) statusText.textContent = 'Validando token...';
        
        try {
            const response = await fetch(`${this.apiBase}/validate-shortId?shortId=${shortId}`, {
                method: 'GET',
                headers: { 
                    'Accept': 'application/json', 
                    'Content-Type': 'application/json' 
                }
            });
            
            console.log('✅ Estado respuesta:', response.status);
            const data = await response.json();
            console.log('📊 Datos validación:', data);
            
            if (data.success) {
                if (statusText) statusText.innerHTML = '<i class="fas fa-check-circle me-2"></i> Token válido. Puedes crear una nueva contraseña.';
                if (statusDiv) {
                    statusDiv.classList.remove('alert-info', 'alert-danger');
                    statusDiv.classList.add('alert-success');
                }
                
                const form = document.getElementById('resetPasswordForm');
                if (form) {
                    form.classList.remove('d-none');
                    const shortIdInput = document.getElementById('resetShortId');
                    if (shortIdInput) shortIdInput.value = shortId;
                }
                
                this.setupPasswordValidation();
            } else {
                if (statusText) statusText.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i> ' + (data.message || 'Token inválido o expirado');
                if (statusDiv) {
                    statusDiv.classList.remove('alert-info', 'alert-success');
                    statusDiv.classList.add('alert-danger');
                }
                this.showMessage('error', 'El enlace ha expirado o no es válido. Solicita un nuevo enlace.');
                setTimeout(() => window.location.href = 'forgot-password.html', 5000);
            }
        } catch (error) {
            console.error('❌ Error validando shortId:', error);
            if (statusText) statusText.innerHTML = '<i class="fas fa-exclamation-triangle me-2"></i> Error de conexión con el servidor';
            if (statusDiv) statusDiv.classList.add('alert-danger');
            this.showMessage('error', 'No se pudo conectar con el servidor. Verifica tu conexión.');
        }
    },
    
    async resetPassword(shortId, newPassword) {
        console.log('🔄 Cambiando contraseña con shortId:', shortId);
        
        const btnSubmit = document.getElementById('btnSubmit');
        if (btnSubmit) {
            btnSubmit.disabled = true;
            btnSubmit.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> Cambiando...';
        }
        
        try {
            const response = await fetch(`${this.apiBase}/reset-password`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json', 
                    'Accept': 'application/json' 
                },
                body: JSON.stringify({ shortId: shortId, newPassword: newPassword })
            });
            
            const data = await response.json();
            console.log('✅ Respuesta reset:', data);
            
            if (response.ok && data.success) {
                this.showMessage('success', '✅ ¡Contraseña cambiada exitosamente!');
                setTimeout(() => window.location.href = 'login.html', 3000);
            } else {
                this.showMessage('error', data.message || 'Error al cambiar la contraseña');
            }
        } catch (error) {
            console.error('❌ Error:', error);
            this.showMessage('error', 'Error de conexión. Intenta nuevamente.');
        } finally {
            if (btnSubmit) {
                btnSubmit.disabled = false;
                btnSubmit.innerHTML = '<i class="fas fa-save me-2"></i> Cambiar contraseña';
            }
        }
    },
    
    setupPasswordValidation() {
        const newPassword = document.getElementById('newPassword');
        const confirmPassword = document.getElementById('confirmPassword');
        const strengthBar = document.getElementById('passwordStrength');
        const matchText = document.getElementById('passwordMatch');
        const btnSubmit = document.getElementById('btnSubmit');
        
        if (!newPassword || !confirmPassword) return;
        
        const checkPasswords = () => {
            const password = newPassword.value;
            const confirm = confirmPassword.value;
            let isValid = true;
            
            if (strengthBar) {
                const strength = this.checkPasswordStrength(password);
                strengthBar.className = `password-strength strength-${strength.level}`;
            }
            
            if (matchText) {
                if (confirm.length === 0) {
                    matchText.textContent = 'Confirma tu contraseña';
                    matchText.className = 'form-text text-muted';
                } else if (password === confirm) {
                    matchText.innerHTML = '<i class="fas fa-check text-success me-1"></i> Las contraseñas coinciden';
                    matchText.className = 'form-text text-success';
                } else {
                    matchText.innerHTML = '<i class="fas fa-times text-danger me-1"></i> Las contraseñas no coinciden';
                    matchText.className = 'form-text text-danger';
                    isValid = false;
                }
            }
            
            if (password.length < 6) isValid = false;
            if (btnSubmit) btnSubmit.disabled = !isValid;
        };
        
        // Evento para enviar el formulario
        const form = document.getElementById('resetPasswordForm');
        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const shortId = document.getElementById('resetShortId').value;
                const newPassword = document.getElementById('newPassword').value;
                if (shortId && newPassword) {
                    this.resetPassword(shortId, newPassword);
                }
            });
        }
        
        newPassword.addEventListener('input', checkPasswords);
        confirmPassword.addEventListener('input', checkPasswords);
        checkPasswords();
    },
    
    checkPasswordStrength(password) {
        let score = 0;
        if (password.length >= 6) score++;
        if (password.length >= 8) score++;
        if (/[a-z]/.test(password)) score++;
        if (/[A-Z]/.test(password)) score++;
        if (/[0-9]/.test(password)) score++;
        if (/[^A-Za-z0-9]/.test(password)) score++;
        
        const levels = [
            { level: 'weak', message: 'Débil' },
            { level: 'fair', message: 'Regular' },
            { level: 'good', message: 'Buena' },
            { level: 'strong', message: 'Fuerte' }
        ];
        return levels[Math.min(score, 3)];
    }
};

window.passwordReset = passwordReset;
console.log('✅ password-reset.js cargado correctamente');
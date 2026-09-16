package com.peral.alumnos.dto;

public class AlumnoResponseDTO {
    
    private Long id;
    private String nombre;
    private String email;
    private Integer edad;
    private String carrera;
    
    private Boolean activo; // ✅ NUEVO CAMPO
    
    // ✅ NUEVOS CAMPOS PARA EL USUARIO
    private Long usuarioId;
    private String usuarioUsername;
    
    // Constructor vacío
    public AlumnoResponseDTO() {}

    // ✅ NUEVO CONSTRUCTOR CON PARÁMETROS (para usar en UsuarioServiceImpl)
    public AlumnoResponseDTO(Long id, String nombre, String email, Integer edad, String carrera) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.edad = edad;
        this.carrera = carrera;
    }

    // Getters y Setters existentes
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    // ✅ NUEVOS GETTERS Y SETTERS
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioUsername() {
        return usuarioUsername;
    }

    public void setUsuarioUsername(String usuarioUsername) {
        this.usuarioUsername = usuarioUsername;
    }
    
    

    public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	// Opcional: toString para debugging
	@Override
	public String toString() {
	    return "AlumnoResponseDTO{" +
	            "id=" + id +
	            ", nombre='" + nombre + '\'' +
	            ", email='" + email + '\'' +
	            ", edad=" + edad +
	            ", carrera='" + carrera + '\'' +
	            ", activo=" + activo +
	            ", usuarioId=" + usuarioId +
	            ", usuarioUsername='" + usuarioUsername + '\'' +
	            '}';
	}
}
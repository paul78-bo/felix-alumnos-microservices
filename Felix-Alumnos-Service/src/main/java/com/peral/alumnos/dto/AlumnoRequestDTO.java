package com.peral.alumnos.dto;

import jakarta.validation.constraints.*;

public class AlumnoRequestDTO {

	@NotBlank(message = "El nombre es obligatorio")
	private String nombre;

	@Email(message = "Email inválido")
	@NotBlank(message = "El email es obligatorio")
	private String email;

	@NotNull(message = "La edad es obligatoria")
	@Min(value = 15, message = "La edad mínima es 15 años")
	@Max(value = 99, message = "La edad máxima es 99 años")
	private Integer edad;

	@NotBlank(message = "La carrera es obligatoria")
	private String carrera;
	
	 // ✅ OPCIONAL: Si quieres permitir activar/desactivar desde DTO
    private Boolean activo;

	// getters y setters
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

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
	
}

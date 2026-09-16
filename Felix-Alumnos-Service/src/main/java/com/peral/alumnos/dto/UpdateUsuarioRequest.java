package com.peral.alumnos.dto;

import jakarta.validation.constraints.*;

public class UpdateUsuarioRequest {

	@Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
	private String username; // ← Quitar @NotBlank

	@NotBlank(message = "El email es obligatorio")
	@Email(message = "Debe ser un email válido")
	private String email;

	private String password; // ← SIN @NotBlank

	@NotBlank(message = "El rol es obligatorio")
	@Pattern(regexp = "ROLE_ADMIN|ROLE_PROFESOR|ROLE_ALUMNO", message = "Rol inválido")
	private String role;

	private Boolean enabled;

	// Campos para alumno
	private String nombreAlumno;
	private Integer edadAlumno;
	private String carreraAlumno;

	// Getters y Setters (igual que Create)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getNombreAlumno() {
		return nombreAlumno;
	}

	public void setNombreAlumno(String nombreAlumno) {
		this.nombreAlumno = nombreAlumno;
	}

	public Integer getEdadAlumno() {
		return edadAlumno;
	}

	public void setEdadAlumno(Integer edadAlumno) {
		this.edadAlumno = edadAlumno;
	}

	public String getCarreraAlumno() {
		return carreraAlumno;
	}

	public void setCarreraAlumno(String carreraAlumno) {
		this.carreraAlumno = carreraAlumno;
	}
}
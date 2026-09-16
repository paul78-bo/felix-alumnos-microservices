package com.peral.alumnos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "acceso_entrega")
public class AccesoEntrega {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String shortId;

	@Column(nullable = false)
	private Long tareaId;

	@Column(nullable = false)
	private Long profesorId;

	@Column(nullable = false)
	private LocalDateTime fechaExpiracion;

	@Column(nullable = false)
	private boolean usado = false;

	// Constructor vacío
	public AccesoEntrega() {
	}

	// Constructor con parámetros
	public AccesoEntrega(String shortId, Long tareaId, Long profesorId, LocalDateTime fechaExpiracion) {
		this.shortId = shortId;
		this.tareaId = tareaId;
		this.profesorId = profesorId;
		this.fechaExpiracion = fechaExpiracion;
	}

	// Getters y Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getShortId() {
		return shortId;
	}

	public void setShortId(String shortId) {
		this.shortId = shortId;
	}

	public Long getTareaId() {
		return tareaId;
	}

	public void setTareaId(Long tareaId) {
		this.tareaId = tareaId;
	}

	public Long getProfesorId() {
		return profesorId;
	}

	public void setProfesorId(Long profesorId) {
		this.profesorId = profesorId;
	}

	public LocalDateTime getFechaExpiracion() {
		return fechaExpiracion;
	}

	public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
		this.fechaExpiracion = fechaExpiracion;
	}

	public boolean isUsado() {
		return usado;
	}

	public void setUsado(boolean usado) {
		this.usado = usado;
	}
}
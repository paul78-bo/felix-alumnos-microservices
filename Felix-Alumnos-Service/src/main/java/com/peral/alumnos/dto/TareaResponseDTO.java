package com.peral.alumnos.dto;

import java.time.LocalDateTime;

public class TareaResponseDTO {

	private Long id;
	private String titulo;
	private String descripcion;
	private String nombreArchivo;
	private String tipoArchivo;
	private Long tamanioArchivo;
	private LocalDateTime fechaSubida;
	private Double calificacion;
	private String comentarios;

	// ✅ AGREGAR ESTOS CAMPOS
	private String tipoTarea; // "ENUNCIADO" o "ENTREGA"
	private String estado;
	private LocalDateTime fechaEntrega;

	// Datos del alumno
	private Long alumnoId;
	private String alumnoNombre;
	private String alumnoEmail;
	
	private String archivoEntrega;


	// Getters y Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public String getTipoArchivo() {
		return tipoArchivo;
	}

	public void setTipoArchivo(String tipoArchivo) {
		this.tipoArchivo = tipoArchivo;
	}

	public Long getTamanioArchivo() {
		return tamanioArchivo;
	}

	public void setTamanioArchivo(Long tamanioArchivo) {
		this.tamanioArchivo = tamanioArchivo;
	}

	public LocalDateTime getFechaSubida() {
		return fechaSubida;
	}

	public void setFechaSubida(LocalDateTime fechaSubida) {
		this.fechaSubida = fechaSubida;
	}

	public Double getCalificacion() {
		return calificacion;
	}

	public void setCalificacion(Double calificacion) {
		this.calificacion = calificacion;
	}

	public String getComentarios() {
		return comentarios;
	}

	public void setComentarios(String comentarios) {
		this.comentarios = comentarios;
	}

	// ✅ Getters y Setters para los nuevos campos
	public String getTipoTarea() {
		return tipoTarea;
	}

	public void setTipoTarea(String tipoTarea) {
		this.tipoTarea = tipoTarea;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public LocalDateTime getFechaEntrega() {
		return fechaEntrega;
	}

	public void setFechaEntrega(LocalDateTime fechaEntrega) {
		this.fechaEntrega = fechaEntrega;
	}

	public Long getAlumnoId() {
		return alumnoId;
	}

	public void setAlumnoId(Long alumnoId) {
		this.alumnoId = alumnoId;
	}

	public String getAlumnoNombre() {
		return alumnoNombre;
	}

	public void setAlumnoNombre(String alumnoNombre) {
		this.alumnoNombre = alumnoNombre;
	}

	public String getAlumnoEmail() {
		return alumnoEmail;
	}

	public void setAlumnoEmail(String alumnoEmail) {
		this.alumnoEmail = alumnoEmail;
	}

	public String getArchivoEntrega() {
		return archivoEntrega;
	}

	public void setArchivoEntrega(String archivoEntrega) {
		this.archivoEntrega = archivoEntrega;
	}
	
	
}
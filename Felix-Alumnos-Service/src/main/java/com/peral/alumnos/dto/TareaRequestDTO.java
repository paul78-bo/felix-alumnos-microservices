package com.peral.alumnos.dto;

import org.springframework.web.multipart.MultipartFile;

public class TareaRequestDTO {
    
    private String titulo;
    private String descripcion;
    private MultipartFile archivo;
    private Long alumnoId;
    
    private String tipoTarea; // "ENUNCIADO" o "ENTREGA"
    
  

    // Constructor vacío (obligatorio para Spring)
    public TareaRequestDTO() {}

    // Getters y Setters
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

    public MultipartFile getArchivo() {
        return archivo;
    }

    public void setArchivo(MultipartFile archivo) {
        this.archivo = archivo;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }
    
    

    public String getTipoTarea() {
		return tipoTarea;
	}

	public void setTipoTarea(String tipoTarea) {
		this.tipoTarea = tipoTarea;
	}
	
	


	// Opcional: toString para debugging
    @Override
    public String toString() {
        return "TareaRequestDTO{" +
                "titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", archivo=" + (archivo != null ? archivo.getOriginalFilename() : "null") +
                ", alumnoId=" + alumnoId +
                '}';
    }
    
}
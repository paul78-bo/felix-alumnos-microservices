package com.peral.alumnos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tareas")
public class Tarea {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    @Column(length = 500)
    private String descripcion;
    
    @Column(nullable = false)
    private String nombreArchivo;
    
    @Column(nullable = false)
    private String tipoArchivo;
    
    @Column(nullable = false)
    private Long tamanioArchivo;
    
    @Column(nullable = false)
    private String rutaArchivo;
    
    @Column(nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();
    
    private Double calificacion;
    
    @Column(length = 500)
    private String comentarios;
    
    @Column(name = "tipo_tarea")
    private String tipoTarea = "ENUNCIADO";
    
    @Column(name = "archivo_entrega")
    private String archivoEntrega;

  
    // getters y setters
    
    
    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
    
    @Column(name = "estado")
    private String estado = "PENDIENTE"; // PENDIENTE, ENTREGADO, CALIFICADO
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // ✅ O usa @JsonIgnore
    private Alumno alumno;
    // ==============================================
    // CONSTRUCTORES
    // ==============================================
    
    public Tarea() {}

    public Tarea(String titulo, String descripcion, String nombreArchivo, 
                 String tipoArchivo, Long tamanioArchivo, String rutaArchivo, Alumno alumno) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.nombreArchivo = nombreArchivo;
        this.tipoArchivo = tipoArchivo;
        this.tamanioArchivo = tamanioArchivo;
        this.rutaArchivo = rutaArchivo;
        this.alumno = alumno;
    }

    // ==============================================
    // GETTERS Y SETTERS
    // ==============================================

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

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
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

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }
    
    
    

    // ==============================================
    // toString (opcional, útil para debugging)
    // ==============================================
    
    public LocalDateTime getFechaEntrega() {
		return fechaEntrega;
	}

	public void setFechaEntrega(LocalDateTime fechaEntrega) {
		this.fechaEntrega = fechaEntrega;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getTipoTarea() {
		return tipoTarea;
	}

	public void setTipoTarea(String tipoTarea) {
		this.tipoTarea = tipoTarea;
	}
	
	

	public String getArchivoEntrega() {
		return archivoEntrega;
	}

	public void setArchivoEntrega(String archivoEntrega) {
		this.archivoEntrega = archivoEntrega;
	}

	@Override
    public String toString() {
        return "Tarea{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", fechaSubida=" + fechaSubida +
                ", calificacion=" + calificacion +
                '}';
    }
}
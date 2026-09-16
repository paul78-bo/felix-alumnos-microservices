package com.peral.alumnos.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "alumnos") // ← Nombre específico de tabla
public class Alumno {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // ← Auto-increment MySQL
	private Long id;

	@Column(name = "nombre", length = 100, nullable = false)
	private String nombre;

	@NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Column(nullable = false, unique = true)
    private String email;
    

	@Column(name = "edad")
	private Integer edad;

	@Column(name = "carrera", length = 100)
	private String carrera;
	
	 // ✅ NUEVO CAMPO PARA SOFT DELETE
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

	// 🔥 NUEVO: Relación con Usuario
	@OneToOne
	@JoinColumn(name = "usuario_id", referencedColumnName = "id")
	@JsonIgnore // Evita recursión infinita al serializar
	private Usuario usuario;
	
	
	
	@OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore  // ✅ Agrega esta anotación
	private List<Tarea> tareas = new ArrayList<>();
	
	
	
	
	public void agregarTarea(Tarea tarea) {
	    tareas.add(tarea);
	    tarea.setAlumno(this);
	}

	public void removerTarea(Tarea tarea) {
	    tareas.remove(tarea);
	    tarea.setAlumno(null);
	}

	// Constructores, getters y setters (igual que antes)
	public Alumno() {
	}

	public Alumno(String nombre, String email, Integer edad, String carrera) {
		this.nombre = nombre;
		this.email = email;
		this.edad = edad;
		this.carrera = carrera;
		this.activo = true; // ✅ Por defecto activo
	}

	// Getters y Setters...
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

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<Tarea> getTareas() {
		return tareas;
	}

	public void setTareas(List<Tarea> tareas) {
		this.tareas = tareas;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
	
	
	
	
	
}
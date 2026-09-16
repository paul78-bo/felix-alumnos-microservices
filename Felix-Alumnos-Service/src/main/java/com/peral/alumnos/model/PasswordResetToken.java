package com.peral.alumnos.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String token;

	@ManyToOne
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(nullable = false)
	private LocalDateTime expiryDate;

	@Column(nullable = false)
	private boolean used = false;

	@Column(nullable = false, unique = true)
	private String shortId; // ✅ Identificador corto para la URL

	// Constructor vacío (requerido por JPA)
	public PasswordResetToken() {
	}

	// Constructor con parámetros - CORREGIDO
	public PasswordResetToken(Usuario usuario, LocalDateTime expiryDate) {
		this.token = UUID.randomUUID().toString();
		this.shortId = generarShortId();
		this.usuario = usuario;
		this.expiryDate = expiryDate;
		this.used = false;
	}

	private String generarShortId() {
		// Generar ID corto de 8 caracteres (ej: aB3dE5fG)
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			int index = (int) (Math.random() * chars.length());
			sb.append(chars.charAt(index));
		}
		return sb.toString();
	}

	@PrePersist
	public void prePersist() {
		if (this.token == null) {
			this.token = UUID.randomUUID().toString();
		}
		if (this.shortId == null) {
			this.shortId = generarShortId();
		}
	}

	// Getters y setters...
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiryDate);
	}

	public String getShortId() {
		return shortId;
	}

	public void setShortId(String shortId) {
		this.shortId = shortId;
	}
}
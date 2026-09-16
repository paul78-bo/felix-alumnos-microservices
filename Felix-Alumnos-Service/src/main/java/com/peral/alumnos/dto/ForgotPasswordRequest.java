package com.peral.alumnos.dto;

public class ForgotPasswordRequest {

	private String email;

	// Constructor, getters y setters
	public ForgotPasswordRequest() {
	}

	public ForgotPasswordRequest(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
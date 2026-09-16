package com.peral.alumnos.dto;

public class ResetPasswordRequest {
    private String token;        // Token largo (UUID) - para compatibilidad
    private String shortId;       // Identificador corto para URLs
    private String newPassword;

    // Constructor vacío
    public ResetPasswordRequest() {}

    // Constructor con token
    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    // Constructor con shortId
    public ResetPasswordRequest(String shortId, String newPassword, boolean usarShortId) {
        this.shortId = shortId;
        this.newPassword = newPassword;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
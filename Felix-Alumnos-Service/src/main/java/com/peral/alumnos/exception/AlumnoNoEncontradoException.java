package com.peral.alumnos.exception;

public class AlumnoNoEncontradoException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Constructor con mensaje
    public AlumnoNoEncontradoException(String message) {
        super(message);
    }
    
    // Constructor con mensaje y causa (opcional)
    public AlumnoNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
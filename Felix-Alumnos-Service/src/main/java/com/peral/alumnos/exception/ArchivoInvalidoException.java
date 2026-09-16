package com.peral.alumnos.exception;

public class ArchivoInvalidoException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArchivoInvalidoException(String message) {
        super(message);
    }
    
    public ArchivoInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
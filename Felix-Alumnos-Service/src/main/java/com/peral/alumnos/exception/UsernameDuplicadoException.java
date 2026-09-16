package com.peral.alumnos.exception;

public class UsernameDuplicadoException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UsernameDuplicadoException(String message) {
        super(message);
    }
    
    public UsernameDuplicadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
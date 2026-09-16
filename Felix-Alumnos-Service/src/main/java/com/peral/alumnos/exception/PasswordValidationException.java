// Crea archivo: src/main/java/com/peral/alumnos/exception/PasswordValidationException.java
package com.peral.alumnos.exception;

public class PasswordValidationException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PasswordValidationException(String message) {
        super(message);
    }
}
package com.peral.alumnos.exception;

public class EmailDuplicadoException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EmailDuplicadoException(String message) {
        super(message);
    }
}

package com.peral.alumnos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TokenExpiradoException extends RuntimeException {
    public TokenExpiradoException(String message) {
        super(message);}
}
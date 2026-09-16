package com.peral.alumnos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔴 VALIDACIONES DTO (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "message", "Error de validación",
                        "errors", errores
                ));
    }

    // 🔴 EMAIL DUPLICADO
    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<?> handleEmailDuplicado(EmailDuplicadoException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "status", 409,
                        "message", "Conflicto de datos",
                        "errors", Map.of(
                                "email", ex.getMessage()
                        )
                ));
    }

    // 🔴 ALUMNO NO ENCONTRADO
    @ExceptionHandler(AlumnoNoEncontradoException.class)
    public ResponseEntity<?> handleAlumnoNoEncontrado(AlumnoNoEncontradoException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", 404,
                        "message", "Recurso no encontrado",
                        "errors", Map.of(
                                "alumno", ex.getMessage()
                        )
                ));
    }

    // 🔴 FALLBACK (ERROR DESCONOCIDO)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "message", "Error interno del servidor",
                        "errors", Map.of(
                                "error", ex.getMessage()
                        )
                ));
    }
}

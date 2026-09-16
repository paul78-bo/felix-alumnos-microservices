package com.peral.alumnos.util;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class EmailTestStandalone {
    
    private static final Logger log = LoggerFactory.getLogger(EmailTestStandalone.class);
    private final JavaMailSender mailSender;
    
    public EmailTestStandalone(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @PostConstruct
    public void testEmailOnStartup() {
        log.info("🚀 INICIANDO PRUEBA AUTOMÁTICA DE EMAIL...");
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@universidad.edu");
            message.setTo("paulponce56@gmail.com"); // ← TU EMAIL
            message.setSubject("🔧 PRUEBA TÉCNICA - Sistema Alumnos");
            message.setText(
                "Hola,\n\n" +
                "Esta es una prueba automática del sistema.\n" +
                "Fecha: " + new java.util.Date() + "\n" +
                "Servidor: Backend Container\n\n" +
                "✅ Si recibes esto, el email FUNCIONA."
            );
            
            mailSender.send(message);
            log.info("✅✅✅ PRUEBA EXITOSA - Email enviado a paulponce56@gmail.com");
            
        } catch (Exception e) {
            log.error("❌❌❌ FALLO EN PRUEBA DE EMAIL");
            log.error("Tipo error: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());
            
            // Mostrar causa raíz
            if (e.getCause() != null) {
                log.error("Causa: {}", e.getCause().getMessage());
            }
        }
    }
}
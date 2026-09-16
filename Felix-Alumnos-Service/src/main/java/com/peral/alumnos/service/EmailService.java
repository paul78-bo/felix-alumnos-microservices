package com.peral.alumnos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email-from}")
    private String emailFrom;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String shortId) {
        try {
            
            System.out.println("========== DEBUG EMAIL ==========");
            System.out.println("📧 toEmail: " + toEmail);
            System.out.println("🔑 shortId: " + shortId);
            System.out.println("🌐 frontendUrl desde properties: " + frontendUrl);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(toEmail);
            helper.setSubject("Restablecer contraseña - Sistema Académico");

            // Limpiar la URL base
            String baseUrl = frontendUrl.replace("/reset-password", "");
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            
            // ✅ CORREGIDO: Generar enlace correcto con .html y parámetro
            String resetLink = baseUrl + "/reset-password.html?shortId=" + shortId;
            
            // Log para depuración
            System.out.println("📧 Enviando correo a: " + toEmail);
            System.out.println("🔗 Enlace generado: " + resetLink);

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .button { 
                            display: inline-block; 
                            padding: 10px 20px; 
                            background-color: #4CAF50; 
                            color: white; 
                            text-decoration: none; 
                            border-radius: 5px; 
                            margin: 15px 0;
                        }
                        .footer { margin-top: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Sistema Académico</h2>
                        </div>
                        <div class="content">
                            <h3>Restablecer contraseña</h3>
                            <p>Hemos recibido una solicitud para restablecer tu contraseña.</p>
                            <p>Haz clic en el siguiente botón para continuar:</p>
                            <a href="%s" class="button">Restablecer contraseña</a>
                            <p><strong>Importante:</strong> Este enlace expirará en 24 horas.</p>
                            <p>Si no solicitaste restablecer tu contraseña, ignora este correo.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un correo automático, por favor no responder.</p>
                            <p style="font-size: 10px; color: #999;">ID: %s</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(resetLink, shortId);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }
}
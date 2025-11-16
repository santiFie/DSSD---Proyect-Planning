package com.proyect_planning.proyect_planning_system.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envía un email a múltiples destinatarios
     */
    public void sendEmail(List<String> toEmails, String subject, String body) {
        if (toEmails == null || toEmails.isEmpty()) {
            logger.warn("No hay destinatarios para enviar el email");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmails.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(body, true); // true indica que el body es HTML

            mailSender.send(message);
            logger.info("Email enviado exitosamente a {} destinatarios", toEmails.size());
        } catch (MessagingException e) {
            logger.error("Error al enviar email: {}", e.getMessage(), e);
            throw new RuntimeException("Error al enviar email", e);
        }
    }

    /**
     * Genera el cuerpo HTML del email para una nueva observación
     */
    public String generarCuerpoEmailObservacion(String proyectoNombre, String observacionDescripcion, 
                                                 String ongNombre, Long observacionId) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; border-radius: 5px; }
                    .observacion-id { font-weight: bold; color: #f44336; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div>
                        <h2>Nueva Observación en Proyecto</h2>
                    </div>
                    <div class="content">
                        <p>Estimado/a miembro de la organización <strong>%s</strong>,</p>
                        
                        <p>Se ha registrado una nueva observación en el proyecto:</p>
                        
                        <p><strong>Proyecto:</strong> %s</p>
                        <p><strong>ID de Observación:</strong> <span class="observacion-id">#%d</span></p>
                        
                        <p><strong>Descripción de la observación:</strong></p>
                        <p style="background-color: white; padding: 15px; border-left: 4px solid #f44336;">
                            %s
                        </p>
                        
                        <p>Por favor, revise la observación.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(ongNombre, proyectoNombre, observacionId, observacionDescripcion);
    }
}

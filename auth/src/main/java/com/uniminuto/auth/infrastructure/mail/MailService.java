package com.uniminuto.auth.infrastructure.mail;

import com.uniminuto.auth.application.port.out.MailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService implements MailPort {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendResetPasswordEmail(String to, String resetToken, int expirationMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Recuperación de Contraseña");
        message.setText("""
                Hola,

                Has solicitado recuperar tu contraseña.

                Tu código de recuperación es: %s

                Este código expira en %d minutos.

                Si no solicitaste este cambio, ignora este correo.

                Saludos,
                El equipo de Auth
                """.formatted(resetToken, expirationMinutes));

        mailSender.send(message);
        log.info("Correo de recuperación enviado a: {}", to);
    }

    public void sendVerificationEmail(String to, String verificationLink, int expirationMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Verifica tu cuenta");
        message.setText("""
                Hola,

                Gracias por registrarte. Haz clic en el siguiente enlace para verificar tu correo:

                %s

                Este enlace expira en %d minutos.

                Si no creaste esta cuenta, ignora este correo.

                Saludos,
                El equipo de Auth
                """.formatted(verificationLink, expirationMinutes));

        mailSender.send(message);
        log.info("Correo de verificación enviado a: {}", to);
    }
}

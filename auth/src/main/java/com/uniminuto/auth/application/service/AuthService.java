package com.uniminuto.auth.application.service;

import com.uniminuto.auth.application.port.out.JwtPort;
import com.uniminuto.auth.application.port.out.MailPort;
import com.uniminuto.auth.domain.exception.BadRequestException;
import com.uniminuto.auth.domain.exception.ConflictException;
import com.uniminuto.auth.domain.exception.ResourceNotFoundException;
import com.uniminuto.auth.domain.exception.UnauthorizedException;
import com.uniminuto.auth.domain.model.User;
import com.uniminuto.auth.domain.repository.UserRepository;
import com.uniminuto.auth.application.dto.request.ForgotPasswordRequest;
import com.uniminuto.auth.application.dto.request.LoginRequest;
import com.uniminuto.auth.application.dto.request.RegisterRequest;
import com.uniminuto.auth.application.dto.request.ResetPasswordRequest;
import com.uniminuto.auth.application.dto.response.AuthResponse;
import com.uniminuto.auth.application.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtPort jwtPort;
    private final MailPort mailPort;

    @Value("${app.mail.reset-token-expiration:15}")
    private int resetTokenExpirationMinutes;

    @Value("${app.mail.verification-token-expiration:1440}")
    private int verificationTokenExpirationMinutes;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // ==================== LOGIN ====================
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        if (!user.getEnabled()) {
            throw new UnauthorizedException("Debes verificar tu correo electrónico antes de iniciar sesión");
        }

        String accessToken = jwtPort.generateToken(user.getEmail(), user.getId());
        String refreshToken = jwtPort.generateRefreshToken(user.getEmail());

        log.info("Usuario autenticado: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtPort.getExpirationTime())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build())
                .build();
    }

    // ==================== REGISTER ====================
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El correo electrónico ya está registrado");
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(false)
                .verificationCode(verificationToken)
                .verificationCodeExpiration(LocalDateTime.now().plusMinutes(verificationTokenExpirationMinutes))
                .build();

        String verificationLink = frontendUrl + "/verify/VerifyEmail.html?token=" + verificationToken;

        // Enviar correo PRIMERO, si falla el @Transactional revierte el INSERT
        mailPort.sendVerificationEmail(user.getEmail(), verificationLink, verificationTokenExpirationMinutes);

        userRepository.save(user);

        log.info("Usuario registrado, verificación pendiente: {}", user.getEmail());

        return MessageResponse.success("Registro exitoso. Revisa tu correo para verificar tu cuenta.");
    }

    // ==================== VERIFY EMAIL ====================
    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationCode(token)
                .orElseThrow(() -> new BadRequestException("Token de verificación inválido"));

        if (user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token de verificación ha expirado. Solicita uno nuevo.");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        userRepository.save(user);

        log.info("Email verificado: {}", user.getEmail());

        return MessageResponse.success("Correo verificado exitosamente. Ya puedes iniciar sesión.");
    }

    // ==================== FORGOT PASSWORD ====================
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String rawToken = UUID.randomUUID().toString();

            user.setResetToken(passwordEncoder.encode(rawToken));
            user.setResetTokenExpiration(LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes));
            userRepository.save(user);

            String resetLink = frontendUrl + "/reset/ResetPassword.html?token=" + rawToken + "&email=" + user.getEmail();
            mailPort.sendResetPasswordEmail(user.getEmail(), resetLink, resetTokenExpirationMinutes);
            log.info("Enlace de recuperación enviado a: {}", user.getEmail());
        }

        // Siempre retorna éxito para evitar user enumeration attack
        return MessageResponse.success("Si el correo está registrado, recibirás las instrucciones de recuperación.");
    }

    // ==================== RESET PASSWORD ====================
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getResetToken() == null || user.getResetTokenExpiration() == null) {
            throw new BadRequestException("No hay solicitud de recuperación de contraseña activa");
        }

        if (user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token ha expirado");
        }

        if (!passwordEncoder.matches(request.getToken(), user.getResetToken())) {
            throw new BadRequestException("Token inválido");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiration(null);
        userRepository.save(user);

        log.info("Contraseña restablecida para: {}", user.getEmail());

        return MessageResponse.success("Contraseña restablecida exitosamente");
    }
}

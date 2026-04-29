package com.uniminuto.auth.web.controller;

import com.uniminuto.auth.application.service.AuthService;
import com.uniminuto.auth.application.dto.request.ForgotPasswordRequest;
import com.uniminuto.auth.application.dto.request.LoginRequest;
import com.uniminuto.auth.application.dto.request.RegisterRequest;
import com.uniminuto.auth.application.dto.request.ResetPasswordRequest;
import com.uniminuto.auth.application.dto.response.AuthResponse;
import com.uniminuto.auth.application.dto.response.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(MessageResponse.success("Auth service is running"));
    }
}

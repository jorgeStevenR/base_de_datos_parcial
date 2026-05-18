package com.uniminuto.auth.domain.port.in;

import com.uniminuto.auth.application.dto.request.ForgotPasswordRequest;
import com.uniminuto.auth.application.dto.request.LoginRequest;
import com.uniminuto.auth.application.dto.request.RegisterRequest;
import com.uniminuto.auth.application.dto.request.ResetPasswordRequest;
import com.uniminuto.auth.application.dto.response.AuthResponse;
import com.uniminuto.auth.application.dto.response.MessageResponse;

public interface AuthServicePort {

    AuthResponse login(LoginRequest request);

    MessageResponse register(RegisterRequest request);

    MessageResponse verifyEmail(String token);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);
}

package com.uniminuto.auth.application.port.out;

public interface JwtPort {

    String generateToken(String email, Long userId);

    String generateRefreshToken(String email);

    String extractEmail(String token);

    boolean isTokenValid(String token);

    long getExpirationTime();
}

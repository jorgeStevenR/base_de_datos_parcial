package com.uniminuto.auth.domain.port.out;

public interface MailPort {

    void sendVerificationEmail(String to, String verificationLink, int expirationMinutes);

    void sendResetPasswordEmail(String to, String resetLink, int expirationMinutes);
}

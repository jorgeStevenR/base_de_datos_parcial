package com.uniminuto.auth.application.port.out;

public interface MailPort {

    void sendVerificationEmail(String to, String verificationLink, int expirationMinutes);

    void sendResetPasswordEmail(String to, String resetLink, int expirationMinutes);
}

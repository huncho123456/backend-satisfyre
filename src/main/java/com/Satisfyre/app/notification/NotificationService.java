package com.Satisfyre.app.notification;


public interface NotificationService {
    void sendWelcomeEmail(String to, String fullName, String email, String password, String phoneNumber, String consultantId);
    void sendPasswordResetEmail(String to, String firstName, String resetLink);
    void sendSms();
    void sendWhatsapp();
}

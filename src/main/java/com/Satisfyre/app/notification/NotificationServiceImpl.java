package com.Satisfyre.app.notification;

import com.Satisfyre.app.config.dotenvConfig;
import com.Satisfyre.app.repo.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {


    private final JavaMailSender javaMailSender;
    private final NotificationRepository notificationRepository;
    String FRONT_ENDPOINT = dotenvConfig.get("FRONTEND_BASEURL");
    String BaseUrl = dotenvConfig.get("BASEURL");

    @Async
    @Override
    public void sendWelcomeEmail(String to, String fullName, String email, String password, String phoneNumber, String consultantId) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {

            URL url = new URL(FRONT_ENDPOINT + "/html/email.html");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Satisfyre-Mailer/1.0");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                log.error("Failed to fetch welcome email template. HTTP status: {}", conn.getResponseCode());
                return;
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String rawHtml = reader.lines().collect(Collectors.joining("\n"));

            String referralLink = BaseUrl + "/api/auth/register?ref=" + consultantId;
            // 2️⃣ Replace placeholders in HTML template
            String processedHtml = rawHtml
                    .replace("{{fullName}}", escapeHtml(fullName))
                    .replace("{{email}}", escapeHtml(email))
                    .replace("{{password}}", escapeHtml(password))
                    .replace("{{phoneNumber}}", escapeHtml(phoneNumber))
                    .replace("{{consultantId}}", escapeHtml(consultantId))
                    .replace("{{link}}", escapeHtml(referralLink));



            // 3️⃣ Send email
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper sender = new MimeMessageHelper(message, true, "UTF-8");

            sender.setTo(to);
            sender.setFrom("micheal.okafor.79677@gmail.com");
            sender.setSubject("🎉 Welcome to Satisfyre!");
            sender.setText(processedHtml, true);

            javaMailSender.send(message);
            log.info("✅ Welcome email sent to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to {}: {}", to, e.getMessage(), e);
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Minimal HTML escaping to prevent injection.
     */
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String firstName, String lastName, String resetLink) {
        try {
            String subject = "Password Reset Request";

            // HTML email template
            String htmlBody = "<!DOCTYPE html>"
                    + "<html lang='en'>"
                    + "<head>"
                    + "<meta charset='UTF-8'/>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>"
                    + "<title>Password Reset</title>"
                    + "<style>"
                    + "body {margin:0; padding:0; background-color:#f4f4f5; font-family:'Segoe UI', sans-serif;}"
                    + "table {border-spacing:0;}"
                    + "a {text-decoration:none;}"
                    + ".btn {display:inline-block; padding:12px 24px; background-color:#5b21b6; color:#fff; border-radius:6px; font-weight:bold; margin-top:20px;}"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<table width='100%' bgcolor='#f4f4f5' cellpadding='0' cellspacing='0'>"
                    + "<tr><td align='center' style='padding:40px 10px;'>"
                    + "<table width='600' cellpadding='0' cellspacing='0' bgcolor='#ffffff' style='border-radius:12px; overflow:hidden; box-shadow:0 5px 20px rgba(0,0,0,0.08);'>"
                    + "<tr><td align='center' bgcolor='#5b21b6' style='padding:40px 20px;'>"
                    + "<h1 style='color:#fff; font-size:24px; margin:0;'>Password Reset Request</h1>"
                    + "<p style='color:#dcd7f5; font-size:15px; margin:5px 0 0;'>Securely reset your password below 🔒</p>"
                    + "</td></tr>"
                    + "<tr><td style='padding:30px 40px;'>"
                    + "<p style='font-size:16px;'>Hello <strong>" + firstName + " " + lastName + "</strong>,</p>"
                    + "<p style='font-size:15px; line-height:1.6;'>We received a request to reset your password. Click the button below to proceed:</p>"
                    + "<p style='text-align:center;'><a href='" + resetLink + "' class='btn'>Reset Password</a></p>"
                    + "<p style='font-size:15px; line-height:1.6; margin-top:25px;'>If you did not request this, please ignore this email. Your account is safe.</p>"
                    + "<p style='font-size:15px;'>Best regards,<br><strong>The Satisfyre Team</strong></p>"
                    + "</td></tr>"
                    + "<tr><td align='center' bgcolor='#f9fafb' style='padding:20px;'>"
                    + "<p style='font-size:12px; color:#aaa;'>&copy; 2025 Satisfyre Inc. All rights reserved.</p>"
                    + "</td></tr>"
                    + "</table></td></tr></table>"
                    + "</body></html>";

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom("micheal.okafor.79677@gmail.com");
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content

            javaMailSender.send(message);
            log.info("✅ Password reset email sent to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to {}: {}", to, e.getMessage(), e);
        }
    }





    @Override
    public void sendSms() {

    }

    @Override
    public void sendWhatsapp() {

    }
}

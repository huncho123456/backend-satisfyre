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
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            // 1️⃣ Fetch the HTML template from frontend

            URL url = new URL(FRONT_ENDPOINT + "/html/reset-email.html");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Satisfyre-Mailer/1.0");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                log.error("Failed to fetch password reset email template. HTTP status: {}", conn.getResponseCode());
                return;
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String rawHtml = reader.lines().collect(Collectors.joining("\n"));

            // 2️⃣ Replace placeholders
            String processedHtml = rawHtml
                    .replace("{{firstName}}", escapeHtml(firstName))
                    .replace("{{lastName}}", escapeHtml(lastName))
                    .replace("{{resetLink}}", escapeHtml(resetLink));

            // 3️⃣ Send email
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom("micheal.okafor.79677@gmail.com");
            helper.setSubject("Password Reset Request");
            helper.setText(processedHtml, true);

            javaMailSender.send(message);
            log.info("✅ Password reset email sent to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to {}: {}", to, e.getMessage(), e);
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }






    @Override
    public void sendSms() {

    }

    @Override
    public void sendWhatsapp() {

    }
}

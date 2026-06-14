package com.example.taxi;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender emailSender, TemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@taxidispatch.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            // Dummy logger - in order to not crash if credentials are dummy
            System.out.println("========== MOCK EMAIL SENT ==========");
            System.out.println("TO: " + to);
            System.out.println("SUBJECT: " + subject);
            System.out.println("TEXT: " + text);
            System.out.println("=====================================");

            // Dispatch real email via SMTP
            emailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendOtpEmail(String to, String username, String otpCode) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("otpCode", otpCode);

            String process = templateEngine.process("email-otp", context);
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom("noreply@taxidispatch.com");
            helper.setText(process, true); // true = isHtml
            helper.setTo(to);
            helper.setSubject("Verify Your Taxi AI Dispatch Account");

            emailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }
}

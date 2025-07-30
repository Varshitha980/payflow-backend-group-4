package com.payflow.payflow.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            // Debug logging
            System.out.println("📧 Attempting to send email:");
            System.out.println("   From: " + fromEmail);
            System.out.println("   To: " + to);
            System.out.println("   Subject: " + subject);
            System.out.println("   Content: " + text.substring(0, Math.min(100, text.length())) + "...");
            
            if (to == null || to.trim().isEmpty()) {
                System.err.println("❌ Cannot send email: recipient email is null or empty");
                return;
            }
            
            // ACTUAL EMAIL SENDING
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to: " + to);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

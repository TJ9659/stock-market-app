package com.marketsense.auth;

import com.marketsense.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.util.Locale;

@Component
public class VerificationMailer {

    static final String SUBJECT = "Verify your MarketSense email";

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final String from;
    private final String frontendUrl;

    public VerificationMailer(JavaMailSender mailSender, ITemplateEngine templateEngine,
                              @Value("${app.mail-from}") String from,
                              @Value("${app.frontend-url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    public void send(User user, String rawToken, Duration ttl) {
        String link = verifyUrl(rawToken);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(user.getEmail());
            helper.setSubject(SUBJECT);
            helper.setText(plainText(user, link, ttl), render(user, link, ttl));
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailPreparationException("Could not build verification email for user " + user.getId(), e);
        }
    }

    String verifyUrl(String rawToken) {
        return frontendUrl + "/verify-email?token=" + rawToken;
    }

    String render(User user, String link, Duration ttl) {
        Context context = new Context(Locale.ENGLISH);
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("verifyUrl", link);
        context.setVariable("expiresIn", expiresIn(ttl));
        return templateEngine.process("email/verify-email", context);
    }

    String plainText(User user, String link, Duration ttl) {
        String greeting = user.getFirstName() == null ? "Hi," : "Hi " + user.getFirstName() + ",";
        return greeting + "\n\nConfirm your email to finish setting up your MarketSense account:\n" + link
                + "\n\nThis link expires in " + expiresIn(ttl) + ". If you didn't sign up, you can ignore this email.\n";
    }

    static String expiresIn(Duration ttl) {
        long hours = ttl.toHours();
        if (hours >= 1) return hours + (hours == 1 ? " hour" : " hours");
        long minutes = Math.max(1, ttl.toMinutes());
        return minutes + (minutes == 1 ? " minute" : " minutes");
    }
}

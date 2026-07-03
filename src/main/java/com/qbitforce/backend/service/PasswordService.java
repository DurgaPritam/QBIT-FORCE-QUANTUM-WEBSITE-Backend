package com.qbitforce.backend.service;

import com.qbitforce.backend.config.AppUrlProperties;
import com.qbitforce.backend.dto.ChangePasswordRequest;
import com.qbitforce.backend.dto.ForgotPasswordRequest;
import com.qbitforce.backend.dto.ResetPasswordRequest;
import com.qbitforce.backend.entity.AdminUser;
import com.qbitforce.backend.entity.PasswordResetToken;
import com.qbitforce.backend.repository.AdminUserRepository;
import com.qbitforce.backend.repository.PasswordResetTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordService.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final AppUrlProperties appUrlProperties;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public PasswordService(
            AdminUserRepository adminUserRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            AppUrlProperties appUrlProperties) {
        this.adminUserRepository = adminUserRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.appUrlProperties = appUrlProperties;
    }

    public void requestReset(ForgotPasswordRequest request) {
        adminUserRepository.findByEmailIgnoreCase(request.email().trim()).ifPresent(admin -> {
            String tokenValue = UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken token = new PasswordResetToken();
            token.setToken(tokenValue);
            token.setAdminUserId(admin.getId());
            token.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
            tokenRepository.save(token);

            sendResetEmail(admin.getEmail(), tokenValue);
        });
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository
                .findByTokenAndUsedFalse(request.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset link."));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset link has expired.");
        }

        AdminUser admin = adminUserRepository
                .findById(token.getAdminUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset link."));

        admin.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        adminUserRepository.save(admin);

        token.setUsed(true);
        tokenRepository.save(token);
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        AdminUser admin = adminUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not found."));

        if (!passwordEncoder.matches(request.currentPassword(), admin.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }

        admin.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        adminUserRepository.save(admin);
    }

    private void sendResetEmail(String toEmail, String token) {
        if (mailUsername == null || mailUsername.isBlank()) {
            log.warn("Mail not configured — reset token for {}: {}", toEmail, token);
            return;
        }

        String resetUrl = appUrlProperties.getBaseUrl() + "/qbitadmin-2026-login/reset?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom(mailUsername);
            helper.setSubject("Qbit Force Admin — Reset Your Password");
            helper.setText(
                    """
                    You requested a password reset for the Qbit Force admin panel.

                    Click the link below to set a new password (valid for 1 hour):
                    %s

                    If you did not request this, ignore this email.
                    """
                            .formatted(resetUrl),
                    false);
            mailSender.send(message);
        } catch (MessagingException ex) {
            log.error("Failed to send password reset email to {}", toEmail, ex);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Could not send reset email. Try again later.");
        }
    }
}

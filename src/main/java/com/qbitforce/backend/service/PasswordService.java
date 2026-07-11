package com.qbitforce.backend.service;

import com.qbitforce.backend.config.AppUrlProperties;
import com.qbitforce.backend.dto.ChangePasswordRequest;
import com.qbitforce.backend.dto.ForgotPasswordRequest;
import com.qbitforce.backend.dto.ResetPasswordRequest;
import com.qbitforce.backend.entity.AdminUser;
import com.qbitforce.backend.entity.PasswordResetToken;
import com.qbitforce.backend.repository.AdminUserRepository;
import com.qbitforce.backend.repository.PasswordResetTokenRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordService.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUrlProperties appUrlProperties;

    public PasswordService(
            AdminUserRepository adminUserRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            AppUrlProperties appUrlProperties) {
        this.adminUserRepository = adminUserRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
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

            String resetUrl = appUrlProperties.getBaseUrl() + "/qbitadmin-2026-login/reset?token=" + tokenValue;
            log.info("Password reset link for {}: {}", admin.getEmail(), resetUrl);
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
}

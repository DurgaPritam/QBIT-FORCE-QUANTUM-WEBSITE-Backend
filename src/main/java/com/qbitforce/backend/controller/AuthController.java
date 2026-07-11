package com.qbitforce.backend.controller;

import com.qbitforce.backend.dto.ApiMessage;
import com.qbitforce.backend.dto.ForgotPasswordRequest;
import com.qbitforce.backend.dto.LoginRequest;
import com.qbitforce.backend.dto.LoginResponse;
import com.qbitforce.backend.dto.ResetPasswordRequest;
import com.qbitforce.backend.service.AuthService;
import com.qbitforce.backend.service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordService passwordService;

    public AuthController(AuthService authService, PasswordService passwordService) {
        this.authService = authService;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, resolveClientIp(httpRequest));
    }

    @PostMapping("/forgot-password")
    public ApiMessage forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordService.requestReset(request);
        return new ApiMessage("If that email is registered, a password reset link was created. Check the server logs.");
    }

    @PostMapping("/reset-password")
    public ApiMessage resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return new ApiMessage("Password updated successfully. You can sign in now.");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

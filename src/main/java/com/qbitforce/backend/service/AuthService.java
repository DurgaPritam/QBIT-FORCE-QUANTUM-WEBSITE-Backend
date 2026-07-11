package com.qbitforce.backend.service;

import com.qbitforce.backend.config.JwtProperties;
import com.qbitforce.backend.dto.LoginRequest;
import com.qbitforce.backend.dto.LoginResponse;
import com.qbitforce.backend.entity.AdminUser;
import com.qbitforce.backend.repository.AdminUserRepository;
import com.qbitforce.backend.security.JwtService;
import com.qbitforce.backend.util.SlidingWindowRateLimiter;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AdminUserRepository adminUserRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SlidingWindowRateLimiter rateLimiter =
            new SlidingWindowRateLimiter(10, TimeUnit.MINUTES.toMillis(15));

    public AuthService(
            AuthenticationManager authenticationManager,
            AdminUserRepository adminUserRepository,
            JwtService jwtService,
            JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.adminUserRepository = adminUserRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public LoginResponse login(LoginRequest request, String clientIp) {
        String loginId = request.username().trim();
        String key = clientIp + ":" + loginId.toLowerCase();
        if (!rateLimiter.allow(key)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        rateLimiter.reset(key);

        AdminUser admin = adminUserRepository
                .findByUsername(loginId)
                .or(() -> adminUserRepository.findByEmailIgnoreCase(loginId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));

        String token = jwtService.generateToken(admin);
        return new LoginResponse(token, "Bearer", jwtProperties.getExpirationMs(), admin.getUsername());
    }
}

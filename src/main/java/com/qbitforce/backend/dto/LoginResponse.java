package com.qbitforce.backend.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, String username) {}

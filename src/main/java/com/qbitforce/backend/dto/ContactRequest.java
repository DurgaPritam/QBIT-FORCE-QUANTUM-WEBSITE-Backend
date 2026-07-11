package com.qbitforce.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 200) String email,
        @Size(max = 50) String phone,
        @Size(max = 200) String company,
        @NotBlank @Size(max = 80) String inquiryType,
        @NotBlank @Size(max = 5000) String message) {}

package com.qbitforce.backend.dto;

import com.qbitforce.backend.entity.ContactSubmission;
import java.time.Instant;

public record ContactSubmissionDto(
        Long id,
        String name,
        String email,
        String phone,
        String company,
        String inquiryType,
        String message,
        boolean emailSent,
        boolean read,
        Instant createdAt) {

    public static ContactSubmissionDto fromEntity(ContactSubmission submission) {
        return new ContactSubmissionDto(
                submission.getId(),
                submission.getName(),
                submission.getEmail(),
                submission.getPhone(),
                submission.getCompany(),
                submission.getInquiryType(),
                submission.getMessage(),
                submission.isEmailSent(),
                submission.isRead(),
                submission.getCreatedAt());
    }
}

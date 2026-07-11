package com.qbitforce.backend.service;

import com.qbitforce.backend.dto.ContactRequest;
import com.qbitforce.backend.entity.ContactSubmission;
import com.qbitforce.backend.repository.ContactSubmissionRepository;
import com.qbitforce.backend.util.SlidingWindowRateLimiter;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactService {

    private final ContactSubmissionRepository repository;
    private final SlidingWindowRateLimiter rateLimiter =
            new SlidingWindowRateLimiter(5, TimeUnit.HOURS.toMillis(1));

    public ContactService(ContactSubmissionRepository repository) {
        this.repository = repository;
    }

    public void submit(ContactRequest request, String clientIp) {
        if (!rateLimiter.allow(clientIp)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many submissions. Try again later.");
        }

        ContactSubmission submission = new ContactSubmission();
        submission.setName(request.name().trim());
        submission.setEmail(request.email().trim().toLowerCase());
        submission.setPhone(request.phone() != null ? request.phone().trim() : "");
        submission.setCompany(request.company() != null ? request.company().trim() : "");
        submission.setInquiryType(request.inquiryType().trim());
        submission.setMessage(request.message().trim());
        repository.save(submission);
    }
}

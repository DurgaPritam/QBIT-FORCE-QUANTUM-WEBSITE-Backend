package com.qbitforce.backend.service;

import com.qbitforce.backend.config.ContactProperties;
import com.qbitforce.backend.dto.ContactRequest;
import com.qbitforce.backend.entity.ContactSubmission;
import com.qbitforce.backend.repository.ContactSubmissionRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactSubmissionRepository repository;
    private final JavaMailSender mailSender;
    private final ContactProperties contactProperties;
    private final ConcurrentHashMap<String, AtomicInteger> submitAttempts = new ConcurrentHashMap<>();

    public ContactService(
            ContactSubmissionRepository repository,
            JavaMailSender mailSender,
            ContactProperties contactProperties) {
        this.repository = repository;
        this.mailSender = mailSender;
        this.contactProperties = contactProperties;
    }

    public void submit(ContactRequest request, String clientIp) {
        String key = clientIp;
        int attempts = submitAttempts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        if (attempts > 5) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many submissions. Try again later.");
        }

        ContactSubmission submission = new ContactSubmission();
        submission.setName(request.name().trim());
        submission.setEmail(request.email().trim().toLowerCase());
        submission.setPhone(request.phone().trim());
        submission.setCompany(request.company() != null ? request.company().trim() : "");
        submission.setInquiryType(request.inquiryType().trim());
        submission.setMessage(request.message().trim());
        repository.save(submission);

        boolean sent = sendEmail(submission);
        submission.setEmailSent(sent);
        repository.save(submission);
    }

    private boolean sendEmail(ContactSubmission submission) {
        String mailUsername = contactProperties.getFromEmail();
        if (mailUsername == null || mailUsername.isBlank()) {
            log.warn("Mail not configured — contact saved to database only (id={})", submission.getId());
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(contactProperties.getToEmail());
            helper.setFrom(contactProperties.getFromEmail());
            helper.setReplyTo(submission.getEmail());
            helper.setSubject("Contact Enquiry — " + submission.getInquiryType());
            helper.setText(buildEmailBody(submission), false);
            mailSender.send(message);
            return true;
        } catch (MessagingException ex) {
            log.error("Failed to send contact email for submission {}", submission.getId(), ex);
            return false;
        }
    }

    private String buildEmailBody(ContactSubmission submission) {
        return """
                New contact enquiry from qbitforcequantum.com

                Name: %s
                Email: %s
                Phone: %s
                Company: %s
                Topic: %s

                Message:
                %s
                """
                .formatted(
                        submission.getName(),
                        submission.getEmail(),
                        submission.getPhone(),
                        submission.getCompany(),
                        submission.getInquiryType(),
                        submission.getMessage());
    }
}

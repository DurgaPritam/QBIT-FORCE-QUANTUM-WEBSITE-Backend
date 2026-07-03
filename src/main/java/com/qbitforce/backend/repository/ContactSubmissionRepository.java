package com.qbitforce.backend.repository;

import com.qbitforce.backend.entity.ContactSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactSubmissionRepository extends JpaRepository<ContactSubmission, Long> {

    long countByReadFalse();
}

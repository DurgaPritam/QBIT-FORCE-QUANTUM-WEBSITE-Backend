package com.qbitforce.backend.repository;

import com.qbitforce.backend.entity.AdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);

    Optional<AdminUser> findByEmailIgnoreCase(String email);
}

package com.qbitforce.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "seed_flags")
public class SeedFlag {

    @Id
    @Column(length = 80)
    private String id;

    @Column(nullable = false)
    private Instant completedAt = Instant.now();

    public SeedFlag() {}

    public SeedFlag(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}

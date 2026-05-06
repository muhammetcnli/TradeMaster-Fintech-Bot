package com.trademaster.fintech_core.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    // add the standard precision and scale
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false)
    private String authProvider; // "TELEGRAM", "GOOGLE" vb.

    @Column(nullable = false, unique = true)
    private String externalId;

    @Column(name = "session_token_hash", unique = true)
    private String sessionTokenHash;

    @Column(name = "session_token_issued_at")
    private Instant sessionTokenIssuedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // add one-to-many relationship
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAsset> assets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
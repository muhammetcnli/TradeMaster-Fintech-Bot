package com.trademaster.fintech_core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_assets")
public class UserAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // add many-to-one relation to user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // add many-to-one relation to asset
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    // To enable many-to-many relationship between user and asset

    @Column(name = "entity_price")
    private BigDecimal entityPrice;

    @Column(name = "alert_enabled")
    private boolean alertEnabled;

    @CreationTimestamp
    @Column(name = "added_at")
    private LocalDateTime addedAt;
}

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

    // add quantity with scale 8 and precision 19
    @Column(name = "quantity", precision = 19, scale = 8)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "average_buy_price", precision = 19, scale = 4)
    private BigDecimal averageBuyPrice = BigDecimal.ZERO;

    @Column(name = "alert_enabled")
    private boolean alertEnabled;

    @CreationTimestamp
    @Column(name = "added_at")
    private LocalDateTime addedAt;
}

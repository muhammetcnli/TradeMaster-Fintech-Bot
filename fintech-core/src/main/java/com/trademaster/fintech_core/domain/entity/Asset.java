package com.trademaster.fintech_core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "name")
    private String name;

    private enum type
    {
        STOCK, CRYPTO, FOREX
    };

    @Column(name = "is_active")
    private Boolean isActive;
}

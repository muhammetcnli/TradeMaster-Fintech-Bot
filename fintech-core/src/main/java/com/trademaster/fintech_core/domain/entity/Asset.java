package com.trademaster.fintech_core.domain.entity;

import com.trademaster.fintech_core.domain.AssetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "asset")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType type;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "asset")
    private Set<UserAsset> followedBy = new HashSet<>();
}

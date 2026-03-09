package com.trademaster.fintech_core.entity;

import com.trademaster.fintech_core.dto.AssetType;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType assetType;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL)
    private List<UserAsset> userHoldings = new ArrayList<>();
}
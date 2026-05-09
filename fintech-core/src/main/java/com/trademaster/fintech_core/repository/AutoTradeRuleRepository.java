package com.trademaster.fintech_core.repository;

import com.trademaster.fintech_core.entity.AutoTradeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AutoTradeRuleRepository extends JpaRepository<AutoTradeRule, UUID> {
    List<AutoTradeRule> findAllByActiveTrue();
}

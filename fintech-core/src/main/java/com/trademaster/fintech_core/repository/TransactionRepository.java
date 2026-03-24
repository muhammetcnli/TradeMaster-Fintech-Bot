package com.trademaster.fintech_core.repository;

import com.trademaster.fintech_core.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAllByUserId(UUID userId);
}


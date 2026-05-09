package com.trademaster.fintech_core.repository;

import com.trademaster.fintech_core.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findAllByActiveTrue();
    List<Alert> findAllByUserIdAndActiveTrue(UUID userId);
}

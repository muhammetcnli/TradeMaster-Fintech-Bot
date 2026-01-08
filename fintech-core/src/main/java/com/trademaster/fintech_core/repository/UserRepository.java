package com.trademaster.fintech_core.repository;

import com.trademaster.fintech_core.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {

    Optional<User> findByChatId(Long chatId);
}

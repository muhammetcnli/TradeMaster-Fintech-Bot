package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.domain.entity.User;
import com.trademaster.fintech_core.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j // for logging
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Registers user to the system.
     * If user already registered, it returns the existing user.
     */
    @Transactional
    public User registerUser(Long chatId, String username, String firstName, String lastName){

        // check the database if the user exists
        Optional<User> existingUser = userRepository.findByChatId(chatId);

        // if user exists
        if (existingUser.isPresent()){
            log.info("User already registered: {}", username);
            return existingUser.get();
        }

        // create a new user and set fields
        User newUser = new User();
        newUser.setChatId(chatId);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setUsername(username);

        // save user to repository
        User savedUser = userRepository.save(newUser);
        log.info("New user registered: Id:{}, User:{}", savedUser.getId(), username);
        return savedUser;
    }


}

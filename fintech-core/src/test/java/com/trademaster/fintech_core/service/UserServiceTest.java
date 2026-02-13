package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.domain.entity.User;
import com.trademaster.fintech_core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Test
    void registerUserReturnsExistingUser() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        User existing = new User();
        existing.setUsername("test");
        when(userRepository.findByChatId(1L)).thenReturn(Optional.of(existing));

        User result = userService.registerUser(1L, "test", "t", "u");
        assertSame(existing, result);
    }

    @Test
    void registerUserCreatesNewUserWhenMissing() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        when(userRepository.findByChatId(2L)).thenReturn(Optional.empty());
        when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerUser(2L, "new", "n", "u");
        assertEquals("new", result.getUsername());
    }
}

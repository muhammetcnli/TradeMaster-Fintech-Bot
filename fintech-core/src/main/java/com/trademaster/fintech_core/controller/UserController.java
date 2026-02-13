package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.domain.dto.UserRegisterRequest;
import com.trademaster.fintech_core.domain.dto.UserResponse;
import com.trademaster.fintech_core.domain.entity.User;
import com.trademaster.fintech_core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Registers user to the system.
     * If user already registered, it returns the existing user.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRegisterRequest request) {
        User user = userService.registerUser(
                request.chatId(),
                request.username(),
                request.firstName(),
                request.lastName()
        );
        return ResponseEntity.ok(toResponse(user));
    }

    /**
     * Returns a user by id.
     * It returns 404 when the user does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(value -> ResponseEntity.ok(toResponse(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Returns a user by chat id.
     * It returns 404 when the user does not exist.
     */
    @GetMapping("/by-chat/{chatId}")
    public ResponseEntity<UserResponse> getByChatId(@PathVariable Long chatId) {
        Optional<User> user = userService.getUserByChatId(chatId);
        return user.map(value -> ResponseEntity.ok(toResponse(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getChatId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsdBalance(),
                user.getRegisteredAt(),
                user.getIsActive()
        );
    }
}

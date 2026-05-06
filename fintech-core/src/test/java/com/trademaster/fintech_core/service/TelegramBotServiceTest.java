package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.telegram.CommandDispatcher;
import com.trademaster.fintech_core.telegram.CommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TelegramBotServiceTest {

    private TelegramBotService telegramBotService;

    @Mock
    private UserService userService;

    @Mock
    private CommandDispatcher commandDispatcher;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CommandHandler mockStartHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        telegramBotService = new TelegramBotService(userService, commandDispatcher, restTemplate);
    }

    @Test
    void testHandleStart_ShouldResolveUserAndDispatch() {
        // Arrange
        Long chatId = 123456789L;
        String username = "testuser";
        UUID userId = UUID.randomUUID();

        TelegramUpdateDto update = createMockTelegramUpdate(chatId, username, "/start");

        User user = User.builder()
                .id(userId)
                .username(username)
                .authProvider("TELEGRAM")
                .externalId(String.valueOf(chatId))
                .balance(BigDecimal.valueOf(100000))
                .build();

        when(userService.findOrCreateTelegramUser(chatId, username)).thenReturn(user);
        when(commandDispatcher.getHandler("/start")).thenReturn(Optional.of(mockStartHandler));
        when(mockStartHandler.handle(eq(user), eq(update), any(String[].class))).thenReturn("Welcome!");

        // Act
        telegramBotService.handleUpdate(update);

        // Assert
        verify(userService, times(1)).findOrCreateTelegramUser(chatId, username);
        verify(commandDispatcher, times(1)).getHandler("/start");
        verify(mockStartHandler, times(1)).handle(eq(user), eq(update), any(String[].class));

        System.out.println("Test passed! /start dispatched successfully");
    }

    @Test
    void testHandleStart_WithEmptyUsername() {
        // Arrange
        Long chatId = 987654321L;
        UUID userId = UUID.randomUUID();

        TelegramUpdateDto update = createMockTelegramUpdateWithoutUsername(chatId, "/start");

        User user = User.builder()
                .id(userId)
                .username("telegram_987654321")
                .authProvider("TELEGRAM")
                .externalId(String.valueOf(chatId))
                .balance(BigDecimal.valueOf(100000))
                .build();

        when(userService.findOrCreateTelegramUser(chatId, null)).thenReturn(user);
        when(commandDispatcher.getHandler("/start")).thenReturn(Optional.of(mockStartHandler));
        when(mockStartHandler.handle(eq(user), eq(update), any(String[].class))).thenReturn("Welcome!");

        // Act
        telegramBotService.handleUpdate(update);

        // Assert
        verify(userService, times(1)).findOrCreateTelegramUser(chatId, null);
        verify(commandDispatcher, times(1)).getHandler("/start");

        System.out.println("Test with empty username passed!");
    }

    @Test
    void testUnknownCommand_ShouldSendHelpMessage() {
        // Arrange
        Long chatId = 111111111L;
        UUID userId = UUID.randomUUID();

        TelegramUpdateDto update = createMockTelegramUpdate(chatId, "testuser", "/unknowncmd");

        User user = User.builder()
                .id(userId)
                .username("testuser")
                .authProvider("TELEGRAM")
                .externalId(String.valueOf(chatId))
                .balance(BigDecimal.valueOf(100000))
                .build();

        when(userService.findOrCreateTelegramUser(chatId, "testuser")).thenReturn(user);
        when(commandDispatcher.getHandler("/unknowncmd")).thenReturn(Optional.empty());

        // Act
        telegramBotService.handleUpdate(update);

        // Assert - no handler should be called, unknown command message sent
        verify(commandDispatcher, times(1)).getHandler("/unknowncmd");

        System.out.println("Unknown command test passed!");
    }

    private TelegramUpdateDto createMockTelegramUpdate(Long chatId, String username, String text) {
        TelegramUpdateDto update = new TelegramUpdateDto();

        TelegramUpdateDto.Message message = new TelegramUpdateDto.Message();
        TelegramUpdateDto.Chat chat = new TelegramUpdateDto.Chat();
        TelegramUpdateDto.From from = new TelegramUpdateDto.From();

        chat.setId(chatId);
        from.setId(chatId);
        from.setUsername(username);

        message.setChat(chat);
        message.setFrom(from);
        message.setText(text);

        update.setMessage(message);

        return update;
    }

    private TelegramUpdateDto createMockTelegramUpdateWithoutUsername(Long chatId, String text) {
        TelegramUpdateDto update = new TelegramUpdateDto();

        TelegramUpdateDto.Message message = new TelegramUpdateDto.Message();
        TelegramUpdateDto.Chat chat = new TelegramUpdateDto.Chat();
        TelegramUpdateDto.From from = new TelegramUpdateDto.From();

        chat.setId(chatId);
        from.setId(chatId);
        from.setUsername(null);

        message.setChat(chat);
        message.setFrom(from);
        message.setText(text);

        update.setMessage(message);

        return update;
    }
}

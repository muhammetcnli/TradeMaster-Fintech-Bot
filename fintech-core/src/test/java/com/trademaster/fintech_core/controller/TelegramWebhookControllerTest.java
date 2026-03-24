package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.service.TelegramBotService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TelegramWebhookControllerTest {

    @Test
    void shouldReturnForbiddenWhenSecretIsConfiguredAndHeaderIsWrong() {
        TelegramBotService botService = mock(TelegramBotService.class);
        TelegramWebhookController controller = new TelegramWebhookController(botService);
        ReflectionTestUtils.setField(controller, "webhookSecret", "test-secret");

        ResponseEntity<Void> response = controller.handleWebhook(new TelegramUpdateDto(), "wrong-secret");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(botService, never()).handleUpdate(any());
    }

    @Test
    void shouldReturnOkWhenSecretMatches() {
        TelegramBotService botService = mock(TelegramBotService.class);
        TelegramWebhookController controller = new TelegramWebhookController(botService);
        ReflectionTestUtils.setField(controller, "webhookSecret", "test-secret");

        ResponseEntity<Void> response = controller.handleWebhook(new TelegramUpdateDto(), "test-secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(botService, times(1)).handleUpdate(any());
    }
}


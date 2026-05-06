package com.trademaster.fintech_core.telegram;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Route Telegram commands to appropriate handlers.
 * Extensible for adding future command handlers.
 */
@Component
public class CommandDispatcher {

    private final Map<String, CommandHandler> handlers = new HashMap<>();

    public CommandDispatcher(List<CommandHandler> commandHandlers) {
        for (CommandHandler handler : commandHandlers) {
            handlers.put(handler.getCommand(), handler);
        }
    }

    /**
     * Get a handler for the given command.
     * @param command The command string (e.g., "/start", "/balance")
     * @return Optional handler
     */
    public Optional<CommandHandler> getHandler(String command) {
        return Optional.ofNullable(handlers.get(command.toLowerCase()));
    }

    /**
     * List all registered commands
     */
    public List<String> getRegisteredCommands() {
        return List.copyOf(handlers.keySet());
    }
}


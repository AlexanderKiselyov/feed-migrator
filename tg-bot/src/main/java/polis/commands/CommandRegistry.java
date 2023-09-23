package polis.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;

public interface CommandRegistry<C extends IBotCommand> {
    C getRegisteredCommand(String commandIdentifier);
}

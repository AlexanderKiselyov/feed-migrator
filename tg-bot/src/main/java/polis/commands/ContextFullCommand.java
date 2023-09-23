package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface ContextFullCommand {
    void execute(AbsSender absSender, User user, Chat chat, String[] arguments);
}

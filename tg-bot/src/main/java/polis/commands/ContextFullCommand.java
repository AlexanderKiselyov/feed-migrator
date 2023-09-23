package polis.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface ContextFullCommand extends IBotCommand {
    void execute(AbsSender absSender, User user, Chat chat, Context context);
}

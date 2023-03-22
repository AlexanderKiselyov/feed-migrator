package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class AddTgChannel extends Command {
    private static final String ADD_TELEGRAM_CHANNEL = """
            Для добавления нового канала необходимо выполнить следующие действия:
            1. Добавить бота в администраторы Вашего телеграм-канала.
            2. Скопировать ссылку на телеграм-канал. Пример такой ссылки: https://t.me/exploitex
            3. Прислать ссылку в данный диалог.""";

    public AddTgChannel(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(), ADD_TELEGRAM_CHANNEL,
                null);
    }
}

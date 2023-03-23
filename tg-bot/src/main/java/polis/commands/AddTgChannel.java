package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.State;

import java.util.List;

public class AddTgChannel extends Command {
    private static final String ADD_TELEGRAM_CHANNEL = """
            Для добавления нового канала необходимо выполнить следующие действия:
            1. Добавить бота в администраторы Вашего телеграм-канала.
            2. Скопировать ссылку на телеграм-канал. Пример такой ссылки: https://t.me/exploitex
            3. Прислать ссылку в данный диалог.""";
    private static final int rowsCount = 1;
    private static final List<String> commandsForKeyboard = List.of(
            // TODO: Добавить проверку, и если канал успешно добавлен - добавить кнопку tg_channel_description
            State.MainMenu.getDescription()
    );

    public AddTgChannel(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswer(
                absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                ADD_TELEGRAM_CHANNEL,
                rowsCount,
                commandsForKeyboard, null,
                null);
    }
}

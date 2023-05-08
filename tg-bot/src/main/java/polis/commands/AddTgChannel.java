package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.State;

import java.util.List;

public class AddTgChannel extends Command {
    private static final String ADD_TELEGRAM_CHANNEL = """
            Для добавления нового канала необходимо выполнить следующие действия:
            1. Добавить бота в администраторы Вашего Телеграм-канала.
            2. Скопировать ссылку на Телеграм-канал. Пример такой ссылки: https://t.me/exploitex
            3. Прислать ссылку в данный диалог.""";
    private static final int ROWS_COUNT = 1;
    private static final List<String> commandsForKeyboard = List.of(State.MainMenu.getDescription());

    public AddTgChannel() {
        super(State.AddTgChannel.getIdentifier(), State.AddTgChannel.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                ADD_TELEGRAM_CHANNEL,
                ROWS_COUNT,
                commandsForKeyboard,
                loggingInfo(user.getUserName()));
    }
}

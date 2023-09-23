package polis.commands.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.DescribableCommand;
import polis.commands.context.Context;
import polis.util.IState;
import polis.util.State;

import java.util.List;

@Component
public class AddTgChannel extends Command implements DescribableCommand {
    private static final String ADD_TELEGRAM_CHANNEL = """
            Для добавления нового канала необходимо выполнить следующие действия:
            1. Добавить бота в администраторы Вашего Телеграмм-канала.
            2. Скопировать ссылку на Телеграмм-канал. Пример такой ссылки: https://t.me/exploitex
            3. Прислать ссылку в данный диалог.""";
    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS = List.of(State.MainMenu.getDescription());

    @Override
    public String helloMessage() {
        return ADD_TELEGRAM_CHANNEL;
    }

    @Override
    public List<IState> nextPossibleCommands() {
        return TRANSITION_TO_NON_COMMAND;
    }

    @Override
    public IState state() {
        return State.AddTgChannel;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                ADD_TELEGRAM_CHANNEL,
                ROWS_COUNT,
                KEYBOARD_COMMANDS,
                loggingInfo(user.getUserName()));
    }
}

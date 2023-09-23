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
public class MainMenu extends Command implements DescribableCommand {

    @Override
    public String helloMessage() {
        return "Добро пожаловать в главное меню!";
    }

    private static final String NEXT_COMMANDS_DESCRIPTION = """
            Здесь Вы можете посмотреть список добавленных Телеграмм-каналов по команде /%s.
            Кроме того, Вы можете добавить новый Телеграмм-канал для синхронизации по команде /%s.
            Справка по боту доступна по команде /%s.""";

    @Override
    public List<IState> nextPossibleCommands() {
        return List.of(
                State.TgChannelsList,
                State.AddTgChannel,
                State.Help
        );
    }

    private static final int ROWS_COUNT = 3;

    private static final List<String> KEYBOARD_COMMANDS = List.of(
            State.TgChannelsList.getDescription(),
            State.AddTgChannel.getDescription(),
            State.Help.getDescription()
    );

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                commandMainMessage(NEXT_COMMANDS_DESCRIPTION),
                ROWS_COUNT,
                KEYBOARD_COMMANDS,
                loggingInfo(user.getUserName()));
    }

    @Override
    public IState state() {
        return State.MainMenu;
    }
}

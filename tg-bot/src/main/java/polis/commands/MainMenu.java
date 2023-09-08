package polis.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.util.State;

import java.util.List;

@Component
public class MainMenu extends Command {
    private static final String MAIN_MENU = """
            Добро пожаловать в главное меню!
            Здесь Вы можете посмотреть список добавленных Телеграмм-каналов по команде /%s.
            Кроме того, Вы можете добавить новый Телеграмм-канал для синхронизации по команде /%s.
            Справка по боту доступна по команде /%s.""";
    private static final int ROWS_COUNT = 3;
    private static final List<String> KEYBOARD_COMMANDS = List.of(
            State.TgChannelsList.getDescription(),
            State.AddTgChannel.getDescription(),
            State.Help.getDescription()
    );

    public MainMenu(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.MainMenu.getIdentifier(), State.MainMenu.getDescription(), inlineKeyboard, replyKeyboard);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                String.format(MAIN_MENU, State.TgChannelsList.getIdentifier(), State.AddTgChannel.getIdentifier(),
                        State.Help.getIdentifier()),
                ROWS_COUNT,
                KEYBOARD_COMMANDS,
                loggingInfo(user.getUserName()));
    }
}

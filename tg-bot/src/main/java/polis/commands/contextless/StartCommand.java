package polis.commands.contextless;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.ContextLessCommand;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.util.IState;
import polis.util.State;

import java.util.List;

@Component
public class StartCommand extends Command implements ContextLessCommand {
    private static final String NEXT_COMMANDS_DESCRIPTION = """
            Введите /%s и добавьте новый Телеграмм-канал, из которого хотите публиковать посты в другие социальные сети.
            Справка по боту доступна по команде /%s.
            Вы также можете воспользоваться клавиатурой с командами.
            """;
    private static final int ROWS_COUNT = 2;
    private static final List<String> KEYBOARD_COMMANDS = List.of(
            State.AddTgChannel.getDescription(),
            State.Help.getDescription()
    );

    @Override
    public String helloMessage() {
        return "Давайте начнём! С помощью бота Вы можете синхронизировать Ваши Телеграмм-каналы " +
                "с группами Одноклассники или группами в ВКонтакте";
    }

    @Override
    public List<IState> nextPossibleCommands() {
        return List.of(
                State.AddTgChannel,
                State.Help
        );
    }

    public StartCommand(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.Start.getIdentifier(), State.Start.getDescription(), inlineKeyboard, replyKeyboard);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                commandMainMessage(NEXT_COMMANDS_DESCRIPTION),
                ROWS_COUNT,
                KEYBOARD_COMMANDS,
                loggingInfo(user.getUserName()));
    }
}

package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.State;

import java.util.List;

public class StartCommand extends Command {
    private static final String startAnswer = String.format("""
            Давайте начнём! С помощью бота Вы можете синхронизировать Ваш Telegram-канал с группой в Одноклассники или
            Вконтакте.
            Введите /%s и добавьте новый Телеграм-канал, из которого хотите публиковать посты в другие социальные сети.
            Или можете воспользоваться клавиатурой с командами.""", State.AddTgChannel.getIdentifier());
    private static final int rowsCount = 1;
    private static final List<String> commandsForKeyboard = List.of(
            State.AddTgChannel.getDescription()
    );

    public StartCommand() {
        super(State.Start.getIdentifier(), State.Start.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswer(
                absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                startAnswer,
                rowsCount,
                commandsForKeyboard,
                null);
    }
}

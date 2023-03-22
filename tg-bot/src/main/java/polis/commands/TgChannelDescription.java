package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.telegram.TelegramDataCheck;
import polis.util.State;
import polis.util.Substate;

import java.util.List;
import java.util.Map;

public class TgChannelDescription extends Command {
    // TODO добавить описание вариантов дальнейших действий
    private static final String TELEGRAM_CHANNEL_DESCRIPTION = """
            Текущий выбранный телеграм-канал <b>%s</b>.
            """;
    private static final String NOT_VALID_CHANNEL = String.format("""
            Телеграм-канал не был выбран.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""",
            State.MainMenu.getIdentifier());
    private final Map<Long, String> currentTgChannel;
    private final TelegramDataCheck telegramDataCheck;
    private static final int rowsCount = 1;
    private static final List<String> commandsForKeyboard = List.of(
            State.MainMenu.getDescription() // TODO: Добавить синхр. группы и добавление группы
    );

    public TgChannelDescription(String commandIdentifier, String description, Map<Long, String> currentTgChannel) {
        super(commandIdentifier, description);
        this.currentTgChannel = currentTgChannel;
        telegramDataCheck = new TelegramDataCheck();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentTgChannel.containsKey(chat.getId()) && !currentTgChannel.get(chat.getId()).isEmpty()) {
            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(TELEGRAM_CHANNEL_DESCRIPTION,
                            telegramDataCheck.getChatTitle(currentTgChannel.get(chat.getId()))),
                    rowsCount,
                    commandsForKeyboard);
        } else {
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(), NOT_VALID_CHANNEL,
                    rowsCount, commandsForKeyboard);
        }
    }
}

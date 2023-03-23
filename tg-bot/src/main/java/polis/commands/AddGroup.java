package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.telegram.TelegramDataCheck;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.Map;

public class AddGroup extends Command {
    private static final String ADD_GROUP = """
            Меню добавления групп для Телеграм-канала <b>%s</b>.""";
    private static final String NOT_VALID_TG_CHANNEL = """
            Невозможно получить информацию по текущему телеграм-каналу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private final TelegramDataCheck telegramDataCheck;

    private final Map<Long, TelegramChannel> currentTgChannel;

    public AddGroup(String commandIdentifier, String description, Map<Long, TelegramChannel> currentTgChannel) {
        super(commandIdentifier, description);
        this.currentTgChannel = currentTgChannel;
        telegramDataCheck = new TelegramDataCheck();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentTgChannel.containsKey(chat.getId())) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(ADD_GROUP,
                            telegramDataCheck.getChatTitle(currentTgChannel.get(chat.getId()).getTelegramChannelId())),
                    rowsCount,
                    commandsForKeyboard,
                    null);
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_TG_CHANNEL, State.MainMenu.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard,
                    null);
        }
    }
}

package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class Autoposting extends Command {
    private static final String AUTOPOSTING = """
            Функция автопостинга позволяет автоматически публиковать новый пост из Телеграм-канала в группу.
            """;
    private static final String AUTOPOSTING_INLINE = """
            Включить данную функцию?""";
    private static final String NO_CURRENT_TG_CHANNEL = """
            Телеграм-канал не был выбран.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, TelegramChannel> currentTgChannel;
    private static final int rowsCount = 1;

    public Autoposting(String commandIdentifier, String description, Map<Long, TelegramChannel> currentTgChannel) {
        super(commandIdentifier, description);
        this.currentTgChannel = currentTgChannel;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentTgChannel.get(chat.getId()) != null) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    AUTOPOSTING,
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    AUTOPOSTING_INLINE,
                    rowsCount,
                    commandsForKeyboard,
                    getIfAddAutoposting(currentTgChannel.get(chat.getId()).getTelegramChannelId()));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NO_CURRENT_TG_CHANNEL, State.MainMenu.getIdentifier()),
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    private String[] getIfAddAutoposting(Long id) {
        return new String[]{
                "Да",
                String.format("autoposting %s 0", id),
                "Нет",
                String.format("autoposting %s 1", id)
        };
    }
}
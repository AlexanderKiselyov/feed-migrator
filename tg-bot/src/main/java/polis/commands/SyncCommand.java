package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.AuthData;
import polis.util.State;

import java.util.List;
import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class SyncCommand extends Command {
    private static final String NOT_AUTHORIZED = "Вы не были авторизованы ни в одной сети. Пожалуйста, авторизуйтесь "
            + "хотя бы в одной социальной сети и выберите команду /%s.";
    private static final String AUTHORIZED = "Вы авторизованы в следующих социальных сетях: %s.";
    private static final String GET_TELEGRAM_CHANNEL_LINK = """
            Теперь необходимо выполнить следующие действия:
            1. Добавить бота в администраторы Вашего телеграм-канала.
            2. Скопировать ссылку на телеграм-канал. Пример такой ссылки: https://t.me/exploitex
            3. Прислать ссылку в данный диалог.""";
    private final Map<Long, List<AuthData>> socialMedia;
    private static final int rowsCount = 0;
    private static final List<String> commandsForKeyboard = List.of(
    );

    public SyncCommand(String commandIdentifier, String description, Map<Long, List<AuthData>> socialMedia) {
        super(commandIdentifier, description);
        this.socialMedia = socialMedia;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (socialMedia.get(chat.getId()) == null || socialMedia.get(chat.getId()).isEmpty()) {
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(),
                    String.format(NOT_AUTHORIZED, State.Sync.getIdentifier()), rowsCount, commandsForKeyboard,
                    GO_BACK_BUTTON_TEXT);
        } else {
            List<AuthData> currentAuthData = socialMedia.get(chat.getId());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentAuthData.size(); i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(currentAuthData.get(i).getSocialMedia().getName());
            }
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(),
                    String.format(AUTHORIZED, sb), rowsCount, commandsForKeyboard, GO_BACK_BUTTON_TEXT);
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(),
                    GET_TELEGRAM_CHANNEL_LINK, rowsCount, commandsForKeyboard, GO_BACK_BUTTON_TEXT);
        }
    }
}

package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.authorization.AuthData;
import polis.keyboards.Keyboard;
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
    private final Logger logger = LoggerFactory.getLogger(SyncCommand.class);

    public SyncCommand(String commandIdentifier, String description, Map<Long, List<AuthData>> socialMedia) {
        super(commandIdentifier, description);
        this.socialMedia = socialMedia;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            if (socialMedia.get(chat.getId()) == null || socialMedia.get(chat.getId()).isEmpty()) {
                SendMessage sendMessage = Keyboard.createSendMessage(chat.getId(), String.format(NOT_AUTHORIZED,
                        State.Sync.getIdentifier()), GO_BACK_BUTTON_TEXT);
                absSender.execute(sendMessage);
            } else {
                List<AuthData> currentAuthData = socialMedia.get(chat.getId());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < currentAuthData.size(); i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    sb.append(currentAuthData.get(i).getSocialMedia().getName());
                }
                SendMessage sendMessage1 = Keyboard.createSendMessage(chat.getId(), String.format(AUTHORIZED, sb),
                        GO_BACK_BUTTON_TEXT);
                SendMessage sendMessage2 = Keyboard.createSendMessage(chat.getId(), GET_TELEGRAM_CHANNEL_LINK,
                        GO_BACK_BUTTON_TEXT);
                absSender.execute(sendMessage1);
                absSender.execute(sendMessage2);
            }
        } catch (TelegramApiException e) {
            logger.error(String.format("Cannot send message: %s", e.getMessage()));
        }
    }
}

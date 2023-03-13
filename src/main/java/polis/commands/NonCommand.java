package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.authorization.AuthData;
import polis.authorization.OkAuthorization;
import polis.util.SocialMedia;
import polis.util.State;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class NonCommand {
    private static final String START_STATE_ANSWER = "Не могу распознать команду. Попробуйте еще раз.";
    private static final String OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER =
            "Введенный код авторизации не верный. Пожалуйста, попробуйте еще раз.";
    private static final String OK_AUTH_STATE_ANSWER = "Вы были успешно авторизованы в социальной сети Одноклассники." +
            " Выберите /%s, чтобы продолжить настройку постинга.";
    private static final String OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER = "Ошибка на сервере. Попробуйте еще раз.";
    private static final String BOT_WRONG_STATE_ANSWER = "Неверное состояние бота. Попробуйте еще раз.";
    private final Logger logger = LoggerFactory.getLogger(NonCommand.class);

    public String nonCommandExecute(String text, Long chatId, State state, Properties properties, Map<Long,
            List<AuthData>> socialMedia) {
        switch (state) {
            case Start -> {
                return START_STATE_ANSWER;
            }
            case OkAuth -> {
                OkAuthorization.TokenPair pair;
                try {
                    pair = OkAuthorization.getToken(text, properties.getProperty("okapp.id"),
                            properties.getProperty("okapp.secret_key"), properties.getProperty("okapp.redirect_uri"));
                    if (pair.accessToken() == null) {
                        return OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER;
                    }
                    if (socialMedia.get(chatId) == null || socialMedia.get(chatId).isEmpty()) {
                        List<AuthData> newSocialMedia = new ArrayList<>(1);
                        newSocialMedia.add(new AuthData(SocialMedia.OK, pair.accessToken()));
                        socialMedia.put(chatId, newSocialMedia);
                    } else {
                        List<AuthData> currentSocialMedia = socialMedia.get(chatId);
                        currentSocialMedia.add(new AuthData(SocialMedia.OK, pair.accessToken()));
                        socialMedia.put(chatId, currentSocialMedia);
                    }
                    return String.format(OK_AUTH_STATE_ANSWER, State.Sync.getIdentifier());
                } catch (URISyntaxException | IOException | InterruptedException e) {
                    logger.error(String.format("Unknown error: %s", e.getMessage()));
                    return OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER;
                }
            }
            default -> {
                return BOT_WRONG_STATE_ANSWER;
            }
        }
    }
}

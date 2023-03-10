package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.authorization.OkAuthorization;
import polis.state.State;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class NonCommand {
    private static final String START_STATE_ANSWER = "Не могу распознать команду. Попробуйте еще раз.";
    private static final String OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER =
            "Введенный код авторизации не верный. Пожалуйста, попробуйте еще раз.";
    private static final String OK_AUTH_STATE_ANSWER = """
                                            Получен access_token: %s
                                            Получен refresh_token: %s""";
    private static final String OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER = "Ошибка на сервере. Попробуйте еще раз.";
    private static final String BOT_WRONG_STATE_ANSWER = "Неверное состояние бота. Попробуйте еще раз.";
    private final Logger logger = LoggerFactory.getLogger(NonCommand.class);

    public String nonCommandExecute(String text, State state, Properties properties) {
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
                    return String.format(OK_AUTH_STATE_ANSWER, pair.accessToken(), pair.refreshToken());
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

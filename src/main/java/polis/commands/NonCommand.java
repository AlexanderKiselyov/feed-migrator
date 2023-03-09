package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.authorization.OkAuthorization;
import polis.state.State;

import java.io.IOException;
import java.net.URISyntaxException;

public class NonCommand {
    private final Logger logger = LoggerFactory.getLogger(NonCommand.class);

    public String nonCommandExecute(Long chatId, String userName, String text, State state) {
        switch (state) {
            case Start -> {
                return "Не могу распознать команду. Попробуйте еще раз.";
            }
            case OkAuth -> {
                OkAuthorization.TokenPair pair;
                try {
                    pair = OkAuthorization.getToken(text);
                    if (pair.accessToken() == null) {
                        return "Введенный код авторизации не верный. Пожалуйста, попробуйте еще раз.";
                    }
                    return String.format("""
                                            Получен access_token: %s
                                            Получен refresh_token: %s""", pair.accessToken(), pair.refreshToken());
                } catch (URISyntaxException | IOException | InterruptedException e) {
                    logger.error(String.format("Unknown error: %s", e.getMessage()));
                    return "Ошибка на сервере. Попробуйте еще раз.";
                }
            }
            default -> {
                return "Ошибка на сервере. Попробуйте еще раз.";
            }
        }
    }
}

package polis.telegram;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.bot.BotProperties;
import polis.commands.NonCommand;
import polis.util.State;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

@Component
public class TelegramDataCheck {
    private static final String GET_CHAT_MEMBER = "https://api.telegram.org/bot%s/getChatMember";
    public static final String WRONG_LINK_OR_BOT_NOT_ADMIN = """
            Введенная ссылка не является верной или бот не был добавлен в администраторы канала.
            Пожалуйста, проверьте, что бот был добавлен в администраторы канала и введите ссылку еще раз.""";
    public static final String BOT_NOT_ADMIN = """
            Бот не был добавлен в администраторы канала.
            Пожалуйста, добавьте бота в администраторы канала и введите ссылку еще раз.""";
    public static final String RIGHT_LINK = String.format("""
            Телеграм-канал успешно добавлен.
            Посмотреть информацию по телеграм-каналу можно по команде /%s""",
            State.TgChannelDescription.getIdentifier());
    private static final String GET_CHAT = "https://api.telegram.org/bot%s/getChat";
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramDataCheck.class);

    @Autowired
    private HttpClient client;

    public TelegramDataCheck() {

    }

    public NonCommand.AnswerPair checkTelegramChannelLink(String checkChannelLink) {
        try {
            URI uri = new URIBuilder(String.format(GET_CHAT_MEMBER, BotProperties.TOKEN))
                    .addParameter("chat_id", String.format("@%s", checkChannelLink))
                    .addParameter("user_id", String.format("%s", BotProperties.ID))
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .GET()
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String errorMessage;
            if (response.statusCode() != 200) {
                errorMessage = """
                                Not 200 code of the response.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
                logError(request, response, errorMessage);
                return new NonCommand.AnswerPair(WRONG_LINK_OR_BOT_NOT_ADMIN, true);
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("result")) {
                errorMessage = """
                                Response doesn't contain 'result' field.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
                logError(request, response, errorMessage);
                return new NonCommand.AnswerPair(WRONG_LINK_OR_BOT_NOT_ADMIN, true);
            }

            JSONObject result = object.getJSONObject("result");

            if (!result.has("status")) {
                errorMessage = """
                                Response doesn't contain 'status' field.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
                logError(request, response, errorMessage);
                return new NonCommand.AnswerPair(WRONG_LINK_OR_BOT_NOT_ADMIN, true);
            }

            String status = result.getString("status");
            if (Objects.equals(status, "administrator")) {
                return new NonCommand.AnswerPair(RIGHT_LINK, false);
            } else {
                return new NonCommand.AnswerPair(BOT_NOT_ADMIN, true);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Cannot create request: %s", e.getMessage()));
            return new NonCommand.AnswerPair(WRONG_LINK_OR_BOT_NOT_ADMIN, true);
        }
    }

    public Object getChatParameter(String chatUsername, String parameter) {
        try {
            URI uri = new URIBuilder(String.format(GET_CHAT, BotProperties.TOKEN))
                    .addParameter("chat_id", String.format("@%s", chatUsername))
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .GET()
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String errorMessage;
            if (response.statusCode() != 200) {
                errorMessage = """
                                Not 200 code of the response.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
                logError(request, response, errorMessage);
                return null;
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("result")) {
                errorMessage = """
                                Response doesn't contain 'result' field.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
                logError(request, response, errorMessage);
                return null;

            }

            JSONObject result = object.getJSONObject("result");

            if (!result.has(parameter)) {
                errorMessage = """
                                Response doesn't contain 'title' field.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
                logError(request, response, errorMessage);
                return null;
            }

            return result.get(parameter);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Cannot create request: %s", e.getMessage()));
            return null;
        }
    }

    private void logError(HttpRequest request, HttpResponse<String> response, String errorMessage) {
        LOGGER.error(String.format(errorMessage, request.uri(), request.headers().toString(), response.body()));
    }
}

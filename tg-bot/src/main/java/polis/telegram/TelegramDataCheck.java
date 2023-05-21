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
            Телеграмм-канал успешно добавлен.
            Посмотреть информацию по телеграм-каналу можно по команде /%s""",
            State.TgChannelDescription.getIdentifier());
    private static final String GET_CHAT = "https://api.telegram.org/bot%s/getChat";
    private static final String RESULT_FIELD = "result";
    private static final String STATUS_FIELD = "status";
    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String USERNAME_FIELD = "username";
    private static final String NOT_SUCCESS_CODE = """
                                Not 200 code of the response.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
    private static final String FIELD_ABSENCE = """
                                Response doesn't contain '%s' field.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
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

            if (response.statusCode() != 200) {
                logNotSuccessCode(request, response);
                return new NonCommand.AnswerPair(WRONG_LINK_OR_BOT_NOT_ADMIN, true);
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has(RESULT_FIELD)) {
                logFieldAbsence(request, response, RESULT_FIELD);
                return new NonCommand.AnswerPair(WRONG_LINK_OR_BOT_NOT_ADMIN, true);
            }

            JSONObject result = object.getJSONObject(RESULT_FIELD);

            if (!result.has(STATUS_FIELD)) {
                logFieldAbsence(request, response, STATUS_FIELD);
                return new NonCommand.AnswerPair(WRONG_LINK_OR_BOT_NOT_ADMIN, true);
            }

            String status = result.getString(STATUS_FIELD);
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

    public TelegramChannel getChannelParameters(String channelUsername) {
        try {
            URI uri = new URIBuilder(String.format(GET_CHAT, BotProperties.TOKEN))
                    .addParameter("chat_id", String.format("@%s", channelUsername))
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .GET()
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logNotSuccessCode(request, response);
                return null;
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has(RESULT_FIELD)) {
                logFieldAbsence(request, response, RESULT_FIELD);
                return null;
            }

            JSONObject channel = object.getJSONObject(RESULT_FIELD);

            if (!channel.has(ID_FIELD)) {
                logFieldAbsence(request, response, ID_FIELD);
                return null;
            }

            Long id = channel.getLong(ID_FIELD);

            if (!channel.has(TITLE_FIELD)) {
                logFieldAbsence(request, response, TITLE_FIELD);
                return null;
            }

            String title = channel.getString(TITLE_FIELD);

            if (!channel.has(USERNAME_FIELD)) {
                logFieldAbsence(request, response, USERNAME_FIELD);
                return null;
            }

            String username = channel.getString(USERNAME_FIELD);

            return new TelegramChannel(id, title, username);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Cannot create request: %s", e.getMessage()));
            return null;
        }
    }

    private void logNotSuccessCode(HttpRequest request, HttpResponse<String> response) {
        LOGGER.error(String.format(NOT_SUCCESS_CODE, request.uri(), request.headers().toString(), response.body()));
    }

    private void logFieldAbsence(HttpRequest request, HttpResponse<String> response, String field) {
        LOGGER.error(String.format(FIELD_ABSENCE, field, request.uri(), request.headers().toString(), response.body()));
    }

    public record TelegramChannel(Long id, String title, String username) {

    }
}

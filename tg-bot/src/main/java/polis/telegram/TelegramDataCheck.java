package polis.telegram;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.bot.BotProperties;
import polis.util.State;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TelegramDataCheck {
    private static final String GET_CHAT_MEMBER = "https://api.telegram.org/bot%s/getChatMember";
    private static final String WRONG_LINK_OR_BOT_NOT_ADMIN = """
            Введенная ссылка не является верной или бот не был добавлен в администраторы канала.
            Пожалуйста, проверьте, что бот был добавлен в администраторы канала и введите ссылку еще раз.""";
    private static final String BOT_NOT_ADMIN = """
            Бот не был добавлен в администраторы канала.
            Пожалуйста, добавьте бота в администраторы канала и введите ссылку еще раз.""";
    private static final String RIGHT_LINK = String.format("""
            Телеграм-канал успешно добавлен.
            Посмотреть информацию по телеграм-каналу можно по команде /%s""",
            State.TgChannelDescription.getIdentifier());
    private static final String GET_CHAT_TITLE = "https://api.telegram.org/bot%s/getChat";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Logger logger = LoggerFactory.getLogger(TelegramDataCheck.class);

    public TelegramDataCheck() {

    }

    public String checkTelegramChannelLink(String checkChatId, Long chatId, Map<Long, List<String>> tgChannels,
                                           Map<Long, String> currentTgChannel) {
        try {
            URI uri = new URIBuilder(String.format(GET_CHAT_MEMBER, BotProperties.TOKEN))
                    .addParameter("chat_id", String.format("@%s", checkChatId))
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
                return WRONG_LINK_OR_BOT_NOT_ADMIN;
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("result")) {
                errorMessage = """
                                Response doesn't contain 'result' field.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
                logError(request, response, errorMessage);
                return WRONG_LINK_OR_BOT_NOT_ADMIN;
            }

            JSONObject result = object.getJSONObject("result");

            if (!result.has("status")) {
                errorMessage = """
                                Response doesn't contain 'status' field.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
                logError(request, response, errorMessage);
                return WRONG_LINK_OR_BOT_NOT_ADMIN;
            }

            String status = result.getString("status");
            if (Objects.equals(status, "administrator")) {
                if (tgChannels.containsKey(chatId)) {
                    List<String> currentTelegramChannels = tgChannels.get(chatId);
                    currentTelegramChannels.add(checkChatId);
                    tgChannels.put(chatId, currentTelegramChannels);
                } else {
                    List<String> newTelegramChannel = new ArrayList<>(1);
                    newTelegramChannel.add(checkChatId);
                    tgChannels.put(chatId, newTelegramChannel);
                }
                currentTgChannel.put(chatId, checkChatId);
                return RIGHT_LINK;
            } else {
                return BOT_NOT_ADMIN;
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Cannot create request: %s", e.getMessage()));
            return WRONG_LINK_OR_BOT_NOT_ADMIN;
        }
    }

    public String getChatTitle(String chatId) {
        try {
            URI uri = new URIBuilder(String.format(GET_CHAT_TITLE, BotProperties.TOKEN))
                    .addParameter("chat_id", String.format("@%s", chatId))
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

            if (!result.has("title")) {
                errorMessage = """
                                Response doesn't contain 'title' field.
                                Request URI: %s
                                Request headers: %s
                                Response: %s""";
                logError(request, response, errorMessage);
                return null;
            }

            return result.getString("title");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Cannot create request: %s", e.getMessage()));
            return null;
        }
    }

    private void logError(HttpRequest request, HttpResponse<String> response, String errorMessage) {
        logger.error(String.format(errorMessage, request.uri(), request.headers().toString(), response.body()));
    }
}

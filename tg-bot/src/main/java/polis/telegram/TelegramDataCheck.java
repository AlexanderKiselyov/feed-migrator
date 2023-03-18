package polis.telegram;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Properties;

public class TelegramDataCheck {
    private static final String GET_CHAT_MEMBER = "https://api.telegram.org/bot%s/getChatMember";
    private static final String WRONG_LINK_OR_BOT_NOT_ADMIN = """
            Введенная ссылка не является верной или бот не был добавлен в администраторы канала.
            Пожалуйста, проверьте, что бот был добавлен в администраторы канала и введите ссылку еще раз.""";
    private static final String BOT_NOT_ADMIN = """
            Бот не был добавлен в администраторы канала.
            Пожалуйста, добавьте бота в администраторы канала и введите ссылку еще раз.""";
    private static final String RIGHT_LINK = "Ссылка верная!";
    private final Properties properties;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Logger logger = LoggerFactory.getLogger(TelegramDataCheck.class);

    public TelegramDataCheck(Properties properties) {
        this.properties = properties;
    }

    public String checkTelegramChannelLink(String checkChatId) {
        try {
            URI uri = new URIBuilder(String.format(GET_CHAT_MEMBER, properties.getProperty("bot.token")))
                    .addParameter("chat_id", String.format("@%s", checkChatId))
                    .addParameter("user_id", String.format("%s", properties.getProperty("bot.id")))
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return WRONG_LINK_OR_BOT_NOT_ADMIN;
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("result")) {
                return WRONG_LINK_OR_BOT_NOT_ADMIN;
            }

            JSONObject result = object.getJSONObject("result");

            if (!result.has("status")) {
                return WRONG_LINK_OR_BOT_NOT_ADMIN;
            }

            String status = result.getString("status");
            if (Objects.equals(status, "administrator")) {
                return RIGHT_LINK;
            } else {
                return BOT_NOT_ADMIN;
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Cannot create request: %s", e.getMessage()));
            return WRONG_LINK_OR_BOT_NOT_ADMIN;
        }
    }
}

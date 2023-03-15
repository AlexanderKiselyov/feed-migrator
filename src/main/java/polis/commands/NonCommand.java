package polis.commands;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.authorization.AuthData;
import polis.authorization.OkAuthorization;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.State;
import polis.util.Substate;

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
import java.util.Properties;

public class NonCommand {
    private static final String START_STATE_ANSWER = "Не могу распознать команду. Попробуйте еще раз.";
    private static final String OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER =
            "Введенный код авторизации не верный. Пожалуйста, попробуйте еще раз.";
    private static final String OK_AUTH_STATE_ANSWER = """
            Вы были успешно авторизованы в социальной сети Одноклассники.
            Теперь введите ссылку на группу в Одноклассниках, куда хотели бы публиковать контент.
            Примеры такой ссылки:
            https://ok.ru/ok
            https://ok.ru/group/44602239156479""";
    private static final String OK_GROUP_ADDED = """
            Группа была успешно добавлена.
            Выберите /%s, чтобы продолжить настройку постинга.""";
    private static final String OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER = "Ошибка на сервере. Попробуйте еще раз.";
    private static final String BOT_WRONG_STATE_ANSWER = "Неверное состояние бота. Попробуйте еще раз.";
    private static final String GET_CHAT_MEMBER = "https://api.telegram.org/bot%s/getChatMember";
    private static final String OK_METHOD_DO = "https://api.ok.ru/fb.do";
    private static final String WRONG_LINK_OR_USER_HAS_NO_RIGHTS = """
            Введенная ссылка не является верной или пользователь не является администратором или модератором группы.
            Пожалуйста, проверьте, что пользователь - администратор или модератор группы и введите ссылку еще раз.""";
    private static final String USER_HAS_NO_RIGHTS = """
            Пользователь не является администратором или модератором группы.
            Пожалуйста, проверьте, что пользователь - администратор или модератор группы и введите ссылку еще раз.""";
    private static final String WRONG_LINK_OR_BOT_NOT_ADMIN = """
            Введенная ссылка не является верной или бот не был добавлен в администраторы канала.
            Пожалуйста, проверьте, что бот был добавлен в администраторы канала и введите ссылку еще раз.""";
    private static final String WRONG_LINK_TELEGRAM = """
             Ссылка неверная.
             Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";
    private static final String BOT_NOT_ADMIN = """
            Бот не был добавлен в администраторы канала.
            Пожалуйста, добавьте бота в администраторы канала и введите ссылку еще раз.""";
    private static final String RIGHT_LINK = "Ссылка верная!";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Map<Long, IState> states;
    private final Map<Long, List<AuthData>> socialMedia;
    private final Properties properties;
    private final Logger logger = LoggerFactory.getLogger(NonCommand.class);

    public NonCommand(Map<Long, IState> states, Map<Long, List<AuthData>> socialMedia, Properties properties) {
        this.states = states;
        this.socialMedia = socialMedia;
        this.properties = properties;
    }

    public String nonCommandExecute(String text, Long chatId, IState state) {
        if (state == null) {
            return BOT_WRONG_STATE_ANSWER;
        }
        if (state.equals(State.Start)) {
            return START_STATE_ANSWER;
        } else if (state.equals(Substate.OkAuth_AuthCode)) {
            return getOKAuthCode(text, chatId);
        } else if (state.equals(Substate.OkAuth_GroupSync)) {
            String accessToken = "";
            for (AuthData authData : socialMedia.get(chatId)) {
                if (authData.getSocialMedia() == SocialMedia.OK) {
                    accessToken = authData.getAccessToken();
                    break;
                }
            }

            Long groupId = getOKGroupId(text, accessToken);

            return checkOKGroupAdminRights(accessToken, groupId);
        } else if (state.equals(Substate.Sync_TelegramChannel)) {
            String[] split = text.split("/");
            if (split.length < 2) {
                return WRONG_LINK_TELEGRAM;
            }
            String checkChatId = text.split("/")[split.length - 1];

            return checkTelegramChannelLink(checkChatId);
        }
        return BOT_WRONG_STATE_ANSWER;
    }

    private String getOKAuthCode(String text, Long chatId) {
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

            states.put(chatId, Substate.nextSubstate(Substate.OkAuth_AuthCode));

            return OK_AUTH_STATE_ANSWER;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Unknown error: %s", e.getMessage()));
            return OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER;
        }
    }

    private String checkTelegramChannelLink(String checkChatId) {
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

    private String checkOKGroupAdminRights(String accessToken, Long groupId) {
        String uid = getOKUserId(accessToken);

        try {
            URI uri = new URIBuilder(OK_METHOD_DO)
                    .addParameter("method", "group.getUserGroupsByIds")
                    .addParameter("access_token", accessToken)
                    .addParameter("application_key", properties.getProperty("okapp.app_key"))
                    .addParameter("group_id", String.valueOf(groupId))
                    .addParameter("uids", uid)
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return WRONG_LINK_OR_USER_HAS_NO_RIGHTS;
            }

            JSONArray array = new JSONArray(response.body());

            if (array.length() == 0) {
                return WRONG_LINK_OR_USER_HAS_NO_RIGHTS;
            }

            JSONObject object = array.getJSONObject(0);

            if (!object.has("status")) {
                return WRONG_LINK_OR_USER_HAS_NO_RIGHTS;
            }

            String status = object.getString("status");
            if (Objects.equals(status, "ADMIN") || Objects.equals(status, "MODERATOR")) {
                return String.format(OK_GROUP_ADDED, State.Sync.getIdentifier());
            } else {
                return USER_HAS_NO_RIGHTS;
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Cannot create request: %s", e.getMessage()));
            return WRONG_LINK_OR_USER_HAS_NO_RIGHTS;
        }
    }

    private Long getOKGroupId(String groupLink, String accessToken) {
        try {
            URI uri = new URIBuilder(OK_METHOD_DO)
                    .addParameter("method", "url.getInfo")
                    .addParameter("access_token", accessToken)
                    .addParameter("application_key", properties.getProperty("okapp.app_key"))
                    .addParameter("url", groupLink)
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return -1L;
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("objectId")) {
                return -1L;
            }

            return object.getLong("objectId");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Cannot create request: %s", e.getMessage()));
            return -1L;
        }
    }

    private String getOKUserId(String accessToken) {
        try {
            URI uri = new URIBuilder(OK_METHOD_DO)
                    .addParameter("method", "users.getCurrentUser")
                    .addParameter("access_token", accessToken)
                    .addParameter("application_key", properties.getProperty("okapp.app_key"))
                    .addParameter("fields", "UID")
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "";
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("uid")) {
                return "";
            }

            return object.getString("uid");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Cannot create request: %s", e.getMessage()));
            return "";
        }
    }
}

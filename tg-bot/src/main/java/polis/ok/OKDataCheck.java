package polis.ok;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.ok.api.OkAppProperties;
import polis.ok.api.OkAuthorizator;
import polis.util.AuthData;
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

public class OKDataCheck {
    private static final String OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER =
            "Введенный код авторизации не верный. Пожалуйста, попробуйте еще раз.";
    private static final String OK_AUTH_STATE_ANSWER = """
            Вы были успешно авторизованы в социальной сети Одноклассники.
            Теперь введите ссылку на группу в Одноклассниках, куда хотели бы публиковать контент.
            Примеры такой ссылки:
            https://ok.ru/ok
            https://ok.ru/group/44602239156479""";
    private static final String OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER = "Ошибка на сервере. Попробуйте еще раз.";
    private static final String OK_GROUP_ADDED = """
            Группа была успешно добавлена.
            Выберите /%s, чтобы продолжить настройку постинга.""";
    private static final String OK_METHOD_DO = "https://api.ok.ru/fb.do";
    private static final String WRONG_LINK_OR_USER_HAS_NO_RIGHTS = """
            Введенная ссылка не является верной или пользователь не является администратором или модератором группы.
            Пожалуйста, проверьте, что пользователь - администратор или модератор группы и введите ссылку еще раз.""";
    private static final String USER_HAS_NO_RIGHTS = """
            Пользователь не является администратором или модератором группы.
            Пожалуйста, проверьте, что пользователь - администратор или модератор группы и введите ссылку еще раз.""";
    private final Map<Long, List<AuthData>> socialMedia;
    private final Map<Long, IState> states;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Logger logger = LoggerFactory.getLogger(OKDataCheck.class);
    private final OkAuthorizator okAuthorizator = new OkAuthorizator();

    public OKDataCheck(Map<Long, List<AuthData>> socialMedia, Map<Long, IState> states) {
        this.socialMedia = socialMedia;
        this.states = states;
    }

    public String getOKAuthCode(String text, Long chatId) {
        OkAuthorizator.TokenPair pair;
        try {
            pair = okAuthorizator.getToken(text);
            if (pair.accessToken() == null) {
                return OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER;
            }
            if (socialMedia.containsKey(chatId)) {
                List<AuthData> currentSocialMedia = socialMedia.get(chatId);
                currentSocialMedia.add(new AuthData(SocialMedia.OK, pair.accessToken()));
                socialMedia.put(chatId, currentSocialMedia);
            } else {
                List<AuthData> newSocialMedia = new ArrayList<>(1);
                newSocialMedia.add(new AuthData(SocialMedia.OK, pair.accessToken()));
                socialMedia.put(chatId, newSocialMedia);
            }

            states.put(chatId, Substate.nextSubstate(Substate.OkAuth_AuthCode));

            return OK_AUTH_STATE_ANSWER;
        } catch (Exception e) {
            logger.error(String.format("Unknown error: %s", e.getMessage()));
            return OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER;
        }
    }

    public String checkOKGroupAdminRights(String accessToken, Long groupId) {
        String uid = getOKUserId(accessToken);

        try {
            URI uri = new URIBuilder(OK_METHOD_DO)
                    .addParameter("method", "group.getUserGroupsByIds")
                    .addParameter("access_token", accessToken)
                    .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
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

    public Long getOKGroupId(String groupLink, String accessToken) {
        try {
            URI uri = new URIBuilder(OK_METHOD_DO)
                    .addParameter("method", "url.getInfo")
                    .addParameter("access_token", accessToken)
                    .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
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

    public String getOKUserId(String accessToken) {
        try {
            URI uri = new URIBuilder(OK_METHOD_DO)
                    .addParameter("method", "users.getCurrentUser")
                    .addParameter("access_token", accessToken)
                    .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
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

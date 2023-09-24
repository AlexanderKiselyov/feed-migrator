package polis.datacheck;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.commands.NonCommand;
import polis.commands.context.ContextStorage;
import polis.data.repositories.AccountsRepository;
import polis.ok.api.OkAppProperties;
import polis.ok.api.OkAuthorizator;
import polis.util.SocialMedia;
import polis.util.State;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static polis.commands.Command.USER_ID_NOT_FOUND;

@Component
public class OkDataCheck {
    public static final String OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER =
            "Введенный код авторизации неверный. Пожалуйста, попробуйте еще раз.";
    public static final String OK_GROUP_ADDED = """
            Группа была успешно добавлена.
            Синхронизируйте группу с Телеграмм-каналом по команде /%s.""";
    private static final String OK_METHOD_DO = "https://api.ok.ru/fb.do";
    public static final String SAME_OK_ACCOUNT = "Данный аккаунт в социальной сети Одноклассники уже был добавлен.";
    public static final String WRONG_LINK_OR_USER_HAS_NO_RIGHTS = """
            Введенная ссылка не является верной или пользователь не является администратором или модератором группы.
            Пожалуйста, проверьте, что пользователь - администратор или модератор группы и введите ссылку еще раз.""";
    public static final String USER_HAS_NO_RIGHTS = """
            Пользователь не является администратором или модератором группы.
            Пожалуйста, проверьте, что пользователь - администратор или модератор группы и введите ссылку еще раз.""";
    private static final String CODE_EXPIRED_MSG = """
            Время действия кода авторизации истекло.
            Пройдите по ссылке для авторизации еще раз и получите новый код.""";

    private static final String OK_SOCIAL_NAME = SocialMedia.OK.getName();

    //TODO FIX AUTOWIRING REPOSITORIES IN CLASS CALLED DATACHECK
    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private ContextStorage contextStorage;

    @Autowired
    private OkAuthorizator okAuthorizator;

    @Autowired
    private HttpClient client;

    private static final Logger LOGGER = LoggerFactory.getLogger(OkDataCheck.class);

    public NonCommand.AnswerPair checkOKGroupAdminRights(String accessToken, Long groupId) {
        Long uid = getOKUserId(accessToken);

        if (Objects.equals(uid, null)) {
            return new NonCommand.AnswerPair(USER_ID_NOT_FOUND, true);
        }

        try {
            URI uri = new URIBuilder(OK_METHOD_DO)
                    .addParameter("method", "group.getUserGroupsByIds")
                    .addParameter("access_token", accessToken)
                    .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
                    .addParameter("group_id", String.valueOf(groupId))
                    .addParameter("uids", String.valueOf(uid))
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return new NonCommand.AnswerPair(WRONG_LINK_OR_USER_HAS_NO_RIGHTS, true);
            }

            JSONArray array = new JSONArray(response.body());

            if (array.length() == 0) {
                return new NonCommand.AnswerPair(WRONG_LINK_OR_USER_HAS_NO_RIGHTS, true);
            }

            JSONObject object = array.getJSONObject(0);

            if (!object.has("status")) {
                return new NonCommand.AnswerPair(WRONG_LINK_OR_USER_HAS_NO_RIGHTS, true);
            }

            String status = object.getString("status");
            if (Objects.equals(status, "ADMIN") || Objects.equals(status, "MODERATOR")) {
                return new NonCommand.AnswerPair(String.format(OK_GROUP_ADDED, State.SyncOkTg.getIdentifier()), false);
            } else {
                return new NonCommand.AnswerPair(USER_HAS_NO_RIGHTS, true);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Cannot create request: %s", e.getMessage()));
            return new NonCommand.AnswerPair(WRONG_LINK_OR_USER_HAS_NO_RIGHTS, true);
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
                return null;
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("objectId")) {
                return null;
            }

            return object.getLong("objectId");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Cannot create request: %s", e.getMessage()));
            return null;
        }
    }

    public String getOKGroupName(Long groupId, String accessToken) {
        try {
            URI uri = new URIBuilder(OK_METHOD_DO)
                    .addParameter("method", "group.getInfo")
                    .addParameter("access_token", accessToken)
                    .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
                    .addParameter("uids", String.valueOf(groupId))
                    .addParameter("fields", "NAME")
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            JSONArray array = new JSONArray(response.body());

            if (array.length() != 1) {
                return null;
            }

            JSONObject object = array.getJSONObject(0);

            if (!object.has("name")) {
                return null;
            }

            return object.getString("name");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Cannot create request: %s", e.getMessage()));
            return null;
        }
    }

    public Long getOKUserId(String accessToken) {
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
                return null;
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("uid")) {
                return null;
            }

            return object.getLong("uid");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Cannot create request: %s", e.getMessage()));
            return null;
        }
    }

    public String getOKUsername(String accessToken) {
        try {
            URI uri = new URIBuilder(OK_METHOD_DO)
                    .addParameter("method", "users.getCurrentUser")
                    .addParameter("access_token", accessToken)
                    .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
                    .addParameter("fields", "NAME")
                    .build();

            HttpRequest request = HttpRequest
                    .newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                    .uri(uri)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("name")) {
                return null;
            }

            return object.getString("name");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Cannot create request: %s", e.getMessage()));
            return null;
        }
    }
}

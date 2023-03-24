package polis.ok;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.commands.NonCommand;
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
            Вы можете посмотреть информацию по аккаунту, если введете команду /%s.""";
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
    private final Map<Long, AuthData> currentSocialMediaAccount;
    private final Map<Long, List<AuthData>> socialMediaAccounts;
    private final Map<Long, IState> states;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Logger logger = LoggerFactory.getLogger(OKDataCheck.class);
    private final OkAuthorizator okAuthorizator = new OkAuthorizator();

    public OKDataCheck(Map<Long, AuthData> currentSocialMediaAccount, Map<Long, IState> states, Map<Long,
            List<AuthData>> socialMediaAccounts) {
        this.currentSocialMediaAccount = currentSocialMediaAccount;
        this.states = states;
        this.socialMediaAccounts = socialMediaAccounts;
    }

    public NonCommand.AnswerPair getOKAuthCode(String text, Long chatId) {
        OkAuthorizator.TokenPair pair;
        try {
            pair = okAuthorizator.getToken(text);
            if (pair.accessToken() == null) {
                return new NonCommand.AnswerPair(OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER, true);
            }
            AuthData newAccount = new AuthData(SocialMedia.OK, pair.accessToken(), getOKUsername(pair.accessToken()));
            currentSocialMediaAccount.put(chatId, newAccount);
            socialMediaAccounts.computeIfAbsent(chatId, k -> new ArrayList<>());
            socialMediaAccounts.get(chatId).add(newAccount);

            states.put(chatId, Substate.nextSubstate(State.OkAccountDescription));

            return new NonCommand.AnswerPair(
                    String.format(OK_AUTH_STATE_ANSWER, State.OkAccountDescription.getIdentifier()),
                    false);
        } catch (Exception e) {
            logger.error(String.format("Unknown error: %s", e.getMessage()));
            return new NonCommand.AnswerPair(OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER, true);
        }
    }

    public NonCommand.AnswerPair checkOKGroupAdminRights(String accessToken, Long groupId) {
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
                return new NonCommand.AnswerPair(String.format(OK_GROUP_ADDED, State.SyncOkTg.getIdentifier()),
                        false);
            } else {
                return new NonCommand.AnswerPair(USER_HAS_NO_RIGHTS, true);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Cannot create request: %s", e.getMessage()));
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
                return "";
            }

            JSONArray array = new JSONArray(response.body());

            if (array.length() != 1) {
                return "";
            }

            JSONObject object = array.getJSONObject(0);

            if (!object.has("name")) {
                return "";
            }

            return object.getString("name");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Cannot create request: %s", e.getMessage()));
            return "";
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
                return "";
            }

            JSONObject object = new JSONObject(response.body());

            if (!object.has("name")) {
                return "";
            }

            return object.getString("name");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(String.format("Cannot create request: %s", e.getMessage()));
            return "";
        }
    }
}

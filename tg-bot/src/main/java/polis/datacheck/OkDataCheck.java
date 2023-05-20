package polis.datacheck;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.commands.NonCommand;
import polis.data.domain.Account;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentState;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentStateRepository;
import polis.ok.api.OkAppProperties;
import polis.ok.api.OkAuthorizator;
import polis.ok.api.exceptions.CodeExpiredException;
import polis.ok.api.exceptions.OkApiException;
import polis.util.SocialMedia;
import polis.util.State;
import polis.util.Substate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static polis.commands.Command.USERNAME_NOT_FOUND;
import static polis.commands.Command.USER_ID_NOT_FOUND;

@Component
public class OkDataCheck {
    public static final String OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER =
            "Введенный код авторизации неверный. Пожалуйста, попробуйте еще раз.";
    public static final String OK_AUTH_STATE_ANSWER = """
            Вы были успешно авторизованы в социальной сети Одноклассники.
            Вы можете посмотреть информацию по аккаунту, если введете команду /%s.""";
    public static final String OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER = """
            Невозможно выполнить авторизацию в социальной сети Одноклассники.
            Пожалуйста, проверьте данные авторизации и попробуйте еще раз.""";
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

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private CurrentStateRepository currentStateRepository;

    @Autowired
    private OkAuthorizator okAuthorizator;

    @Autowired
    private HttpClient client;

    private static final Logger LOGGER = LoggerFactory.getLogger(OkDataCheck.class);

    public NonCommand.AnswerPair getOKAuthCode(String code, Long chatId) {
        OkAuthorizator.TokenPair pair;
        try {
            pair = okAuthorizator.getToken(code);
        } catch (IOException | URISyntaxException e) {
            return new NonCommand.AnswerPair(OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER, true);
        } catch (CodeExpiredException e) {
            return new NonCommand.AnswerPair(CODE_EXPIRED_MSG, true);
        } catch (OkApiException e) {
            return new NonCommand.AnswerPair(e.getMessage(), true);
        }

        if (pair.accessToken() == null) {
            return new NonCommand.AnswerPair(OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER, true);
        }

        Long userId = getOKUserId(pair.accessToken());

        if (userId == null) {
            return new NonCommand.AnswerPair(USER_ID_NOT_FOUND, true);
        }

        if (accountsRepository.getUserAccount(chatId, userId, OK_SOCIAL_NAME) != null) {
            return new NonCommand.AnswerPair(SAME_OK_ACCOUNT, true);
        }

        String username = getOKUsername(pair.accessToken());

        if (Objects.equals(username, "")) {
            return new NonCommand.AnswerPair(USERNAME_NOT_FOUND, true);
        }

        Account newAccount = new Account(
                chatId,
                OK_SOCIAL_NAME,
                userId,
                username,
                pair.accessToken(),
                pair.refreshToken()
        );

        currentAccountRepository.insertCurrentAccount(
                new CurrentAccount(
                        chatId,
                        newAccount.getSocialMedia().getName(),
                        newAccount.getAccountId(),
                        newAccount.getUserFullName(),
                        newAccount.getAccessToken(),
                        newAccount.getRefreshToken()
                )
        );

        accountsRepository.insertNewAccount(newAccount);

        currentStateRepository.insertCurrentState(new CurrentState(chatId,
                Substate.nextSubstate(State.OkAccountDescription).getIdentifier()));

        return new NonCommand.AnswerPair(
                String.format(OK_AUTH_STATE_ANSWER, State.OkAccountDescription.getIdentifier()),
                false
        );
    }

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

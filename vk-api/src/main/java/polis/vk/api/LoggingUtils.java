package polis.vk.api;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import com.vk.api.sdk.objects.groups.responses.GetByIdLegacyResponse;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQueryWithLegacy;
import com.vk.api.sdk.queries.oauth.OAuthUserAuthorizationCodeFlowQuery;
import com.vk.api.sdk.queries.users.UsersGetQuery;
import org.slf4j.Logger;
import polis.vk.api.exceptions.TokenExpiredException;
import polis.vk.api.exceptions.VkApiException;

import java.util.List;
import java.util.Objects;

public class LoggingUtils {
    public static final Integer PARAM_SESSION_EXPIRED_ERROR_CODE = 28;

    static VkAuthorizator.TokenWithId getAccessToken(OAuthUserAuthorizationCodeFlowQuery request, Logger logger)
            throws VkApiException {
        UserAuthResponse response;
        try {
            response = request.execute();
            return new VkAuthorizator.TokenWithId(
                    response.getAccessToken(),
                    response.getUserId()
            );
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            if (Objects.equals(e.getCode(), PARAM_SESSION_EXPIRED_ERROR_CODE)) {
                throw new TokenExpiredException();
            }

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        }
    }

    static String getUsername(UsersGetQuery request, Logger logger) throws VkApiException {
        List<GetResponse> response;
        try {
            response = request.execute();
            for (GetResponse getResponse : response) {
                String firstName = getResponse.getFirstName();
                String lastName = getResponse.getLastName();
                if (firstName != null || lastName != null) {
                    return firstName == null ? lastName : (lastName == null ? firstName :
                            firstName.concat(" ").concat(lastName));
                }
            }
            return null;
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            if (Objects.equals(e.getCode(), PARAM_SESSION_EXPIRED_ERROR_CODE)) {
                throw new TokenExpiredException();
            }

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        }
    }

    static Integer getGroupId(GroupsGetByIdQueryWithLegacy request, Logger logger) throws VkApiException {
        try {
            List<GetByIdLegacyResponse> response = request.execute();
            return response.get(0).getId();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            if (Objects.equals(e.getCode(), PARAM_SESSION_EXPIRED_ERROR_CODE)) {
                throw new TokenExpiredException();
            }

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static String getGroupName(GroupsGetByIdQueryWithLegacy request, Logger logger) throws VkApiException {
        try {
            List<GetByIdLegacyResponse> response = request.execute();
            return response.get(0).getName();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            if (Objects.equals(e.getCode(), PARAM_SESSION_EXPIRED_ERROR_CODE)) {
                throw new TokenExpiredException();
            }

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static Boolean getIsGroupAdmin(GroupsGetByIdQueryWithLegacy request, Logger logger) throws VkApiException {
        try {
            List<GetByIdLegacyResponse> response = request.execute();
            return response.get(0).isAdmin();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            if (Objects.equals(e.getCode(), PARAM_SESSION_EXPIRED_ERROR_CODE)) {
                throw new TokenExpiredException();
            }

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }
}

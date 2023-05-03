package polis.vk.api;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.groups.responses.GetByIdObjectLegacyResponse;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQueryWithObjectLegacy;
import com.vk.api.sdk.queries.oauth.OAuthUserAuthorizationCodeFlowQuery;
import com.vk.api.sdk.queries.users.UsersGetQuery;
import org.slf4j.Logger;
import polis.vk.api.exceptions.VkApiException;

import java.util.List;

public class LoggingUtils {

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

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        }
    }

    static Boolean getIsGroupAdmin(GroupsGetByIdQueryWithObjectLegacy request, Logger logger) throws VkApiException {
        try {
            List<GetByIdObjectLegacyResponse> response = request.execute();
            return response.get(0).isAdmin();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
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

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        }
    }

    static Integer getGroupId(GroupsGetByIdQueryWithObjectLegacy request, Logger logger) throws VkApiException {
        try {
            List<GetByIdObjectLegacyResponse> response = request.execute();
            return response.get(0).getId();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static String getGroupName(GroupsGetByIdQueryWithObjectLegacy request, Logger logger) throws VkApiException {
        try {
            List<GetByIdObjectLegacyResponse> response = request.execute();
            return response.get(0).getName();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }
}
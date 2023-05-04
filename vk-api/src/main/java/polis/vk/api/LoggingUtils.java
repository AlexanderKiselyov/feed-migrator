package polis.vk.api;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.base.responses.GetUploadServerResponse;
import com.vk.api.sdk.objects.docs.responses.DocUploadResponse;
import com.vk.api.sdk.objects.groups.responses.GetByIdObjectLegacyResponse;
import com.vk.api.sdk.objects.photos.responses.GetWallUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.SaveWallPhotoResponse;
import com.vk.api.sdk.objects.photos.responses.WallUploadResponse;
import com.vk.api.sdk.objects.polls.responses.CreateResponse;
import com.vk.api.sdk.objects.responses.VideoUploadResponse;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import com.vk.api.sdk.objects.video.responses.SaveResponse;
import com.vk.api.sdk.objects.wall.responses.PostResponse;
import com.vk.api.sdk.queries.docs.DocsGetWallUploadServerQuery;
import com.vk.api.sdk.queries.docs.DocsSaveQuery;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQueryWithObjectLegacy;
import com.vk.api.sdk.queries.oauth.OAuthUserAuthorizationCodeFlowQuery;
import com.vk.api.sdk.queries.photos.PhotosGetWallUploadServerQuery;
import com.vk.api.sdk.queries.photos.PhotosSaveWallPhotoQuery;
import com.vk.api.sdk.queries.polls.PollsCreateQuery;
import com.vk.api.sdk.queries.upload.UploadDocQuery;
import com.vk.api.sdk.queries.upload.UploadPhotoWallQuery;
import com.vk.api.sdk.queries.upload.UploadVideoQuery;
import com.vk.api.sdk.queries.users.UsersGetQuery;
import com.vk.api.sdk.queries.video.VideoSaveQuery;
import com.vk.api.sdk.queries.wall.WallPostQuery;
import org.slf4j.Logger;
import polis.vk.api.exceptions.VkApiException;

import java.net.URI;
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

    static URI getVideoUploadLink(VideoSaveQuery request, Logger logger) throws VkApiException {
        try {
            SaveResponse response = request.execute();
            return response.getUploadUrl();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static Integer uploadVideo(UploadVideoQuery request, Logger logger) throws VkApiException {
        try {
            VideoUploadResponse response = request.execute();
            return response.getVideoId();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static URI getPhotoUploadLink(PhotosGetWallUploadServerQuery request, Logger logger) throws VkApiException {
        try {
            GetWallUploadServerResponse response = request.execute();
            return response.getUploadUrl();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static ServerPhoto uploadPhoto(UploadPhotoWallQuery request, Logger logger) throws VkApiException {
        try {
            WallUploadResponse response = request.execute();
            return new ServerPhoto(
                    response.getPhoto(),
                    response.getServer(),
                    response.getHash()
            );
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static Integer getPhotoId(PhotosSaveWallPhotoQuery request, Logger logger) throws VkApiException {
        try {
            List<SaveWallPhotoResponse> response = request.execute();
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

    static Integer getPollId(PollsCreateQuery request, Logger logger) throws VkApiException {
        try {
            CreateResponse response = request.execute();
            return response.getId();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static URI getDocumentUploadLink(DocsGetWallUploadServerQuery request, Logger logger) throws VkApiException {
        try {
            GetUploadServerResponse response = request.execute();
            return response.getUploadUrl();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static String uploadDocument(UploadDocQuery request, Logger logger) throws VkApiException {
        try {
            DocUploadResponse response = request.execute();
            return response.getFile();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static Integer getDocumentId(DocsSaveQuery request, Logger logger) throws VkApiException {
        try {
            com.vk.api.sdk.objects.docs.responses.SaveResponse response = request.execute();
            return response.getDoc().getId();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    static void postMediaTopic(WallPostQuery request, Logger logger) throws VkApiException {
        try {
            PostResponse response = request.execute();
        } catch (ApiException e) {
            logger.error(String.format("Received error from VK: %s", e.getMessage()));

            throw new VkApiException(String.format("Получена ошибка от сервера ВКонтакте: %s", e.getMessage()));
        } catch (ClientException e) {
            logger.error(String.format("Failed to parse response: %s", e.getMessage()));

            throw new VkApiException(String.format("Сервер ВКонтакте ответил в некорректном формате: %s",
                    e.getMessage()));
        }
    }

    public record ServerPhoto(String photo, Integer server, String hash) {

    }
}

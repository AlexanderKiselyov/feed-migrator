package polis.vk.api;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.docs.DocsGetWallUploadServerQuery;
import com.vk.api.sdk.queries.docs.DocsSaveQuery;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQueryWithObjectLegacy;
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
import org.slf4j.LoggerFactory;
import polis.vk.api.exceptions.VkApiException;

import java.io.File;
import java.net.URI;
import java.util.List;

import static polis.vk.api.LoggingUtils.getDocumentId;
import static polis.vk.api.LoggingUtils.getDocumentUploadLink;
import static polis.vk.api.LoggingUtils.getGroupId;
import static polis.vk.api.LoggingUtils.getGroupName;
import static polis.vk.api.LoggingUtils.getIsGroupAdmin;
import static polis.vk.api.LoggingUtils.getPhotoId;
import static polis.vk.api.LoggingUtils.getPhotoUploadLink;
import static polis.vk.api.LoggingUtils.getPollId;
import static polis.vk.api.LoggingUtils.getUsername;
import static polis.vk.api.LoggingUtils.getVideoUploadLink;
import static polis.vk.api.LoggingUtils.postMediaTopic;
import static polis.vk.api.LoggingUtils.uploadDocument;
import static polis.vk.api.LoggingUtils.uploadPhoto;
import static polis.vk.api.LoggingUtils.uploadVideo;

public class VkApiMethods {
    TransportClient transportClient = HttpTransportClient.getInstance();
    VkApiClient vk = new VkApiClient(transportClient);
    private static final Logger logger = LoggerFactory.getLogger(VkApiMethods.class);

    public Boolean getIsVkGroupAdmin(VkAuthorizator.TokenWithId tokenWithId, String groupLink) throws VkApiException {
        String[] groupLinkParts = groupLink.split("/");

        GroupsGetByIdQueryWithObjectLegacy request = vk.groups()
                .getByIdObjectLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(groupLinkParts[groupLinkParts.length - 1])
                .fields();

        return getIsGroupAdmin(request, logger);
    }

    public String getVkUsername(VkAuthorizator.TokenWithId tokenWithId) throws VkApiException {
        UsersGetQuery request = vk.users()
                .get(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .fields();

        return getUsername(request, logger);
    }

    public Integer getVkGroupId(VkAuthorizator.TokenWithId tokenWithId, String groupLink) throws VkApiException {
        String[] groupLinkParts = groupLink.split("/");

        GroupsGetByIdQueryWithObjectLegacy request = vk.groups()
                .getByIdObjectLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(groupLinkParts[groupLinkParts.length - 1])
                .fields();

        return getGroupId(request, logger);
    }

    public String getVkGroupName(VkAuthorizator.TokenWithId tokenWithId, Long groupId) throws VkApiException {
        GroupsGetByIdQueryWithObjectLegacy request = vk.groups()
                .getByIdObjectLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(String.valueOf(groupId))
                .fields();

        return getGroupName(request, logger);
    }

    public URI getVkVideoUploadLink(Integer userId, String accessToken, long groupId)
            throws VkApiException {
        VideoSaveQuery request = vk.videos()
                .save(new UserActor(userId, accessToken))
                .groupId((int) groupId);

        return getVideoUploadLink(request, logger);
    }

    public Integer uploadVkVideo(String uploadUrl, File video) throws VkApiException {
        UploadVideoQuery request = vk.upload()
                .video(uploadUrl, video);

        return uploadVideo(request, logger);
    }

    public URI getVkPhotoUploadLink(Integer userId, String accessToken, long groupId) throws VkApiException {
        PhotosGetWallUploadServerQuery request = vk.photos()
                .getWallUploadServer(new UserActor(userId, accessToken))
                .groupId((int) groupId);

        return getPhotoUploadLink(request, logger);
    }

    public LoggingUtils.ServerPhoto uploadVkPhotos(String uploadUrl, File photo) throws VkApiException {
        UploadPhotoWallQuery request = vk.upload()
                .photoWall(uploadUrl, photo);

        return uploadPhoto(request, logger);
    }

    public Integer getVkPhotoId(Integer userId, long groupId, String accessToken,
                                                LoggingUtils.ServerPhoto serverPhoto) throws VkApiException {
        PhotosSaveWallPhotoQuery request = vk.photos()
                .saveWallPhoto(new UserActor(userId, accessToken), serverPhoto.photo())
                .server(serverPhoto.server())
                .hash(serverPhoto.hash())
                .groupId((int) groupId);

        return getPhotoId(request, logger);
    }

    public Integer getVkPollId(Integer userId, String accessToken, String question, Boolean isAnonymous,
                               Boolean isMultiple, Boolean isClosed, List<String> answers) throws VkApiException {
        PollsCreateQuery request = vk.polls()
                .create(new UserActor(userId, accessToken))
                .question(question)
                .isAnonymous(isAnonymous)
                .isMultiple(isMultiple)
                .disableUnvote(isClosed)
                .addAnswers("[\"".concat(String.join("\",\"", answers)).concat("\"]"));

        return getPollId(request, logger);
    }

    public URI getVkDocumentUploadLink(Integer userId, String accessToken, long groupId) throws VkApiException {
        DocsGetWallUploadServerQuery request = vk.docs()
                .getWallUploadServer(new UserActor(userId, accessToken))
                .groupId((int) groupId);

        return getDocumentUploadLink(request, logger);
    }

    public String uploadVkDocument(String uploadUrl, File document) throws VkApiException {
        UploadDocQuery response = vk.upload()
                .doc(uploadUrl, document);

        return uploadDocument(response, logger);
    }

    public Integer getVkDocumentId(Integer userId, String accessToken, String file) throws VkApiException {
        DocsSaveQuery request = vk.docs()
                .save(new UserActor(userId, accessToken), file);

        return getDocumentId(request, logger);
    }

    public void postVkMediaTopic(Integer userId, String accessToken, long groupId, String message, String attachments)
            throws VkApiException {
        WallPostQuery request = vk.wall()
                .post(new UserActor(userId, accessToken))
                .ownerId((int) -groupId);

        if (message != null) {
            request = request.message(message);
        }

        if (attachments != null && !attachments.isEmpty()) {
            request = request.attachments(attachments);
        }

        postMediaTopic(request, logger);
    }
}

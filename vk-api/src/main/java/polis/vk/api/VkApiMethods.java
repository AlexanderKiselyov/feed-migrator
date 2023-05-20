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
import com.vk.api.sdk.queries.utils.UtilsGetShortLinkQuery;
import com.vk.api.sdk.queries.video.VideoSaveQuery;
import com.vk.api.sdk.queries.wall.WallPostQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.vk.api.exceptions.VkApiException;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static polis.vk.api.LoggingUtils.getDocumentId;
import static polis.vk.api.LoggingUtils.getDocumentUploadLink;
import static polis.vk.api.LoggingUtils.getGroupId;
import static polis.vk.api.LoggingUtils.getGroupName;
import static polis.vk.api.LoggingUtils.getIsGroupAdmin;
import static polis.vk.api.LoggingUtils.getPhotoId;
import static polis.vk.api.LoggingUtils.getPhotoUploadLink;
import static polis.vk.api.LoggingUtils.getPollId;
import static polis.vk.api.LoggingUtils.getShortLink;
import static polis.vk.api.LoggingUtils.getUsername;
import static polis.vk.api.LoggingUtils.getVideoUploadLink;
import static polis.vk.api.LoggingUtils.postMediaTopic;
import static polis.vk.api.LoggingUtils.uploadDocument;
import static polis.vk.api.LoggingUtils.uploadPhoto;
import static polis.vk.api.LoggingUtils.uploadVideo;

public class VkApiMethods {
    TransportClient transportClient = HttpTransportClient.getInstance();
    VkApiClient vk = new VkApiClient(transportClient);
    private static final Logger LOGGER = LoggerFactory.getLogger(VkApiMethods.class);

    public Boolean getIsVkGroupAdmin(VkAuthorizator.TokenWithId tokenWithId, String groupLink) throws VkApiException {
        String[] groupLinkParts = groupLink.split("/");

        GroupsGetByIdQueryWithObjectLegacy request = vk.groups()
                .getByIdObjectLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(groupLinkParts[groupLinkParts.length - 1])
                .fields();

        return getIsGroupAdmin(request, LOGGER);
    }

    public String getVkUsername(VkAuthorizator.TokenWithId tokenWithId) throws VkApiException {
        UsersGetQuery request = vk.users()
                .get(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .fields();

        return getUsername(request, LOGGER);
    }

    public Integer getVkGroupId(VkAuthorizator.TokenWithId tokenWithId, String groupLink) throws VkApiException {
        String[] groupLinkParts = groupLink.split("/");

        GroupsGetByIdQueryWithObjectLegacy request = vk.groups()
                .getByIdObjectLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(groupLinkParts[groupLinkParts.length - 1])
                .fields();

        return getGroupId(request, LOGGER);
    }

    public String getVkGroupName(VkAuthorizator.TokenWithId tokenWithId, Integer groupId) throws VkApiException {
        GroupsGetByIdQueryWithObjectLegacy request = vk.groups()
                .getByIdObjectLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(String.valueOf(groupId))
                .fields();

        return getGroupName(request, LOGGER);
    }

    public URI getVkVideoUploadLink(Integer userId, String accessToken, long groupId)
            throws VkApiException {
        VideoSaveQuery request = vk.videos()
                .save(new UserActor(userId, accessToken))
                .groupId((int) groupId);

        return getVideoUploadLink(request, LOGGER);
    }

    public Integer uploadVkVideo(String uploadUrl, File video) throws VkApiException {
        UploadVideoQuery request = vk.upload()
                .video(uploadUrl, video);

        return uploadVideo(request, LOGGER);
    }

    public URI getVkPhotoUploadLink(Integer userId, String accessToken, long groupId) throws VkApiException {
        PhotosGetWallUploadServerQuery request = vk.photos()
                .getWallUploadServer(new UserActor(userId, accessToken))
                .groupId((int) groupId);

        return getPhotoUploadLink(request, LOGGER);
    }

    public LoggingUtils.ServerPhoto uploadVkPhotos(String uploadUrl, File photo) throws VkApiException {
        UploadPhotoWallQuery request = vk.upload()
                .photoWall(uploadUrl, photo);

        return uploadPhoto(request, LOGGER);
    }

    public Integer getVkPhotoId(Integer userId, long groupId, String accessToken,
                                                LoggingUtils.ServerPhoto serverPhoto) throws VkApiException {
        PhotosSaveWallPhotoQuery request = vk.photos()
                .saveWallPhoto(new UserActor(userId, accessToken), serverPhoto.photo())
                .server(serverPhoto.server())
                .hash(serverPhoto.hash())
                .groupId((int) groupId);

        return getPhotoId(request, LOGGER);
    }

    public Integer getVkPollId(Integer userId, String accessToken, String question, Boolean isAnonymous,
                               Boolean isMultiple, Boolean isClosed, List<String> answers) throws VkApiException {
        List<String> allAnswers = new ArrayList<>();
        for (String answer : answers) {
            allAnswers.add(answer.replaceAll("\"", "'"));
        }
        PollsCreateQuery request = vk.polls()
                .create(new UserActor(userId, accessToken))
                .question(question)
                .isAnonymous(isAnonymous)
                .isMultiple(isMultiple)
                .disableUnvote(isClosed)
                .addAnswers("[\"".concat(String.join("\",\"", allAnswers)).concat("\"]"));

        return getPollId(request, LOGGER);
    }

    public URI getVkDocumentUploadLink(Integer userId, String accessToken, long groupId) throws VkApiException {
        DocsGetWallUploadServerQuery request = vk.docs()
                .getWallUploadServer(new UserActor(userId, accessToken))
                .groupId((int) groupId);

        return getDocumentUploadLink(request, LOGGER);
    }

    public String uploadVkDocument(String uploadUrl, File document) throws VkApiException {
        UploadDocQuery response = vk.upload()
                .doc(uploadUrl, document);

        return uploadDocument(response, LOGGER);
    }

    public Integer getVkDocumentId(Integer userId, String accessToken, String file) throws VkApiException {
        DocsSaveQuery request = vk.docs()
                .save(new UserActor(userId, accessToken), file);

        return getDocumentId(request, LOGGER);
    }

    public long postVkMediaTopic(Integer userId, String accessToken, long groupId, String message, String attachments)
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

        return postMediaTopic(request, groupId, LOGGER);
    }

    public String getVkShortLink(Integer userId, String accessToken, String link) throws VkApiException {
        UtilsGetShortLinkQuery request = vk.utils()
                .getShortLink(new UserActor(userId, accessToken), link)
                .privateParam(true);

        return getShortLink(request, LOGGER).toString();
    }
}

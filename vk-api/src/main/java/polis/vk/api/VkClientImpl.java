package polis.vk.api;

import polis.vk.api.exceptions.VkApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VkClientImpl implements VkClient {
    private final VkApiMethods vkApiMethods = new VkApiMethods();

    @Override
    public void postMediaTopic(Integer userId, String accessToken, long groupId, String message, String attachments)
            throws VkApiException {
        vkApiMethods.postVkMediaTopic(userId, accessToken, groupId, message, attachments);
    }

    @Override
    public long uploadVideo(Integer userId, String accessToken, long groupId, File video) throws VkApiException {
        return vkApiMethods.uploadVkVideo(
                vkApiMethods.getVkVideoUploadLink(userId, accessToken, groupId).toString(),
                video
        );
    }

    @Override
    public List<String> uploadPhotos(Integer userId, String accessToken, long groupId, List<File> photos)
            throws VkApiException {
        List<String> photoIds = new ArrayList<>();

        for (File photo : photos) {
            photoIds.add(String.valueOf(vkApiMethods.getVkPhotoId(
                    userId,
                    groupId,
                    accessToken,
                    vkApiMethods.uploadVkPhotos(
                            vkApiMethods.getVkPhotoUploadLink(userId, accessToken, groupId).toString(),
                            photo
                    )
            )));
        }

        return photoIds;
    }

    @Override
    public String createPoll(Integer userId, String accessToken, String question, Boolean isAnonymous,
                             Boolean isMultiple, Boolean isClosed, List<String> answers) throws VkApiException {
        return String.valueOf(vkApiMethods.getVkPollId(userId, accessToken, question, isAnonymous, isMultiple,
                isClosed, answers));
    }

    @Override
    public List<String> saveDocuments(List<File> documents, Integer userId, String accessToken, long groupId)
            throws VkApiException {
        List<String> documentIds = new ArrayList<>();

        for (File document : documents) {
            documentIds.add(String.valueOf(vkApiMethods.getVkDocumentId(
                    userId,
                    accessToken,
                    vkApiMethods.uploadVkDocument(
                            vkApiMethods.getVkDocumentUploadLink(userId, accessToken, groupId).toString(),
                            document
                    )
            )));
        }

        return documentIds;
    }
}

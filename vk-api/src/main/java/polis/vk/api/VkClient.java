package polis.vk.api;

import polis.vk.api.exceptions.VkApiException;

import java.io.File;
import java.util.List;

public interface VkClient {
    long postMediaTopic(Integer userId, String accessToken, long groupId, String message, String attachments)
            throws VkApiException;

    long uploadVideo(Integer userId, String accessToken, long groupId, File video)
            throws VkApiException;

    List<String> uploadPhotos(Integer userId, String accessToken, long groupId, List<File> photos)
            throws VkApiException;

    String createPoll(Integer userId, String accessToken, String question, Boolean isAnonymous, Boolean isMultiple,
                      Boolean isClosed, List<String> answers) throws VkApiException;

    List<String> saveDocuments(List<File> documents, Integer userId, String accessToken, long groupId)
            throws VkApiException;
}

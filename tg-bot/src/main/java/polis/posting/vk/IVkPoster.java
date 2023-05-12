package polis.posting.vk;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.posting.ApiException;
import polis.vk.api.exceptions.VkApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface IVkPoster {
    List<String> uploadPhotos(List<File> photos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    List<String> uploadVideos(List<File> videos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    List<String> uploadDocuments(List<File> documents, Integer userId, String accessToken, long groupId)
            throws VkApiException;

    String uploadPoll(Integer userId, String accessToken, String question, Boolean isAnonymous,
                      Boolean isMultiple, Boolean isClosed, List<String> answers)
            throws VkApiException, URISyntaxException, IOException;

    String getTextLinks(String text, List<MessageEntity> textLinks, String accessToken, Integer ownerId)
            throws URISyntaxException, IOException, ApiException;

    IVkPost newPost(Long ownerId, String accessToken);

    interface IVkPost {

        IVkPost addPhotos(List<String> photoIds);

        IVkPost addVideos(List<String> videoIds, long groupId);

        IVkPost addTextWithLinks(String formattedText);

        IVkPost addPoll(Poll poll, String pollId);

        IVkPost addDocuments(List<String> documentIds, long groupId);

        long post(Integer userId, long groupId)
                throws URISyntaxException, IOException, ApiException;

    }
}

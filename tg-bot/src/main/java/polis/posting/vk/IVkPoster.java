package polis.posting.vk;

import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.posting.ApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface IVkPoster {
    List<String> uploadPhotos(List<File> photos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    List<String> uploadVideos(List<File> videos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    IVkPost newPost(Long ownerId);

    interface IVkPost {

        IVkPost addPhotos(List<String> photoIds);

        IVkPost addVideos(List<String> videoIds, long groupId);

        IVkPost addText(String text);

        IVkPost addPoll(Poll poll, String pollId);

        IVkPost addDocuments(List<String> documentIds, long groupId);

        void post(Integer userId, String accessToken, long groupId)
                throws URISyntaxException, IOException, ApiException;

    }
}

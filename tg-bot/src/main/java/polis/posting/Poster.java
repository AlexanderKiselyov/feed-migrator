package polis.posting;

import org.telegram.telegrambots.meta.api.objects.polls.Poll;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface Poster {

    List<String> uploadPhotos(List<File> photos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    List<String> uploadVideos(List<File> videos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    Post newPost(Long ownerId);

    interface Post {

        Post addPhotos(List<String> photoIds);

        Post addVideos(List<String> videoIds);

        Post addText(String text);

        Post addPoll(Poll poll, String pollId);

        Post addDocuments(List<String> documentIds);

        void post(Integer userId, String accessToken, long groupId) throws URISyntaxException, IOException, ApiException;

    }
}



package polis.posting;

import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface Poster {

    List<String> uploadPhotos(List<File> photos, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    List<String> uploadVideos(List<File> videos, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    Post newPost();

    interface Post {

        Post addPhotos(List<String> photoIds);

        Post addVideos(List<String> videoIds);

        Post addText(String text);

        Post addPoll(Poll poll);

        Post addDocuments(List<File> documents);

        void post(String accessToken, long groupId) throws URISyntaxException, IOException, ApiException;

    }
}



package polis.posting.ok;

import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.posting.ApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface IOkPoster {

    List<String> uploadPhotos(List<File> photos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    List<String> uploadVideos(List<File> videos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException;

    IOkPost newPost();

    interface IOkPost {

        IOkPost addPhotos(List<String> photoIds);

        IOkPost addVideos(List<String> videoIds);

        IOkPost addText(String text);

        IOkPost addPoll(Poll poll);

        IOkPost addDocuments(List<String> documentIds);

        long post(String accessToken, long groupId) throws URISyntaxException, IOException, ApiException;
    }
}



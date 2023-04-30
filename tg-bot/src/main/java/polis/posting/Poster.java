package polis.posting;

import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface Poster {

    Post newPost(long groupId, String accessToken);

    abstract class Post {
        final long groupId;
        final String accessToken;

        Post(long groupId, String accessToken) {
            this.groupId = groupId;
            this.accessToken = accessToken;
        }

        public abstract Post addPhotos(List<File> photos) throws URISyntaxException, IOException, TelegramApiException, ApiException;

        public abstract Post addVideos(List<File> videos) throws URISyntaxException, IOException, TelegramApiException, ApiException;

        public abstract Post addText(String text);

        public abstract Post addPoll(Poll poll);

        public abstract Post addAnimations(List<File> animations) throws URISyntaxException, IOException, TelegramApiException, ApiException;

        public abstract Post addDocuments(List<File> documents);

        public abstract void post(String accessToken) throws URISyntaxException, IOException, ApiException;

        public void post() throws URISyntaxException, IOException, ApiException {
            post(accessToken);
        }

    }
}



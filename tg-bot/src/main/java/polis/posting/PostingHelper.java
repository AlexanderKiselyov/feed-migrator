package polis.posting;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public abstract class PostingHelper {
    protected final TgApiHelper tgApiHelper;

    protected PostingHelper(TgApiHelper tgApiHelper) {
        this.tgApiHelper = tgApiHelper;
    }

    public abstract Post newPost(long groupId, String accessToken);

    public abstract static class Post {
        final long groupId;
        final String accessToken;

        Post(long groupId, String accessToken) {
            this.groupId = groupId;
            this.accessToken = accessToken;
        }

        public abstract Post addPhotos(List<PhotoSize> photos) throws URISyntaxException, IOException, TelegramApiException, ApiException;

        public abstract Post addVideos(List<Video> videos) throws URISyntaxException, IOException, TelegramApiException, ApiException;

        public abstract Post addText(String text);

        public abstract Post addPoll(Poll poll);

        public abstract Post addAnimations(List<Animation> animations) throws URISyntaxException, IOException, TelegramApiException, ApiException;

        public abstract Post addDocuments(List<Document> documents);

        public abstract void post(String accessToken, long groupId) throws URISyntaxException, IOException, ApiException;
    }
}



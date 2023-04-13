package polis.bot;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

abstract class PostingHelper {
    final Bot bot;
    final String botToken;
    final TgApiHelper tgApiHelper;

    PostingHelper(Bot bot, String botToken, TgApiHelper tgApiHelper) {
        this.bot = bot;
        this.botToken = botToken;
        this.tgApiHelper = tgApiHelper;
    }

    abstract Post newPost(long chatId, long groupId, String accessToken);

    abstract static class Post {
        final long sourceChatId;
        final long groupId;
        final String accessToken;

        Post(long sourceChatId, long groupId, String accessToken) {
            this.sourceChatId = sourceChatId;
            this.groupId = groupId;
            this.accessToken = accessToken;
        }

        abstract Post addPhotos(List<PhotoSize> photos) throws URISyntaxException, IOException, TelegramApiException;

        abstract Post addVideos(List<Video> videos) throws URISyntaxException, IOException, TelegramApiException;

        abstract Post addText(String text);

        abstract Post addPoll(Poll poll);

        abstract Post addAnimations(List<Animation> animations) throws URISyntaxException, IOException, TelegramApiException;

        abstract Post addDocuments(List<Document> documents);

        abstract void post(String accessToken, long groupId) throws URISyntaxException, IOException;
    }
}



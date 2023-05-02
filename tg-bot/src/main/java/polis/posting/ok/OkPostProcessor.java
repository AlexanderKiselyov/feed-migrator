package polis.posting.ok;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.bot.TgContentManager;
import polis.bot.TgNotificator;
import polis.posting.ApiException;
import polis.posting.PostProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class OkPostProcessor extends PostProcessor {
    private final OkPoster okPoster;

    public OkPostProcessor(TgNotificator tgNotificator, TgContentManager tgContentManager, OkPoster okPoster) {
        super(tgNotificator, tgContentManager);
        this.okPoster = okPoster;
    }

    @Override
    public void processPostInChannel(
            List<Video> videos,
            List<PhotoSize> photos,
            List<Animation> animations,
            List<Document> documents,
            String text,
            Poll poll,
            long ownerChatId,
            long channelId,
            long groupId,
            String accessToken
    ) {
        //Здесь можно будет сделать маленькие трайи, чтобы пользователю писать более конкретную ошибку
        try {
            if (!documents.isEmpty() && animations.isEmpty()) {
                tgNotificator.sendMessage(ownerChatId, channelId, """
                                       Тип 'Документ' не поддерживается в социальной сети Одноклассники""");
            }

            int maxListSize = Math.max(photos.size(), animations.size() + videos.size());
            List<File> files = new ArrayList<>(maxListSize);
            for (Video video : videos) {
                File file = tgContentManager.download(video);
                files.add(file);
            }
            for (Video animation : TgContentManager.toVideos(animations)) {
                File file = tgContentManager.download(animation);
                files.add(file);
            }
            List<String> videoIds = okPoster.uploadVideos(files, accessToken, groupId);
            files.clear();

            for (PhotoSize photo : photos) {
                File file = tgContentManager.download(photo);
                files.add(file);
            }
            List<String> photoIds = okPoster.uploadPhotos(files, accessToken, groupId);

            okPoster.newPost()
                    .addVideos(videoIds)
                    .addPhotos(photoIds)
                    .addPoll(poll)
                    .addText(text)
                    .post(accessToken, groupId);
            sendSuccess(channelId, ownerChatId, "ok.ru/group/" + groupId);
        } catch (URISyntaxException | IOException | ApiException | TelegramApiException e) {
            tgNotificator.sendMessage(ownerChatId, channelId, ERROR_POST_MSG + groupId);
        }
    }
}
package polis.posting.ok;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.bot.TgContentManager;
import polis.posting.ApiException;
import polis.posting.PostProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class OkPostProcessor extends PostProcessor {
    private static final String DOCUMENTS_ARENT_SUPPORTED =
            "Тип файла 'Документ' не поддерживается в социальной сети Одноклассники";
    private static final String GROUPS_LINK = "ok.ru/group/";
    private static final String POST_PREFIX = "/topic/";

    private final OkPoster okPoster;

    @Autowired
    public OkPostProcessor(TgContentManager tgContentManager, OkPoster okPoster) {
        super(tgContentManager);
        this.okPoster = okPoster;
    }

    @Override
    public String processPostInChannel(
            List<Video> videos,
            List<PhotoSize> photos,
            List<Animation> animations,
            List<Document> documents,
            List<MessageEntity> textLinks,
            String text,
            Poll poll,
            long ownerChatId,
            long channelId,
            long groupId,
            long accountId,
            String accessToken
    ) {
        //Здесь можно будет сделать маленькие трайи, чтобы пользователю писать более конкретную ошибку
        try {
            if (!documents.isEmpty() && animations.isEmpty()) {
                return DOCUMENTS_ARENT_SUPPORTED;
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
            List<String> videoIds = okPoster.uploadVideos(files, (int) accountId, accessToken, groupId);
            files.clear();

            for (PhotoSize photo : photos) {
                File file = tgContentManager.download(photo);
                files.add(file);
            }
            List<String> photoIds = okPoster.uploadPhotos(files, (int) accountId, accessToken, groupId);

            long postId = okPoster.newPost(accessToken)
                    .addVideos(videoIds)
                    .addPhotos(photoIds)
                    .addPoll(poll)
                    .addTextWithLinks(text, textLinks)
                    .post(groupId);
            return successfulPostMsg(postLink(groupId, postId));
        } catch (URISyntaxException | IOException | ApiException | TelegramApiException e) {
            return failPostToGroupMsg(groupLink(groupId));
        }
    }

    private static String groupLink(long groupId) {
        return GROUPS_LINK + groupId;
    }

    private static String postLink(long groupId, long postId) {
        return groupLink(groupId) + POST_PREFIX + postId;
    }
}

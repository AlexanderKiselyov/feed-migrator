package polis.posting.vk;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.bot.TgContentManager;
import polis.bot.TgNotificator;
import polis.posting.ApiException;
import polis.posting.PostProcessor;
import polis.ratelim.RateLimiter;
import polis.vk.api.LoggingUtils;
import polis.vk.api.exceptions.VkApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class VkPostProcessor extends PostProcessor {
    private static final String VK_GROUP_URL = "vk.com/club";
    private final VkPoster vkPoster;

    public VkPostProcessor(
            @Qualifier("Bot") TgNotificator tgNotificator,
            TgContentManager tgContentManager,
            VkPoster vkPoster,
            RateLimiter postingRateLimiter
    ) {
        super(tgNotificator, tgContentManager, postingRateLimiter);
        this.vkPoster = vkPoster;
    }

    @Override
    protected void processPostInChannel(
            List<Video> videos,
            List<PhotoSize> photos,
            List<Animation> animations,
            List<Document> documents,
            String text,
            Poll poll,
            long ownerChatId,
            long channelId,
            long groupId,
            long userId,
            String accessToken
    ) {
        try {
            int maxListSize = Collections.max(List.of(photos.size(), animations.size() + videos.size(),
                    documents.size()));
            List<File> files = new ArrayList<>(maxListSize);
            for (Video video : videos) {
                File file = tgContentManager.download(video);
                files.add(file);
            }
            for (Video animation : TgContentManager.toVideos(animations)) {
                File file = tgContentManager.download(animation);
                files.add(file);
            }
            List<String> videoIds = vkPoster.uploadVideos(files, (int) userId, accessToken, groupId);
            files.clear();

            for (PhotoSize photo : photos) {
                File file = tgContentManager.download(photo);
                files.add(file);
            }
            List<String> photoIds = vkPoster.uploadPhotos(files, (int) userId, accessToken, groupId);
            files.clear();

            for (Document document : documents) {
                File file = tgContentManager.download(document);
                files.add(file);
            }
            List<String> documentIds = vkPoster.uploadDocuments(files, (int) userId, accessToken, groupId);

            String pollId = null;
            if (poll != null) {
                 pollId = vkPoster.uploadPoll(
                         (int) userId,
                         accessToken,
                         poll.getQuestion(),
                         poll.getIsAnonymous(),
                         poll.getAllowMultipleAnswers(),
                         poll.getIsClosed(),
                         poll.getOptions().stream().map(PollOption::getText).toList()
                );
            }

            vkPoster.newPost(userId)
                    .addPhotos(photoIds)
                    .addVideos(videoIds, groupId)
                    .addText(text)
                    .addPoll(poll, pollId)
                    .addDocuments(documentIds, groupId)
                    .post((int) userId, accessToken, groupId);
            tgNotificator.sendNotification(ownerChatId, channelId, successfulPostToGroupMsg(groupLink(groupId)));
        } catch (VkApiException | ApiException | URISyntaxException | IOException | TelegramApiException e) {
            tgNotificator.sendNotification(ownerChatId, channelId, failPostToGroupMsg(groupLink(groupId)));
        }
    }

    private static String groupLink(long groupId) {
        return VK_GROUP_URL + groupId;
    }
}
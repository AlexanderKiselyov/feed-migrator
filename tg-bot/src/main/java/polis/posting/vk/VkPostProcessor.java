package polis.posting.vk;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.bot.TgContentManager;
import polis.posting.ApiException;
import polis.posting.PostProcessor;
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
    private static final String GROUP_POSTFIX = "?w=wall-";
    private static final String POST_PREFIX = "_";
    private final VkPoster vkPoster;

    public VkPostProcessor(TgContentManager tgContentManager, VkPoster vkPoster) {
        super(tgContentManager);
        this.vkPoster = vkPoster;
    }

    @Override
    protected String processPostInChannel(
            List<Video> videos,
            List<PhotoSize> photos,
            List<Animation> animations,
            List<Document> documents,
            String text,
            Poll poll,
            long ownerChatId,
            long channelId,
            long groupId,
            long accountId,
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
            List<String> videoIds = vkPoster.uploadVideos(files, (int) accountId, accessToken, groupId);
            files.clear();

            for (PhotoSize photo : photos) {
                File file = tgContentManager.download(photo);
                files.add(file);
            }
            List<String> photoIds = vkPoster.uploadPhotos(files, (int) accountId, accessToken, groupId);
            files.clear();

            for (Document document : documents) {
                File file = tgContentManager.download(document);
                files.add(file);
            }
            List<String> documentIds = vkPoster.uploadDocuments(files, (int) accountId, accessToken, groupId);

            String pollId = null;
            if (poll != null) {
                 pollId = vkPoster.uploadPoll(
                         (int) accountId,
                         accessToken,
                         poll.getQuestion(),
                         poll.getIsAnonymous(),
                         poll.getAllowMultipleAnswers(),
                         poll.getIsClosed(),
                         poll.getOptions().stream().map(PollOption::getText).toList()
                );
            }

            long postId = vkPoster.newPost(accountId)
                    .addPhotos(photoIds)
                    .addVideos(videoIds, groupId)
                    .addText(text)
                    .addPoll(poll, pollId)
                    .addDocuments(documentIds, groupId)
                    .post((int) accountId, accessToken, groupId);
            return successfulPostMsg(postLink(groupId, postId));
        } catch (VkApiException | ApiException | URISyntaxException | IOException | TelegramApiException e) {
            return failPostToGroupMsg(groupLink(groupId));
        }
    }

    private static String groupLink(long groupId) {
        return VK_GROUP_URL + groupId;
    }

    private static String postLink(long groupId, long postId) {
        return groupLink(groupId) + GROUP_POSTFIX + groupId + POST_PREFIX + postId;
    }
}

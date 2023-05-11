package polis.posting;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.bot.TgContentManager;
import polis.util.Emojis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class PostProcessor {
    private static final String SUCCESS_POST_MSG = "Успешно опубликовал пост ";
    private static final String ERROR_POST_MSG = "Упс, что-то пошло не так " + Emojis.SAD_FACE + " \n"
            + "Не удалось опубликовать пост в ";
    protected final TgContentManager tgContentManager;

    @Autowired
    public PostProcessor(TgContentManager tgContentManager) {
        this.tgContentManager = tgContentManager;
    }

    protected abstract String processPostInChannel(
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
    );

    public String processPostInChannel(List<Message> postItems, long userChatId, long groupId, long channelId,
                                       long accountId,String accessToken) {
        List<PhotoSize> photos = new ArrayList<>(1);
        List<Video> videos = new ArrayList<>(1);
        String text = null;
        Poll poll = null;
        List<Animation> animations = new ArrayList<>(1);
        List<Document> documents = new ArrayList<>(1);
        for (Message postItem : postItems) {
            if (postItem.hasPhoto()) {
                postItem.getPhoto().stream()
                        .max(Comparator.comparingInt(PhotoSize::getFileSize))
                        .ifPresent(photos::add);
            }
            if (postItem.hasVideo()) {
                videos.add(postItem.getVideo());
            }
            if (postItem.getCaption() != null && !postItem.getCaption().isEmpty()) {
                text = postItem.getCaption();
            }
            if (postItem.hasText() && !postItem.getText().isEmpty()) {
                text = postItem.getText();
            }
            if (postItem.hasPoll()) {
                poll = postItem.getPoll();
            }
            if (postItem.hasAnimation()) {
                animations.add(postItem.getAnimation());
            }
            if (postItem.hasDocument() && !postItem.hasAnimation()) {
                documents.add(postItem.getDocument());
            }
        }
        return processPostInChannel(videos, photos, animations, documents, text, poll, userChatId, channelId, groupId,
                accountId, accessToken);
    }

    protected static String successfulPostMsg(String what) {
        return SUCCESS_POST_MSG + what;
    }

    protected static String failPostToGroupMsg(String where) {
        return ERROR_POST_MSG + where;
    }
}

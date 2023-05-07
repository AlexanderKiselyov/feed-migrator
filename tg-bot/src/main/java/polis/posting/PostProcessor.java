package polis.posting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.bot.TgContentManager;
import polis.bot.TgNotificator;
import polis.ratelim.RateLimiter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class PostProcessor {
    private static final String SUCCESS_POST_MSG = "Успешно опубликовал пост ";
    private static final String ERROR_POST_MSG = "Упс, что-то пошло не так \uD83D\uDE1F \n"
            + "Не удалось опубликовать пост в ";
    private static final String AUTHOR_RIGHTS_MSG = "Пересланный из другого канала пост не может быть опубликован в "
            + "соответствии с Законом об авторском праве.";
    private static final String TOO_MANY_API_REQUESTS_MSG = "Превышено количество публикаций в единицу времени";

    protected final TgNotificator tgNotificator;
    protected final TgContentManager tgContentManager;
    private final RateLimiter postingRateLimiter;

    @Autowired
    public PostProcessor(@Qualifier("Bot") TgNotificator tgNotificator, TgContentManager tgContentManager,
                         RateLimiter postingRateLimiter) {
        this.tgNotificator = tgNotificator;
        this.tgContentManager = tgContentManager;
        this.postingRateLimiter = postingRateLimiter;
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
            long userId,
            String accessToken
    );

    public String processPostInChannel(List<Message> postItems, long ownerChatId, long groupId, long channelId,
                                     long userId, String accessToken) {
        if (!postingRateLimiter.allowRequest(ownerChatId)) {
            return TOO_MANY_API_REQUESTS_MSG;
        }
        List<PhotoSize> photos = new ArrayList<>(1);
        List<Video> videos = new ArrayList<>(1);
        String text = null;
        Poll poll = null;
        List<Animation> animations = new ArrayList<>(1);
        List<Document> documents = new ArrayList<>(1);
        for (Message postItem : postItems) {
            Chat forwardFromChat = postItem.getForwardFromChat();
            if (forwardFromChat != null && forwardFromChat.getId() != channelId) {
                return AUTHOR_RIGHTS_MSG;
            }
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
        return processPostInChannel(videos, photos, animations, documents, text, poll, ownerChatId, channelId, groupId,
                userId, accessToken);
    }

    protected static String successfulPostMsg(String what) {
        return SUCCESS_POST_MSG + what;
    }

    protected static String failPostToGroupMsg(String where) {
        return ERROR_POST_MSG + where;
    }
}

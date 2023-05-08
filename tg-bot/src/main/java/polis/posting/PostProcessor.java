package polis.posting;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.bot.TgContentManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class PostProcessor {
    private static final String SUCCESS_POST_MSG = "Успешно опубликовал пост ";
    private static final String ERROR_POST_MSG = "Упс, что-то пошло не так \uD83D\uDE1F \n"
            + "Не удалось опубликовать пост в ";
    private static final String AUTHOR_RIGHTS_MSG = "Пересланный из другого канала пост не может быть опубликован в "
            + "соответствии с Законом об авторском праве.";
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
            List<MessageEntity> textLinks,
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
        List<MessageEntity> textLinks = new ArrayList<>(1);
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
                List<MessageEntity> captionEntities = postItem.getCaptionEntities();
                if (captionEntities != null && !captionEntities.isEmpty()) {
                    for (MessageEntity entity : captionEntities) {
                        if (entity.getType().equals(EntityType.TEXTLINK)) {
                            textLinks.add(entity);
                        }
                    }
                }
            }
            if (postItem.hasText() && !postItem.getText().isEmpty()) {
                text = postItem.getText();
                if (postItem.hasEntities()) {
                    List<MessageEntity> entities = postItem.getEntities();
                    for (MessageEntity entity : entities) {
                        if (entity.getType().equals(EntityType.TEXTLINK)) {
                            textLinks.add(entity);
                        }
                    }
                }
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
        return processPostInChannel(videos, photos, animations, documents, textLinks, text, poll, userChatId,
                channelId, groupId, accountId, accessToken);
    }

    protected static String successfulPostMsg(String what) {
        return SUCCESS_POST_MSG + what;
    }

    protected static String failPostToGroupMsg(String where) {
        return ERROR_POST_MSG + where;
    }
}

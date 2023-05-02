package polis.posting;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.bot.TgContentManager;
import polis.bot.TgNotificator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class PostProcessor {
    protected static final String ERROR_POST_MSG = "Упс, что-то пошло не так \uD83D\uDE1F \n"
            + "Не удалось опубликовать пост в ok.ru/group/";
    private static final String AUTHOR_RIGHTS_MSG = "Пересланный из другого канала пост не может быть опубликован в "
            + "соответствии с Законом об авторском праве.";

    protected final TgNotificator tgNotificator;
    protected final TgContentManager tgContentManager;

    public PostProcessor(TgNotificator tgNotificator, TgContentManager tgContentManager) {
        this.tgNotificator = tgNotificator;
        this.tgContentManager = tgContentManager;
    }

    protected abstract void processPostInChannel(
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
    );

    protected void sendSuccess(long channelId, long ownerChatId, String groupLink) {
        tgNotificator.sendMessage(channelId, ownerChatId,
                "Успешно опубликовал пост в " + groupLink);
    }

    public void processPostInChannel(List<Message> postItems, long ownerChatId, long groupId, long channelId, String accessToken) {
        List<PhotoSize> photos = new ArrayList<>(1);
        List<Video> videos = new ArrayList<>(1);
        String text = null;
        Poll poll = null;
        List<Animation> animations = new ArrayList<>(1);
        List<Document> documents = new ArrayList<>(1);
        for (Message postItem : postItems) {
            Chat forwardFromChat = postItem.getForwardFromChat();
            if (forwardFromChat != null && forwardFromChat.getId() != channelId) {
                tgNotificator.sendMessage(channelId, ownerChatId, AUTHOR_RIGHTS_MSG);
                return;
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
            if (postItem.hasDocument()) {
                documents.add(postItem.getDocument());
            }
        }
        processPostInChannel(videos, photos, animations, documents, text, poll, ownerChatId, channelId, groupId, accessToken);
    }
}

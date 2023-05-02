package polis.posting;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.bot.TgContentManager;
import polis.bot.TgNotificator;

import java.util.List;

public abstract class PostProcessor {
    protected static final String ERROR_POST_MSG = "Упс, что-то пошло не так \uD83D\uDE1F \n"
            + "Не удалось опубликовать пост в ok.ru/group/";

    protected final TgNotificator tgNotificator;
    protected final TgContentManager tgContentManager;

    public PostProcessor(TgNotificator tgNotificator, TgContentManager tgContentManager) {
        this.tgNotificator = tgNotificator;
        this.tgContentManager = tgContentManager;
    }

    public abstract void processPostInChannel(
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
}

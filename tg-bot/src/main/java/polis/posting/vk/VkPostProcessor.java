package polis.posting.vk;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.bot.TgContentManager;
import polis.bot.TgNotificator;
import polis.posting.PostProcessor;

import java.util.List;

@Component
public class VkPostProcessor extends PostProcessor {
    private final VkPoster vkPoster;

    public VkPostProcessor(@Qualifier("Bot") TgNotificator tgNotificator, TgContentManager tgContentManager,
                           VkPoster vkPoster) {
        super(tgNotificator, tgContentManager);
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
            String accessToken
    ) {

    }
}

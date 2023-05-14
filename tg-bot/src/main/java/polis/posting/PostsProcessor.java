package polis.posting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import polis.bot.TgContentManager;
import polis.bot.TgNotificator;
import polis.data.domain.ChannelGroup;
import polis.data.domain.UserChannels;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.posting.ok.OkPostProcessor;
import polis.posting.vk.VkPostProcessor;
import polis.ratelim.RateLimiter;
import polis.ratelim.Throttler;
import polis.util.Emojis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PostsProcessor implements IPostsProcessor {
    private static final String SINGLE_ITEM_POSTS = "";
    private static final String ERROR_POST_MSG = "Упс, что-то пошло не так " + Emojis.SAD_FACE + " \n"
            + "Не удалось опубликовать пост в ok.ru/group/";
    private static final String CHANNEL_INFO_ERROR = "Ошибка получения информации по каналу.";
    private static final String TOO_MANY_API_REQUESTS_MSG = "Превышено количество публикаций в единицу времени";
    private static final String AUTHOR_RIGHTS_MSG = "Пересланный из другого канала пост не может быть опубликован в "
            + "соответствии с Законом об авторском праве.";
    private static final Logger LOGGER = LoggerFactory.getLogger(PostsProcessor.class);

    @Autowired
    private RateLimiter postingRateLimiter;

    @Autowired
    private Throttler repliesThrottler;

    @Autowired
    private OkPostProcessor okPostProcessor;

    @Autowired
    private VkPostProcessor vkPostProcessor;

    @Autowired
    private UserChannelsRepository userChannelsRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Lazy
    @Autowired
    private TgContentManager tgContentManager;

    @Lazy
    @Autowired
    @Qualifier("Bot")
    private TgNotificator tgNotificator;

    @Override
    public void processPostsInChannel(long channelId, List<Message> posts) {
        Map<String, List<Message>> postsItems = posts.stream().collect(
                Collectors.groupingBy(
                        post -> post.getMediaGroupId() == null ? SINGLE_ITEM_POSTS : post.getMediaGroupId(),
                        Collectors.toList()
                ));
        postsItems.getOrDefault(SINGLE_ITEM_POSTS, Collections.emptyList())
                .forEach(post -> processPostItems(channelId, Collections.singletonList(post)));
        postsItems.remove(SINGLE_ITEM_POSTS);
        postsItems.values().forEach(items -> processPostItems(channelId, items));
    }

    private void processPostItems(long channelId, List<Message> postItems) {
        if (postItems.isEmpty()) {
            return;
        }
        Message postItem = postItems.get(0);
        long ownerChatId = userChannelsRepository.getUserChatId(channelId);
        if (!postingRateLimiter.allowRequest(ownerChatId)) {
            repliesThrottler.throttle(ownerChatId, () ->
                    tgNotificator.sendNotification(ownerChatId, TOO_MANY_API_REQUESTS_MSG)
            );
            return;
        }
        Chat forwardFromChat = postItem.getForwardFromChat();
        if (forwardFromChat != null && forwardFromChat.getId() != channelId) {
            tgNotificator.sendNotification(ownerChatId, AUTHOR_RIGHTS_MSG);
            return;
        }
        try {
            if (!userChannelsRepository.isSetAutoposting(ownerChatId, channelId)) {
                return;
            }
            long userChatId = userChannelsRepository.getUserChatId(channelId);
            UserChannels tgChannel = userChannelsRepository.getUserChannel(channelId, userChatId);
            if (tgChannel == null || !tgChannel.isAutoposting()) {
                return;
            }
            List<String> messagesToChannelOwner = new ArrayList<>();
            for (ChannelGroup group : channelGroupsRepository.getGroupsForChannel(tgChannel.getChannelId())) {
                String accessToken = group.getAccessToken();
                long accountId = group.getAccountId();

                if (accessToken == null) {
                    sendNotificationIfEnabled(ownerChatId, channelId, CHANNEL_INFO_ERROR);
                    continue;
                }

                String message;
                switch (group.getSocialMedia()) {
                    case OK -> message = okPostProcessor.processPostInChannel(postItems, ownerChatId,
                            group.getGroupId(), channelId, accountId, accessToken);
                    case VK -> message = vkPostProcessor.processPostInChannel(postItems, ownerChatId,
                            group.getGroupId(), channelId, accountId, accessToken);
                    default -> {
                        LOGGER.error(String.format("Social media not found: %s",
                                group.getSocialMedia()));
                        message = ERROR_POST_MSG + group.getGroupId();
                    }
                }
                messagesToChannelOwner.add(message);
            }
            String aggregatedMessages = aggregateMessages(messagesToChannelOwner);
            sendNotificationIfEnabled(ownerChatId, channelId, aggregatedMessages);
        } catch (RuntimeException e) {
            LOGGER.error("Error when handling post in " + channelId, e);
            tgNotificator.sendNotification(ownerChatId, "Произошла непредвиденная ошибка при обработке поста " + e);
        }
    }

    private static String aggregateMessages(List<String> messagesToChannelOwner) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : messagesToChannelOwner) {
            stringBuilder.append(message);
            stringBuilder.append("\n\n");
        }
        return stringBuilder.toString();
    }

    private void sendNotificationIfEnabled(long userChatId, long channelId, String message) {
        if (userChannelsRepository.isSetNotification(userChatId, channelId)) {
            tgNotificator.sendNotification(userChatId, message);
        }
    }
}

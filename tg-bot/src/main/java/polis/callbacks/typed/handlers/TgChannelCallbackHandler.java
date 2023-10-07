package polis.callbacks.typed.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.typed.CallbackType;
import polis.callbacks.typed.parsers.TgChannelCallbackParser;
import polis.callbacks.typed.objects.TgChannelCallback;
import polis.commands.context.Context;
import polis.data.domain.CurrentChannel;
import polis.data.domain.UserChannels;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.util.SocialMedia;
import polis.util.State;

import java.util.List;

@Component
public class TgChannelCallbackHandler extends ATypedCallbackHandler<TgChannelCallback> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TgChannelCallbackHandler.class);

    @Autowired
    private UserChannelsRepository userChannelsRepository;
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    public TgChannelCallbackHandler(TgChannelCallbackParser callbackParser) {
        this.callbackParser = callbackParser;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.TG_CHANNEL_CHOSEN;
    }

    @Override
    public void handleCallback(long userChatId, Message message, TgChannelCallback callback, Context context) throws TelegramApiException {
        long channelId = callback.channelId;

        if (callback.isClickedForDeletion) {
            List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(userChatId);
            for (UserChannels ch : tgChannels) {
                if (ch.getChannelId() == channelId) {
                    userChannelsRepository.deleteUserChannel(ch);
                    break;
                }
            }
            context.setCurrentChannel(null);
            for (SocialMedia socialMedia : SocialMedia.values()) {
                channelGroupsRepository.deleteChannelGroup(channelId, socialMedia.getName());
            }
            deleteLastMessage(message);
            processNextCommand(State.TgChannelsList, message, null);
        } else {
            UserChannels currentTelegramChannel = null;
            List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(userChatId);
            for (UserChannels ch : tgChannels) {
                if (ch.getChannelId() == channelId) {
                    currentTelegramChannel = ch;
                    break;
                }
            }
            if (currentTelegramChannel != null) {
                context.setCurrentChannel(new CurrentChannel(
                        userChatId,
                        currentTelegramChannel.getChannelId(),
                        currentTelegramChannel.getChannelUsername()
                ));
                deleteLastMessage(message);
                processNextCommand(State.TgChannelDescription, message,null);
            } else {
                LOGGER.error(String.format("Cannot find such a telegram channel id: %s", channelId));
            }
        }
    }
}
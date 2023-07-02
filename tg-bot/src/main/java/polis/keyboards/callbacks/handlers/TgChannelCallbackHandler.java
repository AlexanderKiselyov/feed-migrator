package polis.keyboards.callbacks.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.data.domain.CurrentChannel;
import polis.data.domain.UserChannels;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.TgChannelCallback;
import polis.keyboards.callbacks.parsers.TgChannelCallbackParser;
import polis.util.SocialMedia;
import polis.util.State;

import java.util.List;

@Component
public class TgChannelCallbackHandler extends ACallbackHandler<TgChannelCallback> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TgChannelCallbackHandler.class);

    @Autowired
    private UserChannelsRepository userChannelsRepository;
    @Autowired
    private CurrentChannelRepository currentChannelRepository;
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    {
        callbackParser = new TgChannelCallbackParser();
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.TG_CHANNEL_CHOSEN;
    }

    @Override
    protected CallbackParser<TgChannelCallback> callbackParser() {
        return callbackParser;
    }

    @Override
    public void handleCallback(long userChatId, Message message, TgChannelCallback callback) throws TelegramApiException {
        long channelId = callback.channelId;

        if (callback.isClickedForDeletion) {
            List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(userChatId);
            for (UserChannels ch : tgChannels) {
                if (ch.getChannelId() == channelId) {
                    userChannelsRepository.deleteUserChannel(ch);
                    break;
                }
            }
            currentChannelRepository.deleteCurrentChannel(userChatId);
            for (SocialMedia socialMedia : SocialMedia.values()) {
                channelGroupsRepository.deleteChannelGroup(channelId, socialMedia.getName());
            }
            deleteLastMessage(message);
            getRegisteredCommand(State.TgChannelsList.getIdentifier())
                    .processMessage(sender, message, null);
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
                currentChannelRepository.insertCurrentChannel(new CurrentChannel(userChatId,
                        currentTelegramChannel.getChannelId(), currentTelegramChannel.getChannelUsername()));
                deleteLastMessage(message);
                getRegisteredCommand(State.TgChannelDescription.getIdentifier())
                        .processMessage(sender, message, null);
            } else {
                LOGGER.error(String.format("Cannot find such a telegram channel id: %s", channelId));
            }
        }
    }
}

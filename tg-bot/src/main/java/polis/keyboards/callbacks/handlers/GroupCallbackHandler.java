package polis.keyboards.callbacks.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.GroupCallback;
import polis.keyboards.callbacks.parsers.GroupCallbackParser;
import polis.util.State;

import java.util.Objects;

@Component
public class GroupCallbackHandler extends ACallbackHandler<GroupCallback> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCallbackHandler.class);
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;
    @Autowired
    private CurrentChannelRepository currentChannelRepository;
    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    public GroupCallbackHandler(GroupCallbackParser callbackParser) {
        this.callbackParser = callbackParser;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.GROUP_CHOSEN;
    }

    @Override
    protected CallbackParser<GroupCallback> callbackParser() {
        return callbackParser;
    }

    @Override
    public void handleCallback(long userChatId, Message message, GroupCallback callback) throws TelegramApiException {
        if (!callback.isClickForDeletion) {
            CurrentGroup currentGroup = getCurrentGroup(userChatId, callback.groupId);
            if (currentGroup != null) {
                currentGroupRepository.insertCurrentGroup(currentGroup);
                deleteLastMessage(message);
                commandRegistry.getRegisteredCommand(State.GroupDescription.getIdentifier())
                        .processMessage(sender, message, null);
            } else {
                LOGGER.error(String.format("Cannot find such a social media group id: %s", callback.groupId));
            }
        } else {
            CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(userChatId);
            String socialMediaName = callback.socialMedia;
            channelGroupsRepository.deleteChannelGroup(currentChannel.getChannelId(), socialMediaName);
            currentGroupRepository.deleteCurrentGroup(userChatId);
            deleteLastMessage(message);
            getRegisteredCommand(State.TgSyncGroups.getIdentifier())
                    .processMessage(sender, message, null);
        }
    }

    private CurrentGroup getCurrentGroup(Long chatId, Long groupId) {
        CurrentGroup currentSocialMedia = null;
        for (ChannelGroup smg : channelGroupsRepository
                .getGroupsForChannel(currentChannelRepository.getCurrentChannel(chatId).getChannelId())) {
            if (Objects.equals(smg.getGroupId(), groupId)) {
                currentSocialMedia = new CurrentGroup(smg.getChatId(), smg.getSocialMedia().getName(), smg.getGroupId(),
                        smg.getGroupName(), smg.getAccountId(), smg.getAccessToken());
                break;
            }
        }
        return currentSocialMedia;
    }
}

package polis.keyboards.callbacks.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentGroup;
import polis.data.domain.UserChannels;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.YesNoCallback;
import polis.keyboards.callbacks.parsers.YesNoCallbackParser;
import polis.util.State;

import java.util.List;
import java.util.Objects;

@Component
public class YesNoCallbackHandler extends ACallbackHandler<YesNoCallback> {
    @Autowired
    private CurrentGroupRepository currentGroupRepository;
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private CurrentAccountRepository currentAccountRepository;
    @Autowired
    private UserChannelsRepository userChannelsRepository;
    @Autowired
    private CurrentChannelRepository currentChannelRepository;
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    {
        callbackParser = new YesNoCallbackParser();
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.YES_NO_ANSWER;
    }

    @Override
    protected CallbackParser<YesNoCallback> callbackParser() {
        return callbackParser;
    }

    @Override
    public void handleCallback(long userChatId, Message message, YesNoCallback callback) throws TelegramApiException {
        if (callback.yes()) {
            CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(userChatId);
            boolean isFound = false;
            for (Account authData : accountsRepository.getAccountsForUser(userChatId)) {
                if (Objects.equals(authData.getAccessToken(),
                        currentAccountRepository.getCurrentAccount(userChatId).getAccessToken())) {
                    List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(userChatId);
                    for (UserChannels tgChannel : tgChannels) {
                        if (Objects.equals(tgChannel.getChannelId(),
                                currentChannelRepository.getCurrentChannel(userChatId).getChannelId())) {
                            channelGroupsRepository.insertChannelGroup(
                                    new ChannelGroup(currentGroup.getAccessToken(),
                                            currentGroup.getGroupName(),
                                            authData.getAccountId(),
                                            currentGroup.getChatId(),
                                            currentGroup.getGroupId(),
                                            authData.getSocialMedia().getName()
                                    ).setChannelId(tgChannel.getChannelId())
                                            .setChannelUsername(tgChannel.getChannelUsername())
                            );
                            isFound = true;
                            break;
                        }
                    }
                    if (isFound) {
                        break;
                    }
                }
            }
            deleteLastMessage(message);
            getRegisteredCommand(State.SyncGroupDescription.getIdentifier())
                    .processMessage(sender, message, null);
        } else {
            currentGroupRepository.deleteCurrentGroup(userChatId);
            deleteLastMessage(message);
            getRegisteredCommand(State.OkAccountDescription.getIdentifier())
                    .processMessage(sender, message, null);
        }
    }
}

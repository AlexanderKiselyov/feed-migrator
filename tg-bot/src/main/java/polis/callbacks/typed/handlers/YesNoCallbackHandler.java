package polis.callbacks.typed.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.typed.objects.YesNoCallback;
import polis.commands.context.Context;
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.UserChannels;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.callbacks.typed.CallbackType;
import polis.callbacks.typed.parsers.YesNoCallbackParser;
import polis.util.State;

import java.util.List;
import java.util.Objects;

@Component
public class YesNoCallbackHandler extends ATypedCallbackHandler<YesNoCallback> {
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private UserChannelsRepository userChannelsRepository;
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    public YesNoCallbackHandler(YesNoCallbackParser callbackParser) {
        this.callbackParser = callbackParser;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.YES_NO_ANSWER;
    }

    @Override
    public void handleCallback(long userChatId, Message message, YesNoCallback callback, Context context) throws TelegramApiException {
        if (callback.yes()) {
            ChannelGroup currentGroup = context.currentGroup();
            boolean isFound = false;
            for (Account authData : accountsRepository.getAccountsForUser(userChatId)) {
                if (Objects.equals(authData.getAccessToken(), context.currentAccount().getAccessToken())) {
                    List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(userChatId);
                    for (UserChannels tgChannel : tgChannels) {
                        if (Objects.equals(tgChannel.getChannelId(), context.currentChannel().getChannelId())) {
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
            processNextCommand(State.SyncGroupDescription, message, null);
        } else {
            context.setCurrentGroup(null);
            deleteLastMessage(message);
            processNextCommand(State.OkAccountDescription, message, null);
        }
    }
}

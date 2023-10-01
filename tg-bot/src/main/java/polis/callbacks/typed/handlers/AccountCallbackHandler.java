package polis.callbacks.typed.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.context.Context;
import polis.data.domain.Account;
import polis.data.domain.UserChannels;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.callbacks.typed.CallbackType;
import polis.callbacks.typed.objects.AccountCallback;
import polis.callbacks.typed.parsers.AccountCallbackParser;
import polis.util.SocialMedia;
import polis.util.State;

import java.util.List;
import java.util.Objects;

@Component
public class AccountCallbackHandler extends ATypedCallbackHandler<AccountCallback> {
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private UserChannelsRepository userChannelsRepository;
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    public AccountCallbackHandler(AccountCallbackParser callbackParser) {
        this.callbackParser = callbackParser;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.ACCOUNT_CHOSEN;
    }

    @Override
    public void handleCallback(long userChatId, Message message, AccountCallback callback, Context context) throws TelegramApiException {
        String socialMediaName = callback.socialMedia;
        State state = callback.isClickedForDeletion ? State.AddGroup :
                (socialMediaName.equals(SocialMedia.OK.getName()) ? State.OkAccountDescription
                        : State.VkAccountDescription);
        for (Account account : accountsRepository.getAccountsForUser(userChatId)) {
            if (Objects.equals(account.getAccountId(), callback.accountId)) {
                if (!callback.isClickedForDeletion) {
                    context.setCurrentAccount(new Account(
                            userChatId,
                            account.getSocialMedia().getName(),
                            account.getAccountId(),
                            account.getUserFullName(),
                            account.getAccessToken(),
                            account.getRefreshToken()
                    ));
                    break;
                }
                context.setCurrentGroup(null);
                context.setCurrentAccount(null);
                List<UserChannels> userChannels = userChannelsRepository.getUserChannels(userChatId);
                for (UserChannels userChannel : userChannels) {
                    channelGroupsRepository.deleteChannelGroup(userChannel.getChannelId(),
                            account.getSocialMedia().getName());
                }
                accountsRepository.deleteAccount(userChatId, account.getAccountId(), account.getSocialMedia().getName());
                break;
            }
        }
        deleteLastMessage(message);
        processNextCommand(state, sender, message, null);
    }
}

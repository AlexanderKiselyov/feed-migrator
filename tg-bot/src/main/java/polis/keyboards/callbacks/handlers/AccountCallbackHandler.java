package polis.keyboards.callbacks.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.data.domain.Account;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentState;
import polis.data.domain.UserChannels;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.data.repositories.CurrentStateRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.AccountCallback;
import polis.keyboards.callbacks.parsers.AccountCallbackParser;
import polis.util.SocialMedia;
import polis.util.State;

import java.util.List;
import java.util.Objects;

@Component
public class AccountCallbackHandler extends ACallbackHandler<AccountCallback> {
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private CurrentAccountRepository currentAccountRepository;
    @Autowired
    private CurrentGroupRepository currentGroupRepository;
    @Autowired
    private UserChannelsRepository userChannelsRepository;
    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;
    @Autowired
    private CurrentStateRepository currentStateRepository;

    public AccountCallbackHandler(AccountCallbackParser callbackParser) {
        this.callbackParser = callbackParser;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.ACCOUNT_CHOSEN;
    }

    @Override
    protected CallbackParser<AccountCallback> callbackParser() {
        return callbackParser;
    }

    @Override
    public void handleCallback(long userChatId, Message message, AccountCallback callback) throws TelegramApiException {
        String socialMediaName = callback.socialMedia;
        State state = callback.isClickedForDeletion ? State.AddGroup :
                (socialMediaName.equals(SocialMedia.OK.getName()) ? State.OkAccountDescription
                        : State.VkAccountDescription);
        for (Account account : accountsRepository.getAccountsForUser(userChatId)) {
            if (Objects.equals(account.getAccountId(), callback.accountId)) {
                if (!callback.isClickedForDeletion) {
                    currentAccountRepository.insertCurrentAccount(
                            new CurrentAccount(
                                    userChatId,
                                    account.getSocialMedia().getName(),
                                    account.getAccountId(),
                                    account.getUserFullName(),
                                    account.getAccessToken(),
                                    account.getRefreshToken()
                            )
                    );
                    break;
                }
                currentGroupRepository.deleteCurrentGroup(userChatId);
                currentAccountRepository.deleteCurrentAccount(userChatId);
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
        getRegisteredCommand(state.getIdentifier()).processMessage(sender, message, null);
        currentStateRepository.insertCurrentState(new CurrentState(
                userChatId,
                state.getIdentifier()
        ));

    }
}

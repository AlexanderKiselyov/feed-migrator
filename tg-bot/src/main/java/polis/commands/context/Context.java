package polis.commands.context;

import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.util.IState;

public interface Context {

    long getCurrentUserChatId();

    IState currentState();

    CurrentChannel currentChannel();

    Account currentAccount();

    ChannelGroup currentGroup();

    void setCurrentState(IState state);

    void setCurrentChannel(CurrentChannel channel);

    void setCurrentAccount(Account account);

    void setCurrentGroup(ChannelGroup group);
}

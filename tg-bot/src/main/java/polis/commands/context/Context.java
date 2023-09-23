package polis.commands.context;

import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.util.IState;

public interface Context {
    IState currentState();

    void resetCurrentState(IState state);

    CurrentChannel currentChannel();

    void resetCurrentChannel(CurrentChannel channel);

    Account currentAccount();

    void resetCurrentAccount(Account account);

    ChannelGroup currentGroup();

    void resetCurrentGroup(ChannelGroup group);
}

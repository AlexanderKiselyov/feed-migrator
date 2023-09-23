package polis.commands;

import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentState;

public interface Context {
    CurrentState currentState();

    void resetCurrentState(CurrentState state);

    CurrentChannel currentChannel();

    void resetCurrentChannel(CurrentChannel channel);

    Account currentAccount();

    void resetCurrentAccount(Account account);

    ChannelGroup currentGroup();

    void resetCurrentGroup(ChannelGroup group);
}

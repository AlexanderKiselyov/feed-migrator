package polis.commands.context;

import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;

public class ContextImpl implements Context {
    private CurrentChannel currentChannel;
    private Account currentAccount;
    private ChannelGroup currentGroup;

    @Override
    public CurrentChannel currentChannel() {
        return currentChannel;
    }

    @Override
    public void resetCurrentChannel(CurrentChannel channel) {
        this.currentChannel = channel;
    }

    @Override
    public Account currentAccount() {
        return currentAccount;
    }

    @Override
    public void resetCurrentAccount(Account account) {
        this.currentAccount = account;
    }

    @Override
    public ChannelGroup currentGroup() {
        return currentGroup;
    }

    @Override
    public void resetCurrentGroup(ChannelGroup group) {
        this.currentGroup = group;
    }
}

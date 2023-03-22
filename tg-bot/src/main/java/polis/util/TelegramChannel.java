package polis.util;

import java.util.List;

public class TelegramChannel {
    private final String telegramChannelId;
    private final List<SocialMediaGroup> groups;

    public TelegramChannel(String telegramChannelId, List<SocialMediaGroup> groups) {
        this.groups = groups;
        this.telegramChannelId = telegramChannelId;
    }

    public String getTelegramChannelId() {
        return telegramChannelId;
    }

    public List<SocialMediaGroup> getGroups() {
        return groups;
    }

    public void addGroup(SocialMediaGroup newGroup) {
        groups.add(newGroup);
    }

    public void deleteGroup(SocialMediaGroup deleteGroup) {
        groups.remove(deleteGroup);
    }
}

package polis.util;

import java.util.ArrayList;
import java.util.List;

public class TelegramChannel {
    private final Long telegramChannelId;
    private List<SocialMediaGroup> synchronizedGroups;

    public TelegramChannel(Long telegramChannelId, List<SocialMediaGroup> synchronizedGroups) {
        this.synchronizedGroups = synchronizedGroups;
        this.telegramChannelId = telegramChannelId;
    }

    public Long getTelegramChannelId() {
        return telegramChannelId;
    }

    public List<SocialMediaGroup> getSynchronizedGroups() {
        return synchronizedGroups;
    }

    public void addGroup(SocialMediaGroup newGroup) {
        if (synchronizedGroups == null) {
            synchronizedGroups = new ArrayList<>();
        }
        synchronizedGroups.add(newGroup);
    }

    public void deleteGroup(SocialMediaGroup deleteGroup) {
        synchronizedGroups.remove(deleteGroup);
    }
}

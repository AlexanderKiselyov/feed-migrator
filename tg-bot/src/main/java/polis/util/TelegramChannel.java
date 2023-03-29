package polis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TelegramChannel {
    private final Long telegramChannelId;
    private final String telegramChannelUsername;
    private List<SocialMediaGroup> synchronizedGroups;

    public TelegramChannel(Long telegramChannelId, String telegramChannelUsername,
                           List<SocialMediaGroup> synchronizedGroups) {
        this.synchronizedGroups = synchronizedGroups;
        this.telegramChannelUsername = telegramChannelUsername;
        this.telegramChannelId = telegramChannelId;
    }

    public Long getTelegramChannelId() {
        return telegramChannelId;
    }

    public String getTelegramChannelUsername() {
        return telegramChannelUsername;
    }

    public List<SocialMediaGroup> getSynchronizedGroups() {
        return synchronizedGroups;
    }

    public boolean addGroup(SocialMediaGroup newGroup) {
        if (synchronizedGroups == null) {
            synchronizedGroups = new ArrayList<>(1);
        }
        for (SocialMediaGroup group : synchronizedGroups) {
            if (group.getSocialMedia() == newGroup.getSocialMedia()) {
                return false;
            }
        }
        synchronizedGroups.add(newGroup);
        return true;
    }

    public void deleteGroup(Long id) {
        for (SocialMediaGroup smg : synchronizedGroups) {
            if (Objects.equals(smg.getId(), id)) {
                synchronizedGroups.remove(smg);
                break;
            }
        }
    }
}

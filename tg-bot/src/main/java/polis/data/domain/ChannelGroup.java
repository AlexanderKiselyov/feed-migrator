package polis.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import polis.util.SocialMedia;

import javax.annotation.Nullable;

@Table("channel_groups")
public class ChannelGroup {
    @PrimaryKeyColumn(name = "channel_id", type = PrimaryKeyType.PARTITIONED)
    private long channelId;

    @PrimaryKeyColumn(name = "social_media", type = PrimaryKeyType.CLUSTERED)
    private final String socialMedia;

    @PrimaryKeyColumn(name = "group_id", type = PrimaryKeyType.CLUSTERED)
    private final long groupId;

    @Column("group_name")
    private final String groupName;

    @Column("account_id")
    private final long accountId;

    @Column("access_token")
    private final String accessToken;

    @Column("chat_id")
    private final long chatId;

    @Column("channel_username")
    private String channelUsername;

    public ChannelGroup(String accessToken, String groupName, long accountId, long chatId, long groupId,
                        String socialMedia) {
        this.groupName = groupName;
        this.accountId = accountId;
        this.accessToken = accessToken;
        this.chatId = chatId;
        this.groupId = groupId;
        this.socialMedia = socialMedia;
    }

    public ChannelGroup setChannelId(long channelId) {
        this.channelId = channelId;
        return this;
    }

    public ChannelGroup setChannelUsername(String channelUsername) {
        this.channelUsername = channelUsername;
        return this;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    public SocialMedia getSocialMedia() {
        return SocialMedia.findSocialMediaByName(socialMedia);
    }

    public long getGroupId() {
        return groupId;
    }

    @Nullable
    public String getGroupName() {
        return groupName;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getChatId() {
        return chatId;
    }

    @Override
    public String toString() {
        return "ChannelGroup{"
                + "channelId=" + channelId
                + ", socialMedia='" + socialMedia + '\''
                + ", groupId=" + groupId
                + ", groupName='" + groupName + '\''
                + ", accountId=" + accountId
                + ", accessToken='" + accessToken + '\''
                + ", chatId=" + chatId
                + ", channelUsername='" + channelUsername + '\''
                + '}';
    }
}

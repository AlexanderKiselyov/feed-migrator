package polis.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import javax.annotation.Nullable;

@Table("channel_groups")
public class Group {
    @PrimaryKeyColumn(name = "channel_id", type = PrimaryKeyType.PARTITIONED)
    private long channelId;

    @PrimaryKeyColumn(name = "social_media", type = PrimaryKeyType.CLUSTERED)
    private String socialMedia;

    @PrimaryKeyColumn(name = "group_id", type = PrimaryKeyType.CLUSTERED)
    private long groupId;

    @Column("group_name")
    private String groupName;

    @Column("account_id")
    private long accountId;

    @Column("access_token")
    private String accessToken;

    @Column("chat_id")
    private long chatId;

    public Group(String accessToken, String groupName, long accountId, long chatId) {
        this.groupName = groupName;
        this.accountId = accountId;
        this.accessToken = accessToken;
        this.chatId = chatId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public void setSocialMedia(String socialMedia) {
        this.socialMedia = socialMedia;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getSocialMedia() {
        return socialMedia;
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
        return "Group{" +
                "channelId=" + channelId +
                ", socialMedia='" + socialMedia + '\'' +
                ", groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", accountId=" + accountId +
                ", accessToken='" + accessToken + '\'' +
                ", chatId=" + chatId +
                '}';
    }
}

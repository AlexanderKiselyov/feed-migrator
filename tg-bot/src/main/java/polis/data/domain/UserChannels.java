package polis.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("user_channels")
public class UserChannels {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "channel_id")
    private final long channelId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "chat_id")
    private final long chatId;

    @Column(value = "channel_username")
    private final String channelUsername;

    @Column(value = "is_autoposting")
    private boolean isAutoposting;

    @Column(value = "is_notification")
    private boolean isNotification;

    public UserChannels(long chatId, long channelId, String channelUsername) {
        this.chatId = chatId;
        this.channelId = channelId;
        this.channelUsername = channelUsername;
    }

    public void setAutoposting(boolean isAutoposting) {
        this.isAutoposting = isAutoposting;
    }

    public void setNotification(boolean isNotification) {
        this.isNotification = isNotification;
    }

    public long getChatId() {
        return chatId;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    public boolean isAutoposting() {
        return isAutoposting;
    }

    public boolean isNotification() {
        return isNotification;
    }

    @Override
    public String toString() {
        return "UserChannels{"
                + "channelId=" + channelId
                + ", chatId=" + chatId
                + ", channelUsername='" + channelUsername + '\''
                + ", isAutoposting=" + isAutoposting
                + ", isNotification=" + isNotification
                + "}";
    }
}

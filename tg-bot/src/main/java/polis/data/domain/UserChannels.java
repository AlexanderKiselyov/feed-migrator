package polis.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("user_channels")
public class UserChannels {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "chat_id")
    private final Long chatId;

    @Column(value = "channel_id")
    private final long channelId;

    @Column(value = "channel_username")
    private final String channelUsername;

    public UserChannels(long chatId, long channelId, String channelUsername) {
        this.chatId = chatId;
        this.channelId = channelId;
        this.channelUsername = channelUsername;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    @Override
    public String toString() {
        return "UserChannels{" +
                "chatId=" + chatId +
                ", channelId=" + channelId +
                ", channelUsername='" + channelUsername + '\'' +
                "}";
    }
}

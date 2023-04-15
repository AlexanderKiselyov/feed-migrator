package polis.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("current_channel")
public class CurrentChannel {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "chat_id")
    private final long chatId;

    @Column(value = "channel_id")
    private final long channelId;

    @Column(value = "channel_username")
    private final String channelUsername;

    public CurrentChannel(long chatId, long channelId, String channelUsername) {
        this.chatId = chatId;
        this.channelId = channelId;
        this.channelUsername = channelUsername;
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

    @Override
    public String toString() {
        return "CurrentChannel{"
                + "chatId=" + chatId
                + ", channelId=" + channelId
                + ", channelUsername='" + channelUsername + '\''
                + "}";
    }
}

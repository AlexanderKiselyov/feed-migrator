package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.UserChannels;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;
import static org.springframework.data.cassandra.core.query.Update.update;

@Repository
public class UserChannelsRepository {
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHAT_ID = "chat_id";
    private static final String IS_AUTOPOSTING = "is_autoposting";
    private static final String IS_NOTIFICATION = "is_notification";

    @Autowired
    private CassandraOperations cassandraOperations;

    public List<UserChannels> getUserChannels(long chatId) throws DataAccessException {
        return cassandraOperations.select(
                query(
                        where(CHAT_ID).is(chatId)
                ).withAllowFiltering(),
                UserChannels.class
        );
    }

    public long getUserChatId(long channelId) throws DataAccessException {
        return Objects.requireNonNull(cassandraOperations.selectOne(
                query(
                        where(CHANNEL_ID).is(channelId)
                ),
                UserChannels.class
        )).getChatId();
    }

    public void insertUserChannel(@NotNull UserChannels userChannels) throws DataAccessException {
        cassandraOperations.insert(userChannels);
    }

    public void deleteUserChannel(@NotNull UserChannels userChannels) throws DataAccessException {
        cassandraOperations.delete(userChannels);
    }

    public void setAutoposting(long chatId, long channelId, boolean isAutoposting) throws DataAccessException {
        cassandraOperations.update(
                query(
                        where(CHAT_ID).is(chatId))
                        .and(where(CHANNEL_ID).is(channelId)),
                update(IS_AUTOPOSTING, isAutoposting),
                UserChannels.class
        );
    }

    public boolean isSetAutoposting(long chatId, long channelId) throws DataAccessException {
        return Objects.requireNonNull(cassandraOperations.selectOne(
                query(
                        where(CHAT_ID).is(chatId))
                        .and(where(CHANNEL_ID).is(channelId)),
                UserChannels.class
        )).isAutoposting();
    }

    public void setNotification(long chatId, long channelId, boolean isNotification) throws DataAccessException {
        cassandraOperations.update(
                query(
                        where(CHAT_ID).is(chatId))
                        .and(where(CHANNEL_ID).is(channelId)),
                update(IS_NOTIFICATION, isNotification),
                UserChannels.class
        );
    }

    public boolean isSetNotification(long chatId, long channelId) throws DataAccessException {
        return Objects.requireNonNull(cassandraOperations.selectOne(
                query(
                        where(CHAT_ID).is(chatId))
                        .and(where(CHANNEL_ID).is(channelId)),
                UserChannels.class
        )).isNotification();
    }
}

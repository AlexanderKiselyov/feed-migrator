package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.CurrentChannel;

import javax.validation.constraints.NotNull;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class CurrentChannelRepository {
    private static final String CHAT_ID = "chat_id";

    @Autowired
    private CassandraOperations cassandraOperations;

    public CurrentChannel getCurrentChannel(long chatId) throws DataAccessException {
        return cassandraOperations.selectOne(
                query(
                        where(CHAT_ID).is(chatId)
                ),
                CurrentChannel.class
        );
    }

    public void insertCurrentChannel(@NotNull CurrentChannel currentChannel) throws DataAccessException {
        cassandraOperations.update(currentChannel);
    }

    public void deleteCurrentChannel(long chatId) throws DataAccessException {
        cassandraOperations.delete(
                query(
                        where(CHAT_ID).is(chatId)
                ),
                CurrentChannel.class
        );
    }
}

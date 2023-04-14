package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.CurrentChannel;

import javax.validation.constraints.NotNull;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class CurrentChannelRepository {
    @Autowired
    private CassandraOperations cassandraOperations;

    public CurrentChannel getCurrentChannel(long chatId) {
        return cassandraOperations.selectOne(
                query(
                        where("chat_id").is(chatId)
                ),
                CurrentChannel.class
        );
    }

    public void insertCurrentChannel(@NotNull CurrentChannel currentChannel) {
        cassandraOperations.update(currentChannel);
    }

    public void deleteCurrentChannel(long chatId) {
        cassandraOperations.delete(
                query(
                        where("chat_id").is(chatId)
                ),
                CurrentChannel.class
        );
    }
}

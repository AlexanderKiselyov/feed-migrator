package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.UserChannels;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class UserChannelsRepository {
    @Autowired
    private CassandraOperations cassandraOperations;

    public List<UserChannels> getUserChannels(long chatId) {
        return cassandraOperations.select(
                query(
                    where("chat_id").is(chatId)
                ),
                UserChannels.class
        );
    }

    public void insertUserChannel(@NotNull UserChannels userChannels) {
        cassandraOperations.insert(userChannels);
    }

    public void deleteUserChannel(@NotNull UserChannels userChannels) {
        cassandraOperations.delete(userChannels);
    }
}

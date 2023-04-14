package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.CurrentState;

import javax.validation.constraints.NotNull;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class CurrentStateRepository {
    @Autowired
    private CassandraOperations cassandraOperations;

    public CurrentState getCurrentState(long chatId) {
        return cassandraOperations.selectOne(
                query(
                    where("chat_id").is(chatId)
                ),
                CurrentState.class
        );
    }

    public void insertCurrentState(@NotNull CurrentState currentState) {
        cassandraOperations.update(currentState);
    }
}

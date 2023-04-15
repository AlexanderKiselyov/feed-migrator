package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.CurrentState;

import javax.validation.constraints.NotNull;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class CurrentStateRepository {
    private static final String CHAT_ID = "chat_id";

    @Autowired
    private CassandraOperations cassandraOperations;

    public CurrentState getCurrentState(long chatId) throws DataAccessException {
        return cassandraOperations.selectOne(
                query(
                    where(CHAT_ID).is(chatId)
                ),
                CurrentState.class
        );
    }

    public void insertCurrentState(@NotNull CurrentState currentState) throws DataAccessException {
        cassandraOperations.update(currentState);
    }
}

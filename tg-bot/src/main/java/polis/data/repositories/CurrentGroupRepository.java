package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.CurrentGroup;

import javax.validation.constraints.NotNull;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class CurrentGroupRepository {
    private static final String CHAT_ID = "chat_id";

    @Autowired
    private CassandraOperations cassandraOperations;

    public CurrentGroup getCurrentGroup(long chatId) throws DataAccessException {
        return cassandraOperations.selectOne(
                query(
                    where(CHAT_ID).is(chatId)
                ),
                CurrentGroup.class
        );
    }

    public void insertCurrentGroup(@NotNull CurrentGroup currentGroup) throws DataAccessException {
        deleteCurrentGroup(currentGroup.getChatId());
        cassandraOperations.update(currentGroup);
    }

    public void deleteCurrentGroup(long chatId) throws DataAccessException {
        cassandraOperations.delete(
                query(
                    where(CHAT_ID).is(chatId)
                ),
                CurrentGroup.class
        );
    }
}

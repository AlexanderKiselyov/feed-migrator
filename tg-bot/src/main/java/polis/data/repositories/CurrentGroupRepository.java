package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.CurrentGroup;

import javax.validation.constraints.NotNull;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class CurrentGroupRepository {
    @Autowired
    private CassandraOperations cassandraOperations;

    public CurrentGroup getCurrentGroup(long chatId) {
        return cassandraOperations.selectOne(
                query(
                    where("chat_id").is(chatId)
                ),
                CurrentGroup.class
        );
    }

    public void insertCurrentGroup(@NotNull CurrentGroup currentGroup) {
        cassandraOperations.update(currentGroup);
    }

    public void deleteCurrentGroup(long chatId) {
        cassandraOperations.delete(
                query(
                    where("chat_id").is(chatId)
                ),
                CurrentGroup.class
        );
    }
}

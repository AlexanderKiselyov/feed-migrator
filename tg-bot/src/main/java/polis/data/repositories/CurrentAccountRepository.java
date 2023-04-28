package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.CurrentAccount;

import javax.validation.constraints.NotNull;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class CurrentAccountRepository {
    private static final String CHAT_ID = "chat_id";

    @Autowired
    private CassandraOperations cassandraOperations;

    public CurrentAccount getCurrentAccount(long chatId) throws DataAccessException {
        return cassandraOperations.selectOne(
                query(
                        where(CHAT_ID).is(chatId)
                ),
                CurrentAccount.class);
    }

    public void insertCurrentAccount(@NotNull CurrentAccount newAccount) throws DataAccessException {
        cassandraOperations.delete(
                query(
                    where(CHAT_ID).is(newAccount.getChatId())
                ),
                CurrentAccount.class
        );
        cassandraOperations.update(newAccount);
    }

    public void deleteCurrentAccount(long chatId) throws DataAccessException {
        cassandraOperations.delete(
                query(
                        where(CHAT_ID).is(chatId)
                ),
                CurrentAccount.class);
    }
}

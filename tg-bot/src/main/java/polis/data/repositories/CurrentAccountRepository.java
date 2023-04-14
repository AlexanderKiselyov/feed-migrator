package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.Account;
import polis.data.domain.CurrentAccount;

import javax.validation.constraints.NotNull;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class CurrentAccountRepository {
    @Autowired
    private CassandraOperations cassandraOperations;

    public CurrentAccount getCurrentAccount(long chatId) {
        return cassandraOperations.selectOne(
                query(
                        where("chat_id").is(chatId)
                ),
                CurrentAccount.class);
    }

    public void insertCurrentAccount(@NotNull Account newAccount) {
        cassandraOperations.update(newAccount);
    }
}

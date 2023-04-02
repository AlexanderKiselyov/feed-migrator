package polis.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.domain.Account;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class AccountsRepository {

    @Autowired
    private CassandraOperations cassandraOperations;

    public Account getAccount(long chatId, String socialNetwork, byte accountId) {
        return cassandraOperations.selectOne(
                query(
                        where("chat_id").is(chatId))
                        .and(where("social_network").is(socialNetwork))
                        .and(where("account_id").is(accountId)),
                Account.class);
    }
}

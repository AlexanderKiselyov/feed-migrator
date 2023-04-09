package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.Account;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class AccountsRepository {

    @Autowired
    private CassandraOperations cassandraOperations;

    public Account getAccount(long chatId, String socialMedia, long accountId) {
        return cassandraOperations.selectOne(
                query(
                        where("chat_id").is(chatId))
                        .and(where("social_media").is(socialMedia))
                        .and(where("account_id").is(accountId)),
                Account.class);
    }
}

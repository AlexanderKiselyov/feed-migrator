package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.Account;

import javax.validation.constraints.NotNull;

import java.util.List;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class AccountsRepository {
    private static final String CHAT_ID = "chat_id";
    private static final String SOCIAL_MEDIA = "social_media";
    private static final String ACCOUNT_ID = "account_id";

    @Autowired
    private CassandraOperations cassandraOperations;

    public void insertNewAccount(@NotNull Account account) throws DataAccessException {
        cassandraOperations.update(account);
    }

    public List<Account> getAccountsForUser(long chatId) throws DataAccessException {
        return cassandraOperations.select(
                query(
                        where(CHAT_ID).is(chatId)),
                Account.class);
    }

    public Account getAccountByKey(long chatId, String socialMedia, long accountId) {
        return cassandraOperations.selectOne(
                query(where(CHAT_ID).is(chatId))
                        .and(where(SOCIAL_MEDIA).is(socialMedia))
                        .and(where(ACCOUNT_ID).is(accountId)),
                Account.class
        );
    }

    public void deleteAccount(long chatId, long accountId, String socialMedia) throws DataAccessException {
        cassandraOperations.delete(
                query(
                        where(CHAT_ID).is(chatId))
                        .and(where(SOCIAL_MEDIA).is(socialMedia))
                        .and(where(ACCOUNT_ID).is(accountId)),
                Account.class);
    }
}

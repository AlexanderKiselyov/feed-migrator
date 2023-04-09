package polis.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("accounts")
public class Account {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "chat_id")
    private long chatId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "social_media")
    private String socialMedia;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "account_id")
    private long accountId;

    @Column(value = "refresh_token")
    private String refreshToken;

    @Override
    public String toString() {
        return "Account{" +
                "chatId=" + chatId +
                ", socialNetwork='" + socialMedia + '\'' +
                ", accountId=" + accountId +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}

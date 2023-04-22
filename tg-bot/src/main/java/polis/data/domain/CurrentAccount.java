package polis.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("current_account")
public class CurrentAccount {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "chat_id")
    private final long chatId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "social_media")
    private final String socialMedia;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "account_id")
    private final long accountId;

    @Column(value = "user_full_name")
    private final String userFullName;

    @Column(value = "access_token")
    private final String accessToken;

    @Column(value = "refresh_token")
    private final String refreshToken;

    public CurrentAccount(long chatId, String socialMedia, long accountId, String userFullName, String accessToken,
                   String refreshToken) {
        this.chatId = chatId;
        this.socialMedia = socialMedia;
        this.accountId = accountId;
        this.userFullName = userFullName;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public long getChatId() {
        return chatId;
    }

    public String getSocialMedia() {
        return socialMedia;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String toString() {
        return "CurrentAccount{"
                + "chatId=" + chatId
                + ", socialMedia='" + socialMedia + '\''
                + ", accountId=" + accountId
                + ", userFullName='" + userFullName + '\''
                + ", accessToken='" + accessToken + '\''
                + ", refreshToken='" + refreshToken + '\''
                + '}';
    }
}

package polis.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import polis.util.SocialMedia;

@Table("accounts")
public class Account {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "chat_id")
    private final long chatId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "social_media")
    private final String socialMedia;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "account_id")
    private final long accountId;

    @Column(value = "user_full_name")
    private final String userFullName;

    @Column(value = "access_token")
    private String accessToken;

    @Column(value = "refresh_token")
    private String refreshToken;

    public Account(long chatId, String socialMedia, long accountId, String userFullName, String accessToken,
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

    public SocialMedia getSocialMedia() {
        return SocialMedia.findSocialMediaByName(socialMedia);
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

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "Account{"
                + "chatId=" + chatId
                + ", socialMedia='" + socialMedia + '\''
                + ", accountId=" + accountId
                + ", userFullName='" + userFullName + '\''
                + ", accessToken='" + accessToken + '\''
                + ", refreshToken='" + refreshToken + '\''
                + '}';
    }
}

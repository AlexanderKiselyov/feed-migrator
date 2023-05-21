package polis.data.domain;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import polis.util.SocialMedia;

@Table("current_group")
public class CurrentGroup {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "chat_id")
    private final long chatId;

    @Column("social_media")
    private final String socialMedia;

    @Column("group_id")
    private final long groupId;

    @Column("group_name")
    private final String groupName;

    @Column("account_id")
    private final long accountId;

    @Column("access_token")
    private final String accessToken;

    public CurrentGroup(long chatId, String socialMedia, long groupId, String groupName, long accountId,
                        String accessToken) {
        this.chatId = chatId;
        this.socialMedia = socialMedia;
        this.groupId = groupId;
        this.groupName = groupName;
        this.accountId = accountId;
        this.accessToken = accessToken;
    }

    public long getChatId() {
        return chatId;
    }

    public SocialMedia getSocialMedia() {
        return SocialMedia.findSocialMediaByName(socialMedia);
    }

    public long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return "CurrentGroup{"
                + "chatId=" + chatId
                + ", socialMedia='" + socialMedia + '\''
                + ", groupId=" + groupId
                + ", groupName='" + groupName + '\''
                + ", accountId=" + accountId
                + ", accessToken='" + accessToken + '\''
                + '}';
    }
}

package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.Group;

import javax.annotation.Nullable;
import java.util.List;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository("ChannelGroupsRepositoryImpl")
public class ChannelGroupsRepositoryImpl implements ChannelGroupsRepository {
    public static final String CHANNEL_ID = "channel_id";
    public static final String GROUP_ID = "group_id";
    public static final String SOCIAL_MEDIA = "social_media";

    @Autowired
    CassandraOperations cassandraOperations;

    @Override
    public List<Group> getGroupsForChannel(long channelId) throws DataAccessException {
        return cassandraOperations.select(query(where(CHANNEL_ID).is(channelId)), Group.class);
    }

    @Override
    public Group getGroupByKey(long channelId, String socialMedia, long groupId) throws DataAccessException {
        return cassandraOperations.selectOne(query(
                        where(CHANNEL_ID).is(channelId))
                        .and(where(SOCIAL_MEDIA).is(socialMedia))
                        .and(where(GROUP_ID).is(groupId)),
                Group.class);
    }

    @Override
    public void upsertGroupByKey(long channelId, String socialMedia, long groupId,
                                 @Nullable Group group) throws DataAccessException {
        if (group == null) {
            cassandraOperations.delete(query(
                    where(CHANNEL_ID).is(channelId))
                    .and(where(SOCIAL_MEDIA).is(socialMedia))
                    .and(where(GROUP_ID).is(groupId)),
                    Group.class
            );
            return;
        }
        group.setChannelId(channelId);
        group.setSocialMedia(socialMedia);
        group.setGroupId(groupId);
        cassandraOperations.update(group);
    }
}

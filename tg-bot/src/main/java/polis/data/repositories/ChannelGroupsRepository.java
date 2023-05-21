package polis.data.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;
import polis.data.domain.ChannelGroup;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

@Repository
public class ChannelGroupsRepository {
    private static final String CHANNEL_ID = "channel_id";
    private static final String GROUP_ID = "group_id";
    private static final String SOCIAL_MEDIA = "social_media";

    @Autowired
    private CassandraOperations cassandraOperations;

    public List<ChannelGroup> getGroupsForChannel(long channelId) throws DataAccessException {
        return cassandraOperations.select(
                query(
                        where(CHANNEL_ID).is(channelId)
                ),
                ChannelGroup.class
        );
    }

    public void insertChannelGroup(@NotNull ChannelGroup channelGroup) throws DataAccessException {
        cassandraOperations.update(channelGroup);
    }

    public void deleteChannelGroup(long channelId, String socialMedia, long groupId) throws DataAccessException {
        cassandraOperations.delete(
                query(
                        where(CHANNEL_ID).is(channelId))
                        .and(where(SOCIAL_MEDIA).is(socialMedia))
                        .and(where(GROUP_ID).is(groupId)),
                ChannelGroup.class
        );
    }

    public void deleteChannelGroup(long channelId, String socialMedia) throws DataAccessException {
        cassandraOperations.delete(
                query(
                        where(CHANNEL_ID).is(channelId))
                        .and(where(SOCIAL_MEDIA).is(socialMedia)),
                ChannelGroup.class
        );
    }
}

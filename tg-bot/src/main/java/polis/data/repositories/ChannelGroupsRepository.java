package polis.data.repositories;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import polis.data.domain.ChannelGroup;

import javax.annotation.Nullable;
import java.util.List;

@Repository
public interface ChannelGroupsRepository {

    List<ChannelGroup> getGroupsForChannel(long channelId) throws DataAccessException;

    ChannelGroup getGroupByKey(long channelId, String socialMedia, long groupId) throws DataAccessException;

    void upsertGroupByKey(long channelId, String socialMedia, long groupId, @Nullable ChannelGroup channelGroup) throws DataAccessException;

}

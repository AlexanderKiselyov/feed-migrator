package polis.data.repositories;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import polis.data.domain.Group;

import javax.annotation.Nullable;
import java.util.List;

@Repository
public interface ChannelGroupsRepository {

    List<Group> getGroupsForChannel(long channelId, boolean loadGroupNames) throws DataAccessException;

    Group getGroup(long channelId, String socialMedia, long groupId, boolean loadGroupNames) throws DataAccessException;

    void upsertGroup(long channelId, String socialMedia, long groupId, @Nullable Group group) throws DataAccessException;

}

package polis.vk.api;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQueryWithObjectLegacy;
import com.vk.api.sdk.queries.users.UsersGetQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.vk.api.exceptions.VkApiException;

import static polis.vk.api.LoggingUtils.getGroupId;
import static polis.vk.api.LoggingUtils.getGroupName;
import static polis.vk.api.LoggingUtils.getIsGroupAdmin;
import static polis.vk.api.LoggingUtils.getUsername;

public class VkApiMethods {
    TransportClient transportClient = HttpTransportClient.getInstance();
    VkApiClient vk = new VkApiClient(transportClient);
    private static final Logger logger = LoggerFactory.getLogger(VkApiMethods.class);

    public Boolean getIsVkGroupAdmin(VkAuthorizator.TokenWithId tokenWithId, String groupLink) throws VkApiException {
        String[] groupLinkParts = groupLink.split("/");

        GroupsGetByIdQueryWithObjectLegacy request = vk.groups()
                .getByIdObjectLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(groupLinkParts[groupLinkParts.length - 1])
                .fields();

        return getIsGroupAdmin(request, logger);
    }

    public String getVkUsername(VkAuthorizator.TokenWithId tokenWithId) throws VkApiException {
        UsersGetQuery request = vk.users()
                .get(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .fields();

        return getUsername(request, logger);
    }

    public Integer getVkGroupId(VkAuthorizator.TokenWithId tokenWithId, String groupLink) throws VkApiException {
        String[] groupLinkParts = groupLink.split("/");

        GroupsGetByIdQueryWithObjectLegacy request = vk.groups()
                .getByIdObjectLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(groupLinkParts[groupLinkParts.length - 1])
                .fields();

        return getGroupId(request, logger);
    }

    public String getVkGroupName(VkAuthorizator.TokenWithId tokenWithId, Long groupId) throws VkApiException {
        GroupsGetByIdQueryWithObjectLegacy request = vk.groups()
                .getByIdObjectLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(String.valueOf(groupId))
                .fields();

        return getGroupName(request, logger);
    }
}

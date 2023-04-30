package polis.vk.api;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQueryWithLegacy;
import com.vk.api.sdk.queries.users.UsersGetQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.vk.api.exceptions.VkApiException;

import static polis.vk.api.LoggingUtils.*;

public class VKApiMethods {
    TransportClient transportClient = HttpTransportClient.getInstance();
    VkApiClient vk = new VkApiClient(transportClient);
    private static final Logger logger = LoggerFactory.getLogger(VKApiMethods.class);

    public String getVkUsername(VkAuthorizator.TokenWithId tokenWithId) throws VkApiException {
        UsersGetQuery request = vk.users()
                .get(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .fields();

        return getUsername(request, logger);
    }

    public Integer getVkGroupId(VkAuthorizator.TokenWithId tokenWithId, String groupLink) throws VkApiException {
        String[] groupLinkParts = groupLink.split("/");

        GroupsGetByIdQueryWithLegacy request = vk.groups()
                .getByIdLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(groupLinkParts[groupLinkParts.length - 1])
                .fields();

        return getGroupId(request, logger);
    }

    public String getVkGroupName(VkAuthorizator.TokenWithId tokenWithId, String groupLink) throws VkApiException {
        String[] groupLinkParts = groupLink.split("/");

        GroupsGetByIdQueryWithLegacy request = vk.groups()
                .getByIdLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(groupLinkParts[groupLinkParts.length - 1])
                .fields();

        return getGroupName(request, logger);
    }

    public Boolean getIsVkGroupAdmin(VkAuthorizator.TokenWithId tokenWithId, String groupLink) throws VkApiException {
        String[] groupLinkParts = groupLink.split("/");

        GroupsGetByIdQueryWithLegacy request = vk.groups()
                .getByIdLegacy(new UserActor(tokenWithId.userId(), tokenWithId.accessToken()))
                .groupIds(groupLinkParts[groupLinkParts.length - 1])
                .fields();

        return getIsGroupAdmin(request, logger);
    }
}

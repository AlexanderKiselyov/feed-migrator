package polis.vk.api;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.oauth.OAuthUserAuthorizationCodeFlowQuery;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.vk.api.exceptions.VkApiException;

import java.net.URI;
import java.net.URISyntaxException;

import static polis.vk.api.LoggingUtils.getAccessToken;

public class VkAuthorizator {
    private static final String AUTH_URL = "https://oauth.vk.com/authorize";
    private static final String APP_SCOPE = "270336"; // groups + wall
    private static final Logger logger = LoggerFactory.getLogger(VkAuthorizator.class);
    TransportClient transportClient = HttpTransportClient.getInstance();
    VkApiClient vk = new VkApiClient(transportClient);

    public TokenWithId getToken(String code) throws VkApiException {
        OAuthUserAuthorizationCodeFlowQuery request = vk.oAuth()
                .userAuthorizationCodeFlow(
                        Integer.parseInt(VkAppProperties.APPLICATION_ID),
                        VkAppProperties.APPLICATION_SECRET_KEY,
                        VkAppProperties.REDIRECT_URI,
                        code
                );
        return getAccessToken(request, logger);
    }

    public static String formAuthorizationUrl() throws URISyntaxException {
        URI uri = new URIBuilder(AUTH_URL)
                .addParameter("client_id", VkAppProperties.APPLICATION_ID)
                .addParameter("scope", APP_SCOPE)
                .addParameter("response_type", "code")
                .addParameter("redirect_uri", VkAppProperties.REDIRECT_URI)
                .addParameter("display", "page")
                .build();
        return uri.toString();
    }

    public record TokenWithId(String accessToken, Integer userId) {

    }
}

package polis.vk.api;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class VkAuthorizator {
    private static final String AUTH_URL = "https://oauth.vk.com/authorize";
    private static final String APP_SCOPE = "466964"; // photos + video + wall + docs + groups + offline

    public static String formAuthorizationUrl() throws URISyntaxException {
        URI uri = new URIBuilder(AUTH_URL)
                .addParameter("client_id", VkAppProperties.APPLICATION_ID)
                .addParameter("scope", APP_SCOPE)
                .addParameter("response_type", "token")
                .addParameter("redirect_uri", VkAppProperties.REDIRECT_URI)
                .addParameter("display", "page")
                .build();
        return uri.toString();
    }

    public record TokenWithId(String accessToken, Integer userId) {

    }
}

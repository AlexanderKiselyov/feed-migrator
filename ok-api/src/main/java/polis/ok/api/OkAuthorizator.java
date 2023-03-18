package polis.ok.api;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class OkAuthorizator {
    private static final String AUTH_URI = "https://connect.ok.ru/oauth/authorize";
    private static final String APP_SCOPE = "VALUABLE_ACCESS;LONG_ACCESS_TOKEN;PHOTO_CONTENT;GROUP_CONTENT;VIDEO_CONTENT";
    private static final String GET_TOKEN_URI = "https://api.ok.ru/oauth/token.do";

    private final HttpClient client = HttpClient.newHttpClient();

    public TokenPair getToken(String code, String clientId, String clientSecret, String redirectUri) throws Exception {
        URI uri = new URIBuilder(GET_TOKEN_URI)
                .addParameter("code", code)
                .addParameter("client_id", clientId)
                .addParameter("client_secret", clientSecret)
                .addParameter("redirect_uri", redirectUri)
                .addParameter("grant_type", "authorization_code")
                .build();

        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject responseJson = new JSONObject(response.body());

        if (responseJson.has("error")) {
            return new TokenPair(null, null);
        }
        String accessToken = responseJson.getString("access_token");
        String refreshToken = responseJson.getString("refresh_token");
        return new TokenPair(accessToken, refreshToken);
    }

    public static String formAuthorizationUrl(String clientId, String redirectUri) throws URISyntaxException {
        URI uri = new URIBuilder(AUTH_URI)
                .addParameter("client_id", clientId)
                .addParameter("scope", APP_SCOPE)
                .addParameter("response_type", "code")
                .addParameter("redirect_uri", redirectUri)
                .build();

        return uri.toString();
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }
}

package polis.authorization;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class OkAuthorization {
    private static final String AUTH_URI = "https://connect.ok.ru/oauth/authorize";
    private static final String APP_SCOPE = "VALUABLE_ACCESS;LONG_ACCESS_TOKEN;PHOTO_CONTENT;GROUP_CONTENT";
    private static final String GET_TOKEN_URI = "https://api.ok.ru/oauth/token.do";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static String formAuthorizationUrl(String clientId, String redirectUri) throws URISyntaxException {
        URI uri = new URIBuilder(AUTH_URI)
                .addParameter("client_id", clientId)
                .addParameter("scope", APP_SCOPE)
                .addParameter("response_type", "code")
                .addParameter("redirect_uri", redirectUri)
                .build();

        return uri.toString();
    }

    public static TokenPair getToken(String code, String clientId, String clientSecret, String redirectUri)
            throws URISyntaxException, IOException, InterruptedException {
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

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return new TokenPair(null, null);
        }

        JSONObject object = new JSONObject(response.body());

        if (object.has("error")) {
            return new TokenPair(null, null);
        }

        String accessToken = object.getString("access_token");
        String refreshToken = object.getString("refresh_token");
        return new TokenPair(accessToken, refreshToken);
    }

    public record TokenPair(String accessToken, String refreshToken) {

    }
}

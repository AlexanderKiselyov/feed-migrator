package polis;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OkAuthorization {
    public static final String APPLICATION_ID = "512001770002";
    public static final String APPLICATION_KEY = "CDBDAQKGDIHBABABA";
    private static final String APPLICATION_SECRET_KEY = "040C0F0B005B3B61A346C801";

    private static final String REDIRECT_URI = "https://webhook.site/c66a2e2a-3aa9-4caa-9b09-105e970e316c";

    private final HttpClient client = HttpClient.newHttpClient();

    static String sig(String accessToken, String methodName) {
        String secretKey = DigestUtils.md5Hex(accessToken + APPLICATION_SECRET_KEY);
        String sig = "application_key=" +
                APPLICATION_KEY +
                "format=jsonmethod=" +
                methodName +
                secretKey;

        return DigestUtils.md5Hex(sig);
    }

    public String formAuthorizationUrl() throws URISyntaxException {
        URI uri = new URIBuilder("https://connect.ok.ru/oauth/authorize")
                .addParameter("client_id", APPLICATION_ID)
                .addParameter("scope", "VALUABLE_ACCESS;LONG_ACCESS_TOKEN;PHOTO_CONTENT;GROUP_CONTENT")
                .addParameter("response_type", "code")
                .addParameter("redirect_uri", REDIRECT_URI)
                .build();

        return uri.toString();
    }

    public TokenPair getToken(String code) throws URISyntaxException, IOException, InterruptedException {

        URI uri = new URIBuilder("https://api.ok.ru/oauth/token.do")
                .addParameter("code", code)
                .addParameter("client_id", APPLICATION_ID)
                .addParameter("client_secret", APPLICATION_SECRET_KEY)
                .addParameter("redirect_uri", REDIRECT_URI)
                .addParameter("grant_type", "authorization_code")
                .build();

        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                .uri(uri)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException();
        }

        JSONObject object = new JSONObject(response.body());
        String accessToken = object.getString("access_token");
        String refreshToken = object.getString("refresh_token");
        return new TokenPair(accessToken, refreshToken);
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }
}

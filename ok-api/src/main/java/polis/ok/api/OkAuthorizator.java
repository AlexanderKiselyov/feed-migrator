package polis.ok.api;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static polis.ok.api.LoggingUtils.parseResponse;
import static polis.ok.api.LoggingUtils.sendRequest;
import static polis.ok.api.LoggingUtils.parseResponse;
import static polis.ok.api.LoggingUtils.sendRequest;

public final class OkAuthorizator {
    private static final String AUTH_URL = "https://connect.ok.ru/oauth/authorize";
    private static final String TOKEN_URL = "https://api.ok.ru/oauth/token.do";
    private static final String APP_SCOPE = "VALUABLE_ACCESS;LONG_ACCESS_TOKEN;PHOTO_CONTENT;GROUP_CONTENT;VIDEO_CONTENT";

    private static final Logger logger = LoggerFactory.getLogger(OkAuthorizator.class);
    private final HttpClient client = HttpClient.newHttpClient();

    public TokenPair getToken(String code) throws IOException, URISyntaxException, OkApiException {
        URI uri = new URIBuilder(TOKEN_URL)
                .addParameter("code", code)
                .addParameter("client_id", OkAppProperties.APPLICATION_ID)
                .addParameter("client_secret", OkAppProperties.APPLICATION_SECRET_KEY)
                .addParameter("redirect_uri", OkAppProperties.REDIRECT_URI)
                .addParameter("grant_type", "authorization_code")
                .build();
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                .uri(uri)
                .build();
        HttpResponse<String> response = sendRequest(client, request, logger);
        JSONObject responseJson = parseResponse(response, logger);

        try {
            return new TokenPair(
                    responseJson.getString("access_token"),
                    responseJson.getString("refresh_token")
            );
        } catch (JSONException e) {
            throw LoggingUtils.wrapAndLog(e, response.toString(), response.body(), logger);
        }
    }

    public TokenPair refreshToken(String refreshToken) throws URISyntaxException, IOException, OkApiException {
        URI uri = new URIBuilder(TOKEN_URL)
                .addParameter("refresh_token", refreshToken)
                .addParameter("client_id", OkAppProperties.APPLICATION_ID)
                .addParameter("client_secret", OkAppProperties.APPLICATION_SECRET_KEY)
                .addParameter("redirect_uri", OkAppProperties.REDIRECT_URI)
                .addParameter("grant_type", " refresh_token")
                .build();
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofByteArray(new byte[]{}))
                .uri(uri)
                .build();
        HttpResponse<String> response = sendRequest(client, request, logger);
        JSONObject responseJson = parseResponse(response, logger);

        try {
            return new TokenPair(
                    responseJson.getString("access_token"),
                    refreshToken
            );
        } catch (JSONException e) {
            throw wrapAndLog(e, response.toString(), response.body(), logger);
        }
    }

    public static String formAuthorizationUrl() throws URISyntaxException {
        URI uri = new URIBuilder(AUTH_URL)
                .addParameter("client_id", OkAppProperties.APPLICATION_ID)
                .addParameter("scope", APP_SCOPE)
                .addParameter("response_type", "code")
                .addParameter("redirect_uri", OkAppProperties.REDIRECT_URI)
                .build();
        return uri.toString();
    }

    static String sig(String accessToken, String methodName) {
        String secretKey = DigestUtils.md5Hex(accessToken + OkAppProperties.APPLICATION_SECRET_KEY);
        String sig = "application_key="
                + OkAppProperties.APPLICATION_KEY
                + "format=jsonmethod="
                + methodName
                + secretKey;

        return DigestUtils.md5Hex(sig);
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }

}

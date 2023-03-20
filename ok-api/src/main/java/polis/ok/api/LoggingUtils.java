package polis.ok.api;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

class LoggingUtils {

    static final String ERROR_DESCRIPTION = "error_description";
    static final String ERROR = "error";

    static HttpResponse<String> sendRequest(HttpClient client, HttpRequest request, Logger logger) throws IOException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static JSONObject parseResponse(HttpResponse<String> response, Logger logger) throws OkApiException {
        return parseResponse(response.body(), response.toString(), logger);
    }

    static org.apache.http.HttpResponse sendRequest(org.apache.http.client.HttpClient client, HttpEntityEnclosingRequestBase request, Logger logger) throws IOException {
        try {
            return client.execute(request);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    static JSONObject parseResponse(org.apache.http.HttpResponse response, Logger logger) throws OkApiException {
        String body = apacheResponseBody(response);
        return parseResponse(body, response.getStatusLine().toString(), logger);
    }

    static OkApiException wrapAndLog(JSONException e, String responseStatus, String responseBody, Logger logger) {
        logger.error("Failed to parse response. " + e.getMessage() + "\nResponse: \n" + responseStatus + "\n" + responseBody + '\n');
        return new OkApiException("Сервер одноклассников ответил в некорректном формате", e);
    }

    private static JSONObject parseResponse(String responseBody, String responseStatus, Logger logger) throws OkApiException {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            if (jsonResponse.has(LoggingUtils.ERROR)) {
                String error = jsonResponse.getString(LoggingUtils.ERROR);
                String errorDescription = jsonResponse.getString(LoggingUtils.ERROR_DESCRIPTION);
                logger.error("Received error from OK " + error + ": " + errorDescription + "\nResponse: \n" + responseStatus + "\n" + responseBody + '\n');
                throw new OkApiException("Получена ошибка от сервера одноклассников " + error + ": " + errorDescription);
            }
            return jsonResponse;
        } catch (JSONException e) {
            throw wrapAndLog(e, responseBody, responseStatus, logger);
        }
    }

    private static String apacheResponseBody(org.apache.http.HttpResponse response) {
        try {
            return new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void logURIException(URISyntaxException e, Logger logger, Object... injectedParams) {
        logger.error("URIException" + e.getMessage() + " with user provided params: " + Arrays.toString(injectedParams));
    }

}

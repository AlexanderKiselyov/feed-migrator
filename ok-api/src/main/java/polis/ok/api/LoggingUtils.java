package polis.ok.api;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import polis.ok.api.exceptions.CodeExpiredException;
import polis.ok.api.exceptions.OkApiException;

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
    private static final String CODE_EXPIRED_ERROR_CODE = "102";
    private static final String ERROR_DESCRIPTION = "error_description";
    private static final String ERROR = "error";
    private static final String ERROR_MSG = "error_msg";
    private static final String ERROR_CODE = "error_code";

    static HttpResponse<String> sendRequest(HttpClient client, HttpRequest request, Logger logger) throws IOException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            logger.error(e + " when sending " + request.toString());
            throw e;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static JSONObject parseResponse(HttpResponse<String> response, Logger logger) throws OkApiException {
        return parseResponse(response.body(), response.toString(), logger);
    }

    static org.apache.http.HttpResponse sendRequest(org.apache.http.client.HttpClient client,
                                                    HttpEntityEnclosingRequestBase request, Logger logger)
            throws IOException {
        try {
            return client.execute(request);
        } catch (IOException e) {
            logger.error(e + " when sending " + request.toString());
            throw e;
        }
    }

    static JSONObject parseResponse(org.apache.http.HttpResponse response, Logger logger) throws OkApiException {
        String body = apacheResponseBody(response);
        return parseResponse(body, response.getStatusLine().toString(), logger);
    }

    static OkApiException wrapAndLog(JSONException e, String responseStatus, String responseBody, Logger logger) {
        logger.error("Failed to parse response. " + e.getMessage() + "\nResponse: \n" + responseStatus + "\n"
                + responseBody + '\n');
        return new OkApiException("Сервер Одноклассников ответил в некорректном формате", e);
    }

    static JSONObject parseResponse(String responseBody, String responseStatus, Logger logger)
            throws OkApiException {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            checkForApiErrors(responseBody, responseStatus, logger, jsonResponse);
            return jsonResponse;
        } catch (JSONException e) {
            throw wrapAndLog(e, responseBody, responseStatus, logger);
        }
    }

    private static void checkForApiErrors(String responseBody, String responseStatus, Logger logger, JSONObject jsonResponse) throws OkApiException {
        String errorCode;
        String errorDesc;

        if (jsonResponse.has(ERROR_CODE)) {
            errorCode = String.valueOf(jsonResponse.getInt(ERROR_CODE));
            errorDesc = jsonResponse.getString(ERROR_MSG);
        } else if (jsonResponse.has(ERROR)) {
            errorCode = jsonResponse.getString(ERROR);
            errorDesc = jsonResponse.getString(ERROR_DESCRIPTION);
        } else {
            return;
        }

        logger.error("Received error from OK. %s: %s\nResponse: \n%s\n%s\n"
                .formatted(errorCode, errorDesc, responseStatus, responseBody)
        );

        if (errorCode.equals(CODE_EXPIRED_ERROR_CODE)) {
            throw new CodeExpiredException();
        }

        throw new OkApiException("Получена ошибка от сервера Одноклассников " + errorCode + ": " + errorDesc);
    }

    static String apacheResponseBody(org.apache.http.HttpResponse response) {
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
        logger.error("URIException" + e.getMessage() + " with user provided params: "
                + Arrays.toString(injectedParams));
    }

}

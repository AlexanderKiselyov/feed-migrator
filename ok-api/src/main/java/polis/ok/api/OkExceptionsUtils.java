package polis.ok.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.net.http.HttpResponse;

public class OkExceptionsUtils {

    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String ERROR = "error";

    public static void handleResponseParsing(Logger logger, JSONException e, HttpResponse<?> response) throws OkApiException {
        logger.error("Failed to parse response. " + e.getMessage() + "\nResponse: \n" + response.toString() + "\n" + response.body() + '\n');
        throw new OkApiException("Сервер одноклассников ответил в некорректном формате", e);
    }

    public static void checkError(Logger logger, JSONObject jsonResponse, HttpResponse<?> response) throws OkApiException {
        try {
            if (jsonResponse.has(OkExceptionsUtils.ERROR)) {
                String error = jsonResponse.getString(OkExceptionsUtils.ERROR);
                String errorDescription = jsonResponse.getString(OkExceptionsUtils.ERROR_DESCRIPTION);
                logger.error("Received error from OK " + error + ": " + errorDescription + "\nResponse: \n" + response.toString() + "\n" + response.body() + '\n');
                throw new OkApiException("Получена ошибка от сервера одноклассников " + error + ": " + errorDescription);
            }
        } catch (JSONException e) {
            handleResponseParsing(logger, e, response);
        }
    }

}

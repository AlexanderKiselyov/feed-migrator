package polis.ok.api;

public class OkApiException extends Exception {
    public OkApiException() {
    }

    public OkApiException(String message) {
        super(message);
    }

    public OkApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public OkApiException(Throwable cause) {
        super(cause);
    }

    public OkApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

package polis.ok.api.exceptions;

public class OkApiException extends Exception {
    public OkApiException() {
        super();
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

}

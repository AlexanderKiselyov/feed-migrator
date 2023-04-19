package polis.dataCheck.api;

import java.io.IOException;

public class OkApiException extends IOException {
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

}

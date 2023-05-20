package polis.ok.api.exceptions;

public class CodeExpiredException extends OkApiException {
    public CodeExpiredException() {
    }

    public CodeExpiredException(String message) {
        super(message);
    }

    public CodeExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodeExpiredException(Throwable cause) {
        super(cause);
    }
}

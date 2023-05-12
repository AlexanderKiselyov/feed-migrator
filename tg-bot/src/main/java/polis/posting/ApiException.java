package polis.posting;

public class ApiException extends Exception {
    private final Integer code;

    public ApiException() {
        super();
        this.code = 0;
    }

    public ApiException(String message) {
        super(message);
        this.code = 0;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.code = 0;
    }

    public ApiException(Throwable cause) {
        super(cause);
        this.code = 0;
    }

    public ApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = 0;
    }

    public ApiException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(Integer code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}

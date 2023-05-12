package polis.vk.api.exceptions;

public class VkApiException extends Exception {
    private final Integer code;

    public VkApiException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public VkApiException(String message) {
        super(message);
        this.code = 0;
    }

    public Integer getCode() {
        return code;
    }
}
